package com.dk3k.framework.core.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Lait on 11/7/2016.
 * 在分库分表的情况下，数据库id是都自增还是由外部统一指定，如果是自增的话，id不需要设置，所有在批量生成时，要避免id被生成sql
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoId {
    boolean value() default false;
}
