package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

import java.sql.Timestamp;

public class TimestampValue implements Value {

    private Timestamp timestampValue;

    public TimestampValue(Timestamp timestampValue) {
        this.timestampValue = timestampValue;
    }

    public Timestamp gettimestampValue() {
        return timestampValue;
    }

    public void settimestampValue(Timestamp timestampValue) {
        this.timestampValue = timestampValue;
    }

    @Override
    public byte[] toBytes() {
        long timestamp = timestampValue.getTime();
        return Bytes.toBytes(timestamp);
    }

    @Override
    public String getType() {
        return "Timestamp Value";
    }
}
