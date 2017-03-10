package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by lilin on 2016/11/14.
 */
public class LongValue implements Value {

    private long longValue;

    public LongValue(long longValue) {
        this.longValue = longValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    @Override
    public byte[] toBytes() {
        return Bytes.toBytes(longValue);
    }

    @Override
    public String getType() {
        return "Long Value";
    }
}
