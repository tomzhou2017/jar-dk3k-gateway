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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk3k.framework.hbase.dao.annonation.DatabaseField;
import com.dk3k.framework.hbase.dao.annonation.DatabaseTable;
import com.dk3k.framework.hbase.exception.HBaseORMException;
import com.dk3k.framework.hbase.support.ConverterException;

/**
 * @author adam
 * @version 1.0
 * @since JDK 1.7
 */
public class ModelToDataMapperConverter<T> {

    private static final Logger logger = LoggerFactory.getLogger(ModelToDataMapperConverter.class);

    @SuppressWarnings("rawtypes")
	public DataMapper convert(Class<T> clazz, T instance) throws ConverterException {
        DatabaseTable databaseTable = (DatabaseTable) clazz.getAnnotation(DatabaseTable.class);
        String tableName = null;
        if (databaseTable == null || databaseTable.tableName().length() == 0) {
            logger.debug("Table name is not specified as annotation, use class name instead");
            tableName = clazz.getSimpleName();
        } else {
            tableName = databaseTable.tableName();
        }

        Field rowKeyField = null;
        // set fixed schema
        Map<Field, FamilyQualifierSchema> fixedSchemas = new HashMap<Field, FamilyQualifierSchema>();
        Map<Field, FieldDataType> fieldDataTypes = new HashMap<Field, FieldDataType>();

        for (Field field : clazz.getDeclaredFields()) {
            DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
            if (databaseField == null) {
                // not included in database
                fieldDataTypes.put(field, new FieldDataType(FieldDataType.SKIP, null));
                continue;
            }
            if (databaseField.id()) {
                // set the field as id
                rowKeyField = field;
                continue;
            }

            FamilyQualifierSchema schema = null;
            try {
                schema = DataMapperWrapperFactory.createSchemaAndDataTypeFromField(clazz, databaseField, field, fieldDataTypes);
            } catch (HBaseORMException e) {
                throw new ConverterException(e.getMessage(), e);
            }
            fixedSchemas.put(field, schema);

        }

        DataMapper<T> dataMapper = new DataMapper<T>(tableName, fixedSchemas, fieldDataTypes, rowKeyField, clazz);
        try {
            dataMapper.copyToDataFieldSchemaFromFixedSchema();//copy the fixed schema to datafieldToSchema.
        } catch (HBaseORMException e) {
            throw new ConverterException(e.getMessage(), e);
        }
        if (instance == null) {
            return dataMapper;
        }
        // check type
        if (!instance.getClass().equals(clazz)) {
            return null;
        }

        try {
            dataMapper.copyToDataFieldsFromInstance(instance);//fill value according to ... to Value of dataFieldToSchema;
            return dataMapper;
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        }
    }
}
