package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by lilin on 2016/11/14.
 */
public class DoubleValue implements Value {

    private double doubleValue;

    public DoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    @Override
    public byte[] toBytes() {
        return Bytes.toBytes(doubleValue);
    }

    @Override
    public String getType() {
        return "Double Value";
    }
}
