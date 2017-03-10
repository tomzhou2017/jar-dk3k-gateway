package com.dk3k.framework.hbase.dao.impl.value;

/**
 * Created by lilin on 2016/11/11.
 */
public interface Value {

    public byte[] toBytes();

    public String getType();

}
