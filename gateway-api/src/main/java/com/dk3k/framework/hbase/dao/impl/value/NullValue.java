package com.dk3k.framework.hbase.dao.impl.value;

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
