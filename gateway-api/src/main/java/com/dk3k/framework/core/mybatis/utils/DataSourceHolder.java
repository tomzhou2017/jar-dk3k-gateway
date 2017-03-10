package com.dk3k.framework.core.mybatis.utils;

import javax.sql.DataSource;

/**
 * DataSourceHolder
 *
 * @author 傅相奎
 * @date 2016-08-04
 */
public class DataSourceHolder {

	private static final String MASTER = "master";

	private static final String SLAVE = "slave";

	/**
	 * dataSource master or slave
	 */
	private static final ThreadLocal<String> dataSources = new ThreadLocal<String>();

	/**
	 * master local
	 */
	private static final ThreadLocal<DataSource> masterLocal = new ThreadLocal<DataSource>();

	/**
	 * master local
	 */
	private static final ThreadLocal<DataSource> slaveLocal = new ThreadLocal<DataSource>();

	/**
	 * 设置数据源
	 * 
	 * @param dataSourceKey
	 * @author 傅相奎
	 * @date 2016-08-04
	 */
	private static void setDataSource(String dataSourceKey) {
		dataSources.set(dataSourceKey);
	}

	/**
	 * 获取数据源
	 * 
	 * @return
	 * @author 傅相奎
	 * @date 2016-08-04
	 */
	private static String getDataSource() {
		return (String) dataSources.get();
	}

	/**
	 * 标志为master
	 */
	public static void setMaster() {
		setDataSource(MASTER);
	}

	/**
	 * 标志为slave
	 */
	public static void setSlave() {
		setDataSource(SLAVE);
	}
	
	/**
	 * 将master放入threadlocal
	 * @param master
	 */
	public static void setMaster(DataSource master) {
		masterLocal.set(master);
	}
	
	/**
	 * 将slave放入threadlocal
	 * @param slave
	 */
	public static void setSlave(DataSource slave) {
		slaveLocal.set(slave);
	}

	
	public static boolean isMaster() {
		return getDataSource() == MASTER;
	}

	public static boolean isSlave() {
		return getDataSource() == SLAVE;
	}

	/**
	 * 清除thread local中的数据源
	 * 
	 * @author 傅相奎
	 * @date 2016-08-04
	 */
	public static void clearDataSource() {
		dataSources.remove();
		masterLocal.remove();
		slaveLocal.remove();
	}
}
