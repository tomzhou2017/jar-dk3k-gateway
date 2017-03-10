package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.Date;

/**
 * Created by lilin on 2016/11/14.
 */
public class DateValue implements Value {

    private Date dateValue;

    public DateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    @Override
    public byte[] toBytes() {
        long date = dateValue.getTime();
        return Bytes.toBytes(date);
    }

    @Override
    public String getType() {
        return "Date Value";
    }
}
