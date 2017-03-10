package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

public class FloatValue implements Value {

    private float floatValue;

    public FloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    @Override
    public byte[] toBytes() {
        return Bytes.toBytes(floatValue);
    }

    @Override
    public String getType() {
        return "Float Value";
    }
}
