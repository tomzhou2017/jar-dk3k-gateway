package com.dk3k.framework.core.bussiness;

import com.alibaba.fastjson.JSONObject;
import com.dk3k.framework.core.entity.BaseEntity;
import com.dk3k.framework.core.exception.DataCommitException;
import com.dk3k.framework.core.mybatis.Sort;
import com.dk3k.framework.core.mybatis.complexQuery.CustomQueryParam;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface BaseBussiness<T extends BaseEntity> {

	/**
	 * 获取所有列表对象，如果数据量较大，请勿使用
	 * @return
	 */
	public List<T> getAll();

	/**
	 * 根据主键Id获取对应对象
	 * @param id
	 * @return
	 */
	public T getById(Long id);


	/**
	 * 根据参数查询并获得条数
	 * @param params
	 * @return
	 */
	public int count(T params);
	/**
	 * 使用like参数查询并获得结果集条数
	 * @param findParams
	 * @return
	 */
	public int countLike(T findParams);
	/**
	 * 根据参数查询并获得结果集条数
	 * @param customQueryParams
	 * @return
	 */
    public int countQuery(List<CustomQueryParam> customQueryParams);
	/**
	 * 根据参数查询并获得结果集
	 * @param customQueryParams
	 * @return
	 */
    public List<T> query(List<CustomQueryParam> customQueryParams);

	/**
	 * 根据参数查询获得结果集，并根据参数分页
	 * @param customQueryParams
	 * @return
	 */
    public List<T> query(List<CustomQueryParam> customQueryParams, Integer start, Integer limit, List<Sort> sortList);
	/**
	 * 根据参数查询获得结果集，并根据参数分页
	 * @param  findParams
	 * @return
	 */
	public List<T> find(T findParams, Integer start, Integer limit);
	/**
	 * 根据参数查询获得结果集
	 * @param  findParams
	 * @return
	 */
	public List<T> findByObj(T findParams);
	/**
	 * 插入单条记录
	 * @param
	 * @return
	 */
	public void insert(T t) throws DataCommitException;
	/**
	 * 插入单条记录
	 * @param
	 * @return
	 */
	public void insert(List<T> list) throws DataCommitException;
	/**
	 * 批量插入多条记录
	 * @param
	 * @return
	 */
	void insertBatch(List<T> list) throws DataCommitException;
	/**
	 * 根据ID删除记录
	 * @param
	 * @return
	 */
	public void deleteById(Long id) throws DataCommitException;
	/**
	 * 根据ID删除记录
	 * @param
	 * @return
	 */
	public void deleteById(List<Long> list) throws DataCommitException;
	/**
	 * 根据单表对象删除记录
	 * @param
	 * @return
	 */
	public void delete(T t) throws DataCommitException;
	/**
	 * 根据参数对象删除记录
	 * @param
	 * @return
	 */
	public void delete(List<T> list) throws DataCommitException;
	/**
	 * 删除该表所有记录
	 * @param
	 * @return
	 */
    public void deleteAll() throws DataCommitException;
	/**
	 * 更新表
	 * @param
	 * @return
	 */
	public void update(T t) throws DataCommitException;
	/**
	 * 更新表
	 * @param
	 * @return
	 */
	public void update(List<T> list) throws DataCommitException;

//    public void export(OutputStream outputStream, String sheetName, JSONArray columns,JSONObject queryFilter) throws IOException, WriteException, InvocationTargetException,
//            IllegalAccessException,
//            NoSuchMethodException;
	/**
	 * 根据参数查询结果集
	 * @param
	 * @return
	 */
    public List<T> findForExport(JSONObject jsonParams) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
	/**
	 * 根据参数查询结果集条数
	 * @param
	 * @return
	 */
    public int countForExport(JSONObject queryParams) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
	/**
	 * 根据参数查询并获得结果集
	 * @param findParams
	 * @return
	 */
    public List<T> getByObj(T findParams) ;


}
