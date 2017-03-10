package com.dk3k.framework.hbase.dao.impl.value;

/**
 * Created by lilin on 2016/11/14.
 */
public class NullValue implements Value {

    @Override
    public byte[] toBytes() {
        return null;
    }

    @Override
    public String getType() {
        return "Null Value";
    }
}
