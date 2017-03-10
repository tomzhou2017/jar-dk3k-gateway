package com.dk3k.framework.hbase.dao.impl;


import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk3k.framework.hbase.dao.annonation.DatabaseField;
import com.dk3k.framework.hbase.dao.impl.value.Value;
import com.dk3k.framework.hbase.dao.impl.value.ValueFactory;
import com.dk3k.framework.hbase.exception.HBaseORMException;
import com.dk3k.framework.hbase.support.BeanUtil;

/**
 * Created by lilin on 2016/11/12.
 */
public class DataMapper<T> {

    private static final Logger logger = LoggerFactory.getLogger(DataMapper.class);

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
     * this map doesn't contain row key field, and if no annotation {@link @DatabaseField}
     * on field, this map doesn't contain the field.
     */
    private Map<Field, FamilyQualifierSchema> fixedSchemas;

    /**
     * field data type for each data model field, field data type supports primitive {@link FieldDataType#PRIMITIVE},
     * list {@link FieldDataType#LIST}, map {@link FieldDataType#MAP},
     * class {@link FieldDataType#SUBLEVELCLASS} now.
     * <p/>
     * if no annotation {@link @DatabaseField} on field, data type resolved as skip {@link FieldDataType#SKIP}
     * <p/>
     * this map doesn't contain row key field
     */
    private Map<Field, FieldDataType> fieldDataTypes;

    /**
     * the field of row key
     */
    private Field rowKeyField;

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
    private Class<T> clazz;

    /**
     * private data for individual instance
     */
    private Map<Field, FamilyToQualifersAndValues> dataFieldsToFamilyQualifierValue;

    /**
     * private data for rowKey
     */
    private Value rowKey;

    /**
     * Construct with fixed members as parameters
     */
    public DataMapper(String tableName, Map<Field, FamilyQualifierSchema> fixedSchemas, Map<Field, FieldDataType> fieldDataTypes, Field rowKeyField, Class<T> clazz) {
        this.tableName = tableName;
        this.fixedSchemas = fixedSchemas;
        this.fieldDataTypes = fieldDataTypes;
        this.rowKeyField = rowKeyField;
        this.clazz = clazz;
    }

    public Class<T> getModelClass() {
        return clazz;
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

    protected Put getPut() {
        Put put = new Put(rowKey.toBytes());
        // add each field's data to the 'put'
        for (Field field : dataFieldsToFamilyQualifierValue.keySet()) {
            dataFieldsToFamilyQualifierValue.get(field).addToPut(put);
        }
        return put;
    }

    protected void insert(Connection conn) throws HBaseORMException {
        HTable hTable = null;
        try {
            hTable = (HTable) conn.getTable(TableName.valueOf(tableName));
            hTable.put(getPut());
        } catch (Exception e) {
            throw new HBaseORMException(e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    protected void insert(Connection conn, List<Put> puts) throws HBaseORMException {
        HTable hTable = null;
        try {
            hTable = (HTable) conn.getTable(TableName.valueOf(tableName));
            hTable.put(puts);
        } catch (Exception e) {
            throw new HBaseORMException(e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected T createObjectFromResult(Result result) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        T instance = clazz.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.equals(rowKeyField)) {
                byte[] value = result.getRow();
                Object fieldInstance = ValueFactory.createObject(field.getType(), value);
                BeanUtil.setProperty(instance, field, fieldInstance);
                continue;
            }

            FieldDataType fdt = fieldDataTypes.get(field);// datatype info
            FamilyQualifierSchema fqs = fixedSchemas.get(field);// schema info
            if (fdt.isSkip()) {
                continue;
            } else if (fdt.isPrimitive()) {
                byte[] value = result.getValue(fqs.getFamily(), fqs.getQualifier());
                if (value == null)
                    continue;

                Class<?> fieldClazz = fdt.clazz;
                Object fieldInstance = ValueFactory.createObject(fieldClazz, value);// convert from byte[] to Object according to field clazz
                BeanUtil.setProperty(instance, field, fieldInstance);
            } else if (fdt.isMap()) {
                // get qualifier-value map and put the map
                NavigableMap<byte[], byte[]> map1 = result.getFamilyMap(fqs.getFamily());
                if (map1 == null || map1.isEmpty())
                    continue;

                Class<?> fieldClazz = BeanUtil.getFieldGenricType(field, 1);
                Map<String, Object> map2 = new HashMap<String, Object>();
                for (byte[] qualifier : map1.keySet()) {
                    map2.put(Bytes.toString(qualifier), ValueFactory.createObject(fieldClazz, map1.get(qualifier)));
                }
                BeanUtil.setProperty(instance, field, map2);
            } else if (fdt.isList()) {
                // get qualifier names and add the the list
                NavigableMap<byte[], byte[]> map1 = result.getFamilyMap(fqs.getFamily());
                if (map1 == null || map1.isEmpty())
                    continue;

                Class<?> fieldClazz = BeanUtil.getFieldGenricType(field);
                List list = new ArrayList();
                for (byte[] qualifier : map1.keySet()) {
                    list.add(ValueFactory.createObject(fieldClazz, qualifier));
                }
                BeanUtil.setProperty(instance, field, list);
            } else if (fdt.isSubLevelClass()) {
                // get the qualifer-object....
                Object subLevelObject = createSubLevelObject(fqs.getSubFieldToQualifier(), fdt, result.getFamilyMap(fqs.getFamily()));
                if (subLevelObject != null) {
                    BeanUtil.setProperty(instance, field, subLevelObject);
                }
            }
        }

        return instance;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Object createSubLevelObject(Map<String, byte[]> subfieldToQualifier, FieldDataType fdt, NavigableMap<byte[], byte[]> map) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (map == null | map.isEmpty())
            return null;

        Class<?> fieldClazz = fdt.clazz;
        Object fieldInstance = fieldClazz.newInstance();

        for (Field subField : fieldClazz.getDeclaredFields()) {
            FieldDataType subLevelDataType = fdt.getSubLevelDataType(subField);
            String fieldName = subField.getName();
            if (subLevelDataType.isSkip()) {
                continue;
            } else if (subLevelDataType.isPrimitive()) {
                byte[] value = map.get(subfieldToQualifier.get(fieldName));
                Class<?> subFieldClazz = subLevelDataType.clazz;
                Object subFieldInstance = ValueFactory.createObject(subFieldClazz, value);// convert from byte[] to Object according to field clazz
                BeanUtil.setProperty(fieldInstance, subField, subFieldInstance);
            } else if (subLevelDataType.isMap()) {
                NavigableMap<byte[], byte[]> map1 = map;
                Class<?> subFieldClazz = BeanUtil.getFieldGenricType(subField, 1);
                Map<String, Object> map2 = new HashMap<String, Object>();
                for (byte[] qualifier : map1.keySet()) {
                    map2.put(Bytes.toString(qualifier), Bytes.toString(map1.get(qualifier)));
                    map2.put(Bytes.toString(qualifier), ValueFactory.createObject(subFieldClazz, map1.get(qualifier)));
                }
                BeanUtil.setProperty(fieldInstance, subField, map2);
            } else if (subLevelDataType.isList()) {
                NavigableMap<byte[], byte[]> map1 = map;
                Class<?> subFieldClazz = BeanUtil.getFieldGenricType(subField);
                List list = new ArrayList();
                for (byte[] qualifier : map1.keySet()) {
                    list.add(ValueFactory.createObject(subFieldClazz, qualifier));
                }
                BeanUtil.setProperty(fieldInstance, subField, list);
            } else {
                BeanUtil.setProperty(fieldInstance, subField, null);
            }
        }
        return fieldInstance;

    }

    /**
     * Just set the rowKey for the instance
     *
     * @param instance
     */
    protected void setRowKey(T instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            // if is rowkey
            if (rowKeyField.equals(field)) {
                rowKey = ValueFactory.create(BeanUtil.getProperty(instance, field));
                break;
            }
        }
    }

    protected void setFieldValue(T instance, List<String> familyFieldNames) throws HBaseORMException, InvocationTargetException, IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!familyFieldNames.contains(field.getName())) {
                continue;
            }

            // if is rowkey
            if (rowKeyField.equals(field)) {
                rowKey = ValueFactory.create(BeanUtil.getProperty(instance, field));
                continue;
            }

            FamilyQualifierSchema schema = fixedSchemas.get(field);
            FieldDataType fdt = fieldDataTypes.get(field);
            // field not included for HBase
            if (schema == null) {
                continue;
            }

            // Primitive, family and qualifier name are both specified
            if (schema.getQualifier() != null) {
                Value value = ValueFactory.create(BeanUtil.getProperty(instance, field));
                dataFieldsToFamilyQualifierValue.get(field).addQualifierValue(schema.getQualifier(), value);
            } else {
                // user defined class or a list as family data <br/>
                // 1. user defined class, need to add fixed qualifer informtion
                // to the fixedField
                if (fdt.isSubLevelClass()) {
                    Map<byte[], Value> qualifierValues = getQualifierValuesFromInstanceAsFamily(BeanUtil.getProperty(instance, field), schema, fdt);
                    dataFieldsToFamilyQualifierValue.get(field).addQualifierValues(qualifierValues);
                } else if (fdt.isMap()) {
                    // get each key as qualifier and value as value
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) BeanUtil.getProperty(instance, field);
                    for (String key : map.keySet()) {
                        String qualifier = key;
                        Value value = ValueFactory.create(map.get(key));
                        dataFieldsToFamilyQualifierValue.get(field).addQualifierValue(Bytes.toBytes(qualifier), value);
                    }
                } else if (fdt.isList()) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) (BeanUtil.getProperty(instance, field));
                    if (list == null)
                        continue;

                    for (String key : list) {
                        String qualifier = key;
                        Value value = ValueFactory.create(null);
                        dataFieldsToFamilyQualifierValue.get(field).addQualifierValue(Bytes.toBytes(qualifier), value);
                    }
                }

            }
        }

    }

    /**
     * Copy from the fixed schema. All members used in the method are fixed
     * according to the <code>clazz</code>
     *
     * @throws HBaseORMException
     */
    protected void copyToDataFieldSchemaFromFixedSchema() throws HBaseORMException {
        dataFieldsToFamilyQualifierValue = new HashMap<Field, FamilyToQualifersAndValues>();
        for (Field field : fixedSchemas.keySet()) {
            FamilyQualifierSchema schema = fixedSchemas.get(field);
            if (schema.getFamily() == null) {
                throw new HBaseORMException("Family should not be null!");
            }

            FamilyToQualifersAndValues f2qvs = new FamilyToQualifersAndValues();
            f2qvs.setFamily(schema.getFamily());
            dataFieldsToFamilyQualifierValue.put(field, f2qvs);
        }
    }

    /**
     * Create a concret DataMapper instance by filling rowkey, family:qualifier
     * etc
     *
     * @param instance
     * @throws IllegalArgumentException
     * @throws HBaseORMException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("rawtypes")
	protected void copyToDataFieldsFromInstance(T instance) throws IllegalArgumentException, HBaseORMException, IllegalAccessException, InvocationTargetException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            // if is rowkey
            if (rowKeyField.equals(field)) {
                rowKey = ValueFactory.create(BeanUtil.getProperty(instance, field));
                continue;
            }
            FamilyQualifierSchema schema = fixedSchemas.get(field);
            FieldDataType fdt = fieldDataTypes.get(field);
            // field not included for HBase
            if (schema == null) {
                continue;
            }

            // Primitive, family and qualifier name are both specified
            if (schema.getQualifier() != null) {
                Value value = ValueFactory.create(BeanUtil.getProperty(instance, field));
                dataFieldsToFamilyQualifierValue.get(field).addQualifierValue(schema.getQualifier(), value);
            } else {
                // user defined class or a list as family data <br/>
                // user defined class, need to add fixed qualifer informtion to the fixedField
                if (fdt.isSubLevelClass()) {
                    Map<byte[], Value> qualifierValues = getQualifierValuesFromInstanceAsFamily(BeanUtil.getProperty(instance, field), schema, fdt);
                    dataFieldsToFamilyQualifierValue.get(field).addQualifierValues(qualifierValues);
                } else if (fdt.isMap()) {
                    // get each key as qualifier and value as value
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) BeanUtil.getProperty(instance, field);
                    for (String key : map.keySet()) {
                        String qualifier = key;
                        Value value = ValueFactory.create(map.get(key));
                        dataFieldsToFamilyQualifierValue.get(field).addQualifierValue(Bytes.toBytes(qualifier), value);
                    }
                } else if (fdt.isList()) {
                    List list = (List) (BeanUtil.getProperty(instance, field));
                    if (list == null)
                        continue;
                    for (Object key : list) {
                        String qualifier = key.toString();
                        Value value = ValueFactory.create(null);
                        dataFieldsToFamilyQualifierValue.get(field).addQualifierValue(Bytes.toBytes(qualifier), value);
                    }
                }

            }
        }
    }

    /**
     * Build a map {qualifier: value} from the object as family
     *
     * @param instance the object as family
     * @return
     * @throws HBaseORMException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("rawtypes")
	protected Map<byte[], Value> getQualifierValuesFromInstanceAsFamily(Object instance, FamilyQualifierSchema fqs, FieldDataType fdt) throws HBaseORMException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (instance == null) {
            return null;
        }

        Map<byte[], Value> qualifierValues = new HashMap<byte[], Value>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
            if (fdt.isSkip()) {
                // not included in database
                continue;
            }
            Class<?> fieldType = field.getType();
            // primitive type (actually include those UDF class, to which we treat them as toString())
            if (fdt.getSubLevelDataType(field).isPrimitive()) {
                if (!fieldType.isPrimitive()) {
                    logger.debug("This is not good: instance is not primitive nor List nor Map , but " + fieldType + ". We use toString() as value.");
                }
                String qualifier = getDatabaseColumnName(databaseField.qualifierName(), field);
                Value value = ValueFactory.create(BeanUtil.getProperty(instance, field));
                qualifierValues.put(Bytes.toBytes(qualifier), value);

            }
            // Map, maybe HashMap or other map, all converted to Map
            else if (fdt.getSubLevelDataType(field).isMap()) {
                // get each key as qualifier and value as value
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) BeanUtil.getProperty(instance, field);
                for (String key : map.keySet()) {
                    String qualifier = key;
                    Value value = ValueFactory.create(map.get(key));
                    qualifierValues.put(Bytes.toBytes(qualifier), value);
                }

            }
            // List, maybe ArrayList or others list, all converted to List
            else if (fdt.getSubLevelDataType(field).isList()) {
                List list = (List) (BeanUtil.getProperty(instance, field));
                for (Object key : list) {
                    String qualifier = key.toString();
                    Value value = ValueFactory.create(null);
                    qualifierValues.put(Bytes.toBytes(qualifier), value);
                }
            } else {
                //
            }

        }

        return qualifierValues;
    }

    private String getDatabaseColumnName(String string, Field field) {
        if (string.length() == 0) {
            logger.debug("Field " + clazz.getName() + "." + field.getName() + " need to take care of ... field name is used as column name");
            return field.getName();
        }
        return string;
    }

}
