package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

public class IntValue implements Value {

    private int intValue;

    public IntValue(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    @Override
    public byte[] toBytes() {
        return Bytes.toBytes(intValue);
    }

    @Override
    public String getType() {
        return "Int Value";
    }
}
