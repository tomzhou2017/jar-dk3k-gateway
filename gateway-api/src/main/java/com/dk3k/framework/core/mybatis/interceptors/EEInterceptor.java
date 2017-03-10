package com.dk3k.framework.core.mybatis.interceptors;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
/**
 * 鹰眼SQL监控拦截器
 * 去除更新操作监控，只监控查询语句
 * @Signature(args = { MappedStatement.class,Object.class }, method = "update", type = Executor.class),
 *
 * Copyright @ 2013QIANLONG.
 * All right reserved.
 * Class Name : com.mobanker.config
 * Description :
 * Author : cailinfeng
 * Date : 2016/6/14
 */

@Intercepts({@Signature(args = { MappedStatement.class,Object.class,RowBounds.class, ResultHandler.class }, method = "query", type = Executor.class)})
public class EEInterceptor implements Interceptor {

    private String  jdbcUrl= "";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //得到 类名-方法
        String[] strArr = mappedStatement.getId().split("\\.");
        String class_method = strArr[strArr.length-2] + "." + strArr[strArr.length-1];
        //得到sql语句
        Object parameter = null;
        if(invocation.getArgs().length > 1){
            parameter = invocation.getArgs()[1];
        }

        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        mappedStatement.getSqlSource().getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = showSql(configuration, boundSql);
        Transaction t = Cat.newTransaction("SQL", class_method);
        Cat.logEvent("SQL.Statement", sql.substring(0, sql.indexOf(" ")), Message.SUCCESS, sql);
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Cat.logEvent("SQL.Method", sqlCommandType.name().toLowerCase());
        Cat.logEvent("SQL.Database", jdbcUrl);

        Object returnObj = null;
        try {
            returnObj = invocation.proceed();
            t.setStatus(Message.SUCCESS);
        } catch(Exception e){
            t.setStatus(e);
            Cat.logError(e);
            throw e;
        } finally{
            t.complete();
        }
        return returnObj;

    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                //sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
                sql = StringUtils.replaceOnce(sql, "?",getParameterValue(parameterObject));//解決value值中包含字符"$"时解析报错的问题，java.lang.IllegalArgumentException: Illegal group reference

            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        //sql = sql.replaceFirst("\\?", getParameterValue(obj));
                        sql = StringUtils.replaceOnce(sql, "?",getParameterValue(parameterObject));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        //sql = sql.replaceFirst("\\?", getParameterValue(obj));
                        sql = StringUtils.replaceOnce(sql, "?", getParameterValue(parameterObject));
                    }
                }
            }
        }
        return sql;
    }

    private String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    @Override
    public Object plugin(Object target) {
        if(target instanceof Executor)
            return Plugin.wrap(target, this);
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        // TODO Auto-generated method stub

    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
}

