package com.dk3k.framework.hbase.dao.annonation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by lilin on 2016/11/11.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface DatabaseTable {

    /**
     * The name of the column in the database. If not set then the name is taken
     * from the class name lowercased.
     */
    String tableName() default "";

    boolean canBeFamily() default false;

}
