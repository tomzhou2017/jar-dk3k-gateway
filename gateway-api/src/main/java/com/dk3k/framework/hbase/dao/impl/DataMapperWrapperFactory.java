/*
 *    COPYRIGHT 2016-~ THE WWW.MOBANK.COM ARCH TEAM
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.dk3k.framework.hbase.dao.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.util.Bytes;

import com.dk3k.framework.hbase.dao.annonation.DatabaseField;
import com.dk3k.framework.hbase.dao.annonation.DatabaseTable;
import com.dk3k.framework.hbase.exception.HBaseORMException;
import com.dk3k.framework.hbase.support.ConverterException;

/**
 * @author adam
 * @version 1.0
 * @since JDK 1.7
 */
public class DataMapperWrapperFactory<T> extends DataMapperFactory<T> {

    protected ModelToDataMapperConverter<T> converter;

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public DataMapperWrapperFactory(Class<T> clazz) throws HBaseORMException {
        super(clazz);
        converter = new ModelToDataMapperConverter();
    }

    public static FamilyQualifierSchema createSchemaAndDataTypeFromField(Class<?> clazz, DatabaseField databaseField, Field field, Map<Field, FieldDataType> fieldDataTypes) throws HBaseORMException {
        String family;
        String qualifier;
        Map<String, byte[]> subFieldToQualifier = null;

        // primitive type or string
        if (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().equals(Integer.class)
                || field.getType().equals(Long.class) || field.getType().equals(Double.class) || field.getType().equals(Float.class)
                || field.getType().equals(Date.class) || field.getType().equals(Timestamp.class) || field.getType().equals(BigDecimal.class)) {
            if (databaseField.familyName().length() == 0) {
                throw new HBaseORMException("For primitive typed field " + clazz.getName() + "." + field.getName() + " we must define family with annotation: familyName=\"familyname\".");
            } else {
                family = getDatabaseColumnName(clazz, databaseField.familyName(), field);
                qualifier = getDatabaseColumnName(clazz, databaseField.qualifierName(), field);
                fieldDataTypes.put(field, new FieldDataType(FieldDataType.PRIMITIVE, field.getType()));
            }
        } else if (field.getType().equals(List.class) || databaseField.isQualiferList()) {
            family = getDatabaseColumnName(clazz, databaseField.familyName(), field);
            qualifier = null;
            if (!databaseField.isQualiferList()) {
                logger.warn("Field " + field.getName() + " is not 'List' (maybe a ArrayList??) but set as qualifierList. May be wrong when converted to 'List' ...");
            }
            fieldDataTypes.put(field, new FieldDataType(FieldDataType.LIST, field.getType()));
        } else if (field.getType().equals(Map.class) || databaseField.isQualifierValueMap()) {
            family = getDatabaseColumnName(clazz, databaseField.familyName(), field);
            qualifier = null;
            if (!databaseField.isQualifierValueMap()) {
                logger.warn("Field " + field.getName() + " is not 'Map' (maybe a HashMap??) but set as qualifierValueMap. May be wrong when converted to 'Map' ...");
            }
            fieldDataTypes.put(field, new FieldDataType(FieldDataType.MAP, field.getType()));
        } else {
            family = getDatabaseColumnName(clazz, databaseField.familyName(), field);
            qualifier = null;

            // check whether is a sub level class as family
            DatabaseTable subdatabasetable = (DatabaseTable) field.getType().getAnnotation(DatabaseTable.class);
            if (subdatabasetable != null && subdatabasetable.canBeFamily()) {
                // create a FieldDataType and later
                FieldDataType fieldDataTypeForFamilyClass = new FieldDataType(FieldDataType.SUBLEVELCLASS, field.getType());
                fieldDataTypes.put(field, fieldDataTypeForFamilyClass);

                for (Field subField : field.getType().getDeclaredFields()) {
                    DatabaseField subDatabasefield = subField.getAnnotation(DatabaseField.class);
                    if (subDatabasefield == null) {
                        fieldDataTypeForFamilyClass.addSubLevelDataType(subField, new FieldDataType(FieldDataType.SKIP, null));
                        continue;
                    }
                    byte[] subQualifierName;
                    // if is list or map, skip this
                    if (subDatabasefield.isQualiferList()) {
                        fieldDataTypeForFamilyClass.addSubLevelDataType(subField, new FieldDataType(FieldDataType.LIST, subField.getType()));
                        continue;
                    } else if (subDatabasefield.isQualifierValueMap()) {
                        fieldDataTypeForFamilyClass.addSubLevelDataType(subField, new FieldDataType(FieldDataType.MAP, subField.getType()));
                        continue;
                    }
                    // field name as qualifier name. Whatever primitive or String or UDF class, we treat is as PRIMITIVE
                    else {
                        fieldDataTypeForFamilyClass.addSubLevelDataType(subField, new FieldDataType(FieldDataType.PRIMITIVE, subField.getType()));
                        subQualifierName = Bytes.toBytes(getDatabaseColumnName(clazz, subDatabasefield.qualifierName(), subField));
                    }
                    // initial once
                    if (subFieldToQualifier == null) {
                        subFieldToQualifier = new HashMap<String, byte[]>();
                    }
                    subFieldToQualifier.put(subField.getName(), subQualifierName);
                }
            }
        }

        FamilyQualifierSchema schema = new FamilyQualifierSchema();
        schema.setFamily(Bytes.toBytes(family));
        if (qualifier == null) {
            schema.setQualifier(null);
        } else {
            schema.setQualifier(Bytes.toBytes(qualifier));
        }
        schema.setSubFieldToQualifier(subFieldToQualifier);

        return schema;

    }

    protected static String getDatabaseColumnName(Class<?> clazz, String columnName, Field field) {
        if (columnName.length() == 0) {
            logger.debug("Field " + clazz.getName() + "." + field.getName() + " need to take care of ... field name is used as column name");
            return field.getName();
        }
        return columnName;
    }

    @SuppressWarnings("unchecked")
	@Override
    protected DataMapper<T> create(T instance) throws HBaseORMException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        try {
            return converter.convert(clazz, instance);
        } catch (ConverterException e) {
            throw new HBaseORMException(e);
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected DataMapper<T> createEmpty() throws HBaseORMException {
        try {
            return converter.convert(clazz, null);
        } catch (ConverterException e) {
            throw new HBaseORMException(e);
        }
    }

}
