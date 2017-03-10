package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

import java.math.BigDecimal;

public class BigDecimalValue implements Value {

    private BigDecimal bigDecimal;

    public BigDecimalValue(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public BigDecimal getbigDecimal() {
        return bigDecimal;
    }

    public void setbigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    @Override
    public byte[] toBytes() {
        return Bytes.toBytes(bigDecimal);
    }

    @Override
    public String getType() {
        return "BigDecimal Value";
    }
}
