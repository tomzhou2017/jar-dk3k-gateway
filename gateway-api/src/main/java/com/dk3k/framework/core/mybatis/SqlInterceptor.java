package com.dk3k.framework.core.mybatis;

import java.sql.Connection;
import java.util.Properties;

import com.dk3k.framework.core.mybatis.utils.DataSourceHolder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk3k.framework.core.mybatis.dialect.Dialect;
import com.dk3k.framework.core.mybatis.dialect.MySql5Dialect;
import com.dk3k.framework.core.mybatis.dialect.OracleDialect;

@SuppressWarnings("all")
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class SqlInterceptor implements Interceptor {
	private static final String SELECT = "SELECT";

	private static final String INSERT = "INSERT";

	private static final String DELETE = "DELETE";

	private static final String UPDATE = "UPDATE";
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private static ThreadLocal<RowBounds> rowBounds = new ThreadLocal<RowBounds>();

	public static RowBounds getRowBounds() {
		RowBounds rowBounds = SqlInterceptor.rowBounds.get();
		SqlInterceptor.rowBounds.remove();
		return rowBounds;
	}

	public static void setRowBounds(RowBounds rowBounds) {
		SqlInterceptor.rowBounds.set(rowBounds);
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		BoundSql boundSql = statementHandler.getBoundSql();
		changeDataSource(boundSql);
		MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, new DefaultObjectFactory(),
				new DefaultObjectWrapperFactory());
		RowBounds rowBounds = getRowBounds();
		if (rowBounds == null) {
			rowBounds = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");
		}
		Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
		Dialect.Type databaseType = null;
		try {
			databaseType = Dialect.Type.valueOf(configuration.getVariables().getProperty("dialect").toUpperCase());
		} catch (Exception e) {
			// ignore
		}
		if (databaseType == null) {
			throw new RuntimeException("the value of the dialect property in configuration.xml is not defined : "
					+ configuration.getVariables().getProperty("dialect"));
		}
		Dialect dialect = null;
		switch (databaseType) {
		case MYSQL:
			dialect = new MySql5Dialect();
			break;
		case ORACLE:
			dialect = new OracleDialect();
			break;
		}

		String sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
		if ((rowBounds != null) && (rowBounds != RowBounds.DEFAULT)) {
			sql = dialect.getLimitString(sql, rowBounds.getOffset(), rowBounds.getLimit());
		}

		metaStatementHandler.setValue("delegate.boundSql.sql", sql);
		metaStatementHandler.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
		metaStatementHandler.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
		// logger.debug("SQL : " + boundSql.getSql());
		return invocation.proceed();
	}

	private void changeDataSource(BoundSql boundSql) {
		String sql = boundSql.getSql();
		if (sql.startsWith(SELECT)) {
			// 获取读集群的数据源
			DataSourceHolder.setSlave();
		} else if (sql.startsWith(INSERT) || sql.startsWith(UPDATE) || sql.startsWith(DELETE)) {
			// 获取主库数据源
			DataSourceHolder.setMaster();
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}