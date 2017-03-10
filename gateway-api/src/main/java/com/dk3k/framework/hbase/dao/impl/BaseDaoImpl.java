package com.dk3k.framework.hbase.dao.impl;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.dk3k.framework.hbase.dao.Dao;
import com.dk3k.framework.hbase.dao.connection.HBaseConnectionPool;
import com.dk3k.framework.hbase.dao.impl.value.Value;
import com.dk3k.framework.hbase.dao.impl.value.ValueFactory;
import com.dk3k.framework.hbase.dao.model.Page;
import com.dk3k.framework.hbase.exception.HBaseORMException;
import com.dk3k.framework.hbase.support.BeanUtil;

public abstract class BaseDaoImpl<T> implements Dao<T> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Class<T> clazz;

    private DataMapperWrapperFactory<T> dataMapperFactory = null;

    @Autowired
    private HBaseConnectionPool pool;

    @SuppressWarnings("unchecked")
	public BaseDaoImpl() {
        try {
            this.clazz = BeanUtil.getSuperClassGenricType(super.getClass());
            this.dataMapperFactory = new DataMapperWrapperFactory<T>(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
	public BaseDaoImpl(HBaseConnectionPool pool) {
        try {
            this.clazz = BeanUtil.getSuperClassGenricType(super.getClass());
            this.dataMapperFactory = new DataMapperWrapperFactory<T>(clazz);
            this.pool = pool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

   

    @Override
    public void createTable() throws HBaseORMException {
        if (tableExists()) {
            deleteTable();
        }
        Connection conn = null;
        try {
            conn = pool.getConnection();
            HBaseAdmin admin = (HBaseAdmin) conn.getAdmin();
            admin.createTable(dataMapperFactory.getHTableDescriptor());
            logger.info(dataMapperFactory.getTableCreateScript());
        } catch (Exception e) {
            throw new HBaseORMException("HBase createTable Error", e);
        } finally {
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

    @Override
    public void createTableIfNotExist() throws HBaseORMException {
        if (tableExists()) {
            logger.info("The table " + dataMapperFactory.getTableName() + " has already existed, will not recreate it.");
            return;
        }
        createTable();
    }

    private boolean tableExists() {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            HBaseAdmin admin = (HBaseAdmin) conn.getAdmin();
            return admin.tableExists(dataMapperFactory.getTableName());
        } catch (Exception e) {
            logger.error("HBase tableExists Error", e);
        } finally {
            if (null != conn) {
                pool.release(conn);
            }
        }

        return false;

    }

    private void deleteTable() throws HBaseORMException {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            HBaseAdmin admin = (HBaseAdmin) conn.getAdmin();

            String tableName = dataMapperFactory.getTableName();
            admin.disableTable(tableName);// 在删除一张表前，要使其失效
            admin.deleteTable(tableName);
        } catch (Exception e) {
            throw new HBaseORMException("HBase deleteTable Error", e);
        } finally {
            if (null != conn) {
                pool.release(conn);
            }
        }

    }

    @SuppressWarnings("unchecked")
	@Override
    public void insert(T t) throws HBaseORMException {
        Connection conn = null;
        try {
            T t1 = (T) BeanUtils.cloneBean(t);
            DataMapper<T> dataMapper = dataMapperFactory.create(t1);
            conn = pool.getConnection();
            dataMapper.insert(conn);
        } catch (Throwable e) {
            throw new HBaseORMException(e.getMessage(), e);
        } finally {
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public void insert(List<T> datas) throws HBaseORMException {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            DataMapper<T> mapper = dataMapperFactory.createEmpty();

            // copy对象加密(这样就不需要将原对象解密)
            List<T> tList = new ArrayList<T>();
            for (T data : datas) {
                tList.add((T) BeanUtils.cloneBean(data));
            }

            List<Put> puts = new ArrayList<>();
            for (T t : tList) {
                DataMapper<T> dataMapper = dataMapperFactory.create(t);
                puts.add(dataMapper.getPut());
            }
            mapper.insert(conn, puts);

        } catch (Throwable e) {
            throw new HBaseORMException(e.getMessage(), e);
        } finally {
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

    @Override
    public void delete(T t) throws HBaseORMException {
        Value rowKey = ValueFactory.create(BeanUtil.getProperty(t, dataMapperFactory.getRowKeyField()));
        deleteById(rowKey);

    }

    @Override
    public void deleteById(Value rowKey) throws HBaseORMException {
        delete(new Delete(rowKey.toBytes()));
    }

    @Override
    public void delete(T t, String familyFieldName) throws HBaseORMException {
        if (familyFieldName == null) {
            delete(t);
            return;
        }

        try {
            Value rowKey = ValueFactory.create(BeanUtil.getProperty(t, dataMapperFactory.getRowKeyField()));
            Delete delete = new Delete(rowKey.toBytes());
            Field familyField = t.getClass().getDeclaredField(familyFieldName);
            byte[] family = dataMapperFactory.getFixedSchemas().get(familyField).getFamily();
            delete.addFamily(family);
            delete(delete);
        } catch (Exception e) {
            throw new HBaseORMException(e);
        }

    }

    @Override
    public void delete(T t, String familyFieldName, String qualifierName) throws HBaseORMException {
        if (qualifierName == null) {
            delete(t, familyFieldName);
            return;
        }
        try {
            Value rowKey = ValueFactory.create(BeanUtil.getProperty(t, dataMapperFactory.getRowKeyField()));
            Delete delete = new Delete(rowKey.toBytes());
            Field familyField = t.getClass().getDeclaredField(familyFieldName);
            byte[] family = dataMapperFactory.getFixedSchemas().get(familyField).getFamily();
            byte[] qualifier = getQualiferByFamilyOrSubLevelQualifierName(familyField, qualifierName);
            delete.addColumns(family, qualifier); //删除指定列的所有版本数据
            delete(delete);
        } catch (Exception e) {
            throw new HBaseORMException(e);
        }

    }

    private void delete(Delete delete) throws HBaseORMException {
        Connection conn = null;
        HTable hTable = null;
        try {
            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapperFactory.getTableName()));
            hTable.delete(delete);
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
            if (null != conn) {
                pool.release(conn);
            }
        }

    }

    private byte[] getQualiferByFamilyOrSubLevelQualifierName(Field familyNameField, String qualifierName) throws HBaseORMException {
        // if qualifier name is set with family name
        byte[] qualifier = dataMapperFactory.getFixedSchemas().get(familyNameField).getQualifier();
        if (qualifierName == null) {
            return qualifier;
        }

        // qualifier is not directly set or set with a wrong value
        if (qualifier == null || Bytes.compareTo(qualifier, Bytes.toBytes(qualifierName)) != 0) {
            qualifier = null;
        }

        if (qualifier == null) {
            Map<String, byte[]> subFieldToQualifier = dataMapperFactory.getFixedSchemas().get(familyNameField).getSubFieldToQualifier();
            if (subFieldToQualifier == null) {
                qualifier = null;
            } else if (subFieldToQualifier.get(qualifierName) != null) {
                qualifier = subFieldToQualifier.get(qualifierName);
            } else {
                throw new HBaseORMException("The field '" + qualifierName + "' of sub level family class '" + familyNameField.getName() + "' is not set as qualifier");
            }
            // else qualifier is set with name of the field's name
            if (qualifier == null) {
                qualifier = Bytes.toBytes(qualifierName);
            }
        }
        return qualifier;

    }

    @Override
    public void update(T t) throws HBaseORMException {
        insert(t);
    }

    @Override
    public void update(List<T> datas) throws HBaseORMException {
        insert(datas);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void update(T t, List<String> familyFieldNames) throws HBaseORMException {
        if (familyFieldNames == null) {
            update(t);
            return;
        }
        Connection conn = null;
        try {
            T t1 = (T) BeanUtils.cloneBean(t);

            conn = pool.getConnection();
            DataMapper<T> dataMapper = dataMapperFactory.createEmpty();
            dataMapper.setRowKey(t1);
            dataMapper.setFieldValue(t1, familyFieldNames);
            dataMapper.insert(conn);
        } catch (Exception e) {
            throw new HBaseORMException(e);
        } catch (Throwable throwable) {
            throw new HBaseORMException(throwable.getMessage(), throwable);
        } finally {
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

    @Override
    public boolean exists(Value id) throws HBaseORMException {
        Connection conn = null;
        HTable hTable = null;
        try {
            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapperFactory.getTableName()));
            byte[] rowKey = id.toBytes();
            Get get = new Get(rowKey);
            return hTable.exists(get);
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
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

    @Override
    public T queryById(Value id) throws HBaseORMException {
        DataMapper<T> dataMapper = dataMapperFactory.createEmpty();
        Connection conn = null;
        HTable hTable = null;
        try {
            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapper.getTableName()));
            byte[] rowKey = id.toBytes();
            Get get = new Get(rowKey);
            Result result = hTable.get(get);
            if (result.isEmpty())
                return null;

            T t = dataMapper.createObjectFromResult(result);
            return t;
        } catch (Throwable e) {
            throw new HBaseORMException(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (null != conn) {
                pool.release(conn);
            }
        }

    }

    @Override
    public List<T> queryWithFilter(Filter filter) throws HBaseORMException {
        return this.queryWithFilter(filter, null, null);
    }

    @Override
    public List<T> queryWithFilter(Filter filter, long startTime, long endTime) throws HBaseORMException {
        return this.queryWithFilter(filter, null, null, startTime, endTime);
    }

    @Override
    public List<T> queryWithFilter(Filter filter, String startRow, String stopRow) throws HBaseORMException {
        return this.queryWithFilter(filter, startRow, stopRow, 0, 0);
    }

    @Override
    public List<T> queryWithFilter(Filter filter, String startRow, String stopRow, long startTime, long endTime) throws HBaseORMException {
        List<T> retVals = new ArrayList<T>();
        DataMapper<T> dataMapper = dataMapperFactory.createEmpty();

        Connection conn = null;
        HTable hTable = null;
        try {

            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapper.getTableName()));
            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);
            scan.setMaxResultsPerColumnFamily(500);

            if (StringUtils.isNotEmpty(startRow)) {
                scan.setStartRow(Bytes.toBytes(startRow));
            }
            if (StringUtils.isNotEmpty(stopRow)) {
                scan.setStopRow(Bytes.toBytes(stopRow));
            }

            if (startTime > 0 && endTime > 0) {
                scan.setTimeRange(startTime, endTime);
            }
            scan.setFilter(filter);

            ResultScanner scanner = hTable.getScanner(scan);
            for (Result result : scanner) {
                T t = dataMapper.createObjectFromResult(result);
                retVals.add(t);
            }
            return retVals;
        } catch (Throwable e) {

            throw new HBaseORMException(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (null != conn) {
                pool.release(conn);
            }
        }

    }

    @Override
    public long increment(Value id, String familyName, String qualifierName, long amount) throws HBaseORMException {
        Connection conn = null;
        HTable hTable = null;

        try {
            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapperFactory.getTableName()));
//            byte[] rowKey = id.toBytes();
            byte[] family = Bytes.toBytes(familyName);
            byte[] qualifier = Bytes.toBytes(qualifierName);
            return hTable.incrementColumnValue(id.toBytes(), family, qualifier, amount);
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
            if (null != conn) {
                pool.release(conn);
            }
        }

    }

    @Override
    public long increment(Value id, String familyFieldName, long amount) throws HBaseORMException {
        Connection conn = null;
        HTable hTable = null;

        try {
            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapperFactory.getTableName()));
//            byte[] rowKey = id.toBytes();
            Field familyField = dataMapperFactory.getClazz().getDeclaredField(familyFieldName);
            byte[] family = dataMapperFactory.getFixedSchemas().get(familyField).getFamily();
            byte[] qualifier = getQualiferByFamilyOrSubLevelQualifierName(familyField, null);
            return hTable.incrementColumnValue(id.toBytes(), family, qualifier, amount);
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
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

    @Override
    public Page<T> queryWithFilter(FilterList filterList, String startRow, String stopRow, String rowIndex, int pageSize) throws HBaseORMException {
        return this.queryWithFilter(filterList, startRow, stopRow, 0, 0, rowIndex, pageSize);
    }

    @Override
    public Page<T> queryWithFilter(FilterList filterList, String startRow, String stopRow, long startTime, long endTime, String rowIndex, int pageSize) throws HBaseORMException {
        List<T> retVals = new ArrayList<T>();
        DataMapper<T> dataMapper = dataMapperFactory.createEmpty();

        Connection conn = null;
        HTable hTable = null;
        try {

            Page<T> page = new Page<T>(pageSize);

            conn = pool.getConnection();
            hTable = (HTable) conn.getTable(TableName.valueOf(dataMapper.getTableName()));
            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);
            scan.setMaxResultsPerColumnFamily(1000);

            if (StringUtils.isEmpty(startRow) || StringUtils.isEmpty(stopRow)) {
                throw new HBaseORMException("startRow与stopRow不能为空");
            }

            if (startRow.compareTo(stopRow) > 0) {
                throw new HBaseORMException("开始ID不得大于结束ID");
            }
            if (StringUtils.isNotEmpty(rowIndex)) {
                // rowIndex非法必须在startRow和stopRow之间
                if (startRow.compareTo(rowIndex) > 0 || stopRow.compareTo(rowIndex) < 0) {
                    throw new HBaseORMException("rowIndex非法");
                }
                // rowIndex在startRow和stopRow之间,以rowIndex非法为startRow,忽略startRow
                startRow = rowIndex;
            }

            scan.setStartRow(Bytes.toBytes(startRow));
            scan.setStopRow(Bytes.toBytes(stopRow));

            if (startTime > 0 && endTime > 0) {
                scan.setTimeRange(startTime, endTime);
            }

            PageFilter pageFilter = new PageFilter(page.getPageSize() + 1);//多查一条数据
            filterList.addFilter(pageFilter);

            scan.setFilter(filterList);

            ResultScanner scanner = hTable.getScanner(scan);
            for (Result result : scanner) {
                T t = dataMapper.createObjectFromResult(result);
                retVals.add(t);
            }

            if (retVals.size() > 0) {
                // 查询结果第一条记录的id等于传入的rowIndex,删除第一条记录
                String id = (String) BeanUtil.getProperty(retVals.get(0), dataMapper.getRowKeyField());
                if (id.equals(rowIndex)) {
                    retVals.remove(0);
                }
                // 第一次查询,并且查询结果多一个,去掉多余的结果
                if (retVals.size() > page.getPageSize()) {
                    retVals.remove(retVals.size() - 1);
                }
                if (retVals.size() < page.getPageSize()) {
                    // 已经查询到最后一批,设置返回的nextIndex为null
                    page.setNextIndex(null);
                } else {
                    // 设置返回的nextIndex为最后一条记录的id;
                    id = (String) BeanUtil.getProperty(retVals.get(page.getPageSize() - 1), dataMapper.getRowKeyField());
                    page.setNextIndex(id);
                }
            } else {
                page.setNextIndex(null);
            }
            page.setList(retVals);

            return page;
        } catch (Throwable e) {
            throw new HBaseORMException(e.getMessage(), e);
        } finally {

            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (null != conn) {
                pool.release(conn);
            }
        }
    }

}
