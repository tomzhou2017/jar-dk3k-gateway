package com.dk3k.framework.hbase.dao;


import java.util.List;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;

import com.dk3k.framework.hbase.dao.impl.value.Value;
import com.dk3k.framework.hbase.dao.model.Page;
import com.dk3k.framework.hbase.exception.HBaseORMException;

public interface Dao<T> {

    /**
     * Create a HBase Table according to it's annotations. <br>
     * If the table already exits, delete and then recreate.
     */
    public void createTable() throws HBaseORMException;

    /**
     * Create a HBase Table according to it's annotations. <br>
     * If the table already exits, return.
     */
    public void createTableIfNotExist() throws HBaseORMException;

    /**
     * Insert one record (row) to HBase table
     *
     * @param data
     */
    public void insert(T data) throws HBaseORMException;

    /**
     * Insert records (rows) to HBase table by batch.
     *
     * @param datas
     */
    public void insert(List<T> datas) throws HBaseORMException;

    /**
     * delete the whole data from HBase.
     *
     * @param data
     */
    public void delete(T data) throws HBaseORMException;

    /**
     * delete the row with data's rowkey
     *
     * @param rowkey
     * @throws HBaseORMException
     */
    public void deleteById(Value rowkey) throws HBaseORMException;

    /**
     * Specify field name and delete whole specific family
     */
    public void delete(T data, String familyFieldName) throws HBaseORMException;

    /**
     * Specify field name and delete specific family:qualifier
     */
    public void delete(T data, String familyFieldName, String qualifierName) throws HBaseORMException;

    /**
     * update the record in table according to data's id (rowkey). <br>
     * We don't know the dirty part of the data compared to record in the table,
     * even don't know whether the data has already exists in the table. So, we
     * just <b>Insert</b> the data to the table...
     *
     * @param data
     */
    public void update(T data) throws HBaseORMException;

    public void update(List<T> datas) throws HBaseORMException;

    /**
     * @param data
     * @param familyFieldName
     */
    public void update(T data, List<String> familyFieldName) throws HBaseORMException;

    public boolean exists(Value id) throws HBaseORMException;


    public T queryById(Value id) throws HBaseORMException;

    /**
     * Query according to the filter. For filters, such as qualifier filters.
     *
     * @param filter
     * @return
     */
    public List<T> queryWithFilter(Filter filter) throws HBaseORMException;

    public List<T> queryWithFilter(Filter filter, long startTime, long endTime) throws HBaseORMException;

    public List<T> queryWithFilter(Filter filter, String startRow, String stopRow) throws HBaseORMException;

    public List<T> queryWithFilter(Filter filter, String startRow, String stopRow, long startTime, long endTime) throws HBaseORMException;

    public long increment(Value id, String familyName, String qualifierName, long amount) throws HBaseORMException;

    public long increment(Value id, String familyFieldName, long amount) throws HBaseORMException;

    public Page<T> queryWithFilter(FilterList filterList, String startRow, String stopRow, String rowIndex, int pageSize) throws HBaseORMException;

    public Page<T> queryWithFilter(FilterList filterList, String startRow, String stopRow, long startTime, long endTime, String rowIndex, int pageSize) throws HBaseORMException;


}
