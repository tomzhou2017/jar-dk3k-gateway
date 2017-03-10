package com.dk3k.framework.hbase.dao.impl;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk3k.framework.hbase.dao.annonation.DatabaseField;
import com.dk3k.framework.hbase.dao.annonation.DatabaseTable;
import com.dk3k.framework.hbase.exception.HBaseORMException;

/**
 * This is factory of DataMapper, each Type can has one DataMapperFactory and
 * the factory can create DataMapper according to the instance
 * <p/>
 * Created by lilin on 2016/11/12.
 */
public class DataMapperFactory<T> {

    protected static final Logger logger = LoggerFactory.getLogger(DataMapperFactory.class);

    /**
     * the table name mapped, via annotation on model:
     * <pre>
     *
     * @DatabaseTable(tableName = "T_USER")
     * </pre>
     * if no annotation {@link @DatabaseTable} on model, the simple name of class as the
     * mapping table by default
     */
    protected String tableName;

    /**
     * the family qualifier schema for each model field of the generic T, copy from the factory.
     * the family and qualifier are declared on model field via annotation {@link @DatabaseField}
     * <p/>
     * with key model field, and value family qualifier schema {@link FamilyQualifierSchema}
     * <p/>
     * the row key field is not contained in the map, and if no annotation {@link @DatabaseField}
     * on field, the field is not contained in this map.
     */
    protected Map<Field, FamilyQualifierSchema> fixedSchemas;

    /**
     * field data type for each data model field, field data type supports primitive {@link FieldDataType#PRIMITIVE},
     * list {@link FieldDataType#LIST}, map {@link FieldDataType#MAP},
     * class {@link FieldDataType#SUBLEVELCLASS} now.
     * <p/>
     * if no annotation {@link @DatabaseField} on field, data type resolved as skip {@link FieldDataType#SKIP}
     * <p/>
     * the row key field is not contained in this map
     */
    protected Map<Field, FieldDataType> fieldDataTypes;

    /**
     * the field of row key
     */
    protected Field rowKeyField;

    /**
     * the class of model, the class declares at dao which inherit from base {@link BaseDaoImpl} or implements
     * the interface {@link com.dk3k.framework.hbase.dao.Dao}
     * <pre>
     * class UserDaoImpl extends BaseDaoImpl<User> implements UserDao
     * </pre>
     * <pre>
     * clazz = BeanUtil.getSuperClassGenricType(super.getClass());
     * </pre>
     */
    protected Class<T> clazz;

    public DataMapperFactory(Class<T> clazz) throws HBaseORMException {
        this.clazz = clazz;
        setTableName();
        fixedSchemas = new HashMap<Field, FamilyQualifierSchema>();
        fieldDataTypes = new HashMap<Field, FieldDataType>();
        setFixedSchemasAndDataTypes();
    }

    public String getTableName() {
        return tableName;
    }

    public Field getRowKeyField() {
        return rowKeyField;
    }

    public Map<Field, FamilyQualifierSchema> getFixedSchemas() {
        return fixedSchemas;
    }

    public Map<Field, FieldDataType> getFieldDataTypes() {
        return fieldDataTypes;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    /**
     * if annotation for a field is not set, the field is omitted.
     *
     * @throws HBaseORMException
     */
    private void setFixedSchemasAndDataTypes() throws HBaseORMException {
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

            FamilyQualifierSchema schema = createSchemaAndDataTypeFromField(databaseField, field);
            fixedSchemas.put(field, schema);

        }
    }

    /**
     * Set family, qualifier schema according to the field's annotation.
     * <p/>
     * Side effect:
     * set field data type for each field and also for fields of class as family class (sub level class)
     *
     * @param databaseField
     * @param field
     * @return
     * @throws HBaseORMException
     */
    public FamilyQualifierSchema createSchemaAndDataTypeFromField(DatabaseField databaseField, Field field) throws HBaseORMException {
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
                family = getDatabaseColumnName(databaseField.familyName(), field);
                qualifier = getDatabaseColumnName(databaseField.qualifierName(), field);
                fieldDataTypes.put(field, new FieldDataType(FieldDataType.PRIMITIVE, field.getType()));
            }
        } else if (field.getType().equals(List.class) || databaseField.isQualiferList()) {
            family = getDatabaseColumnName(databaseField.familyName(), field);
            qualifier = null;
            if (!databaseField.isQualiferList()) {
                logger.warn("Field " + field.getName() + " is not 'List' (maybe a ArrayList??) but set as qualifierList. May be wrong when converted to 'List' ...");
            }
            fieldDataTypes.put(field, new FieldDataType(FieldDataType.LIST, field.getType()));
        } else if (field.getType().equals(Map.class) || databaseField.isQualifierValueMap()) {
            family = getDatabaseColumnName(databaseField.familyName(), field);
            qualifier = null;
            if (!databaseField.isQualifierValueMap()) {
                logger.warn("Field " + field.getName() + " is not 'Map' (maybe a HashMap??) but set as qualifierValueMap. May be wrong when converted to 'Map' ...");
            }
            fieldDataTypes.put(field, new FieldDataType(FieldDataType.MAP, field.getType()));
        } else {
            family = getDatabaseColumnName(databaseField.familyName(), field);
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
                        subQualifierName = Bytes.toBytes(getDatabaseColumnName(subDatabasefield.qualifierName(), subField));
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

    protected String getDatabaseColumnName(String columnName, Field field) {
        if (columnName.length() == 0) {
            logger.debug("Field " + clazz.getName() + "." + field.getName() + " need to take care of ... field name is used as column name");
            return field.getName();
        }
        return columnName;
    }

    @SuppressWarnings("deprecation")
	protected HTableDescriptor getHTableDescriptor() {
        HTableDescriptor td = new HTableDescriptor(Bytes.toBytes(tableName));
        for (Field field : fixedSchemas.keySet()) {
            FamilyQualifierSchema schema = fixedSchemas.get(field);
            if (td.getFamily(schema.getFamily()) == null) {
                td.addFamily(new HColumnDescriptor(schema.getFamily()));
            }
        }
        return td;
    }

    /**
     * a helper method to return script to create the HBase table according to fixedSchema
     *
     * @return Script to create the table
     */
    protected String getTableCreateScript() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("create '");
        buffer.append(tableName + "', ");
        for (Field field : fixedSchemas.keySet()) {
            FamilyQualifierSchema schema = fixedSchemas.get(field);
            String family = Bytes.toString(schema.getFamily());
            buffer.append("{NAME => '" + family + "'},");
        }

        return buffer.toString().substring(0, buffer.length() - 1);
    }

    /**
     * data mapper created via factory. if param <code>instance</code> not specific, a
     * data mapper only contains schema mappings returned.
     *
     * @param instance
     * @return
     * @throws HBaseORMException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected DataMapper<T> create(T instance) throws HBaseORMException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // a new DataMapper constructed with the fixed members
        DataMapper<T> dataMapper = new DataMapper<T>(tableName, fixedSchemas, fieldDataTypes, rowKeyField, clazz);
        dataMapper.copyToDataFieldSchemaFromFixedSchema();//copy the fixed schema to datafieldToSchema.
        if (instance == null) {
            return dataMapper;
        }

        // check type
        if (!instance.getClass().equals(clazz)) {
            return null;
        }

        dataMapper.copyToDataFieldsFromInstance(instance);//fill value according to ... to Value of dataFieldToSchema;
        return dataMapper;
    }

    /**
     * Create an empty DataMapper for the instance, uses can further:<br>
     *
     * @return
     * @throws HBaseORMException
     */
    protected DataMapper<T> createEmpty() throws HBaseORMException {
        try {
            return create(null);
        } catch (Exception e) {
            throw new HBaseORMException(e);
        }
    }

    /**
     * if annotation is not set, use class name instead
     */
    private void setTableName() {
        DatabaseTable databaseTable = (DatabaseTable) clazz.getAnnotation(DatabaseTable.class);
        if (databaseTable == null || databaseTable.tableName().length() == 0) {
            logger.info("Table name is not specified as annotation, use class name instead");
            tableName = clazz.getSimpleName();
        } else {
            tableName = databaseTable.tableName();
        }
    }

}
