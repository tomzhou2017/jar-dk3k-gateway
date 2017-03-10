package com.dk3k.framework.hbase.dao.impl.value;

import org.apache.hadoop.hbase.util.Bytes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class ValueFactory {

    static Class<String> STRING = String.class;
    static Class<Integer> INTEGER = Integer.class;
    static Class<Long> LONG = Long.class;
    static Class<Double> DOUBLE = Double.class;
    static Class<Float> FLOAT = Float.class;
    static Class<BigDecimal> BIGDECIMAL = BigDecimal.class;
    static Class<Timestamp> TIMESTAMP = Timestamp.class;
    static Class<Date> DATE = Date.class;

    public static <T> Value create(T instance) {
        if (instance == null) {
            return new NullValue();
        }

        Class<? extends Object> clazz = instance.getClass();
        if (clazz.equals(STRING)) {
            return new StringValue((String) instance);
        } else if (clazz.equals(INTEGER)) {
            return new IntValue((Integer) instance);
        } else if (clazz.equals(LONG)) {
            return new LongValue((Long) instance);
        } else if (clazz.equals(DOUBLE)) {
            return new DoubleValue((Double) instance);
        } else if (clazz.equals(FLOAT)) {
            return new FloatValue((Float) instance);
        } else if (clazz.equals(BIGDECIMAL)) {
            return new BigDecimalValue((BigDecimal) instance);
        } else if (clazz.equals(TIMESTAMP)) {
            return new TimestampValue((Timestamp) instance);
        } else if (clazz.equals(DATE)) {
            return new DateValue((Date) instance);
        } else {
            return new StringValue((String) instance.toString());
        }

    }

    /**
     * Create a Object with type clazz from byte[]
     *
     * @param clazz
     * @param bytes
     * @return
     */
    public static Object createObject(Class<?> clazz, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (clazz.equals(STRING)) {
            return new String(Bytes.toString(bytes));
        } else if (clazz.equals(int.class)) {
            return new Integer(Bytes.toInt(bytes));
        } else if (clazz.equals(long.class)) {
            return new Long(Bytes.toLong(bytes));
        } else if (clazz.equals(double.class)) {
            return new Double(Bytes.toDouble(bytes));
        } else if (clazz.equals(float.class)) {
            return new Float(Bytes.toFloat(bytes));
        } else if (clazz.equals(INTEGER)) {
            try {
                return new Integer(Bytes.toInt(bytes));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else if (clazz.equals(LONG)) {
            try {
                return new Long(Bytes.toLong(bytes));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else if (clazz.equals(DOUBLE)) {
            try {
                return new Double(Bytes.toDouble(bytes));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else if (clazz.equals(FLOAT)) {
            try {
                return new Float(Bytes.toFloat(bytes));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else if (clazz.equals(BIGDECIMAL)) {
            try {
                return Bytes.toBigDecimal(bytes);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else if (clazz.equals(TIMESTAMP)) {
            try {
                return new Timestamp(Bytes.toLong(bytes));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else if (clazz.equals(DATE)) {
            try {
                return new Date(Bytes.toLong(bytes));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else {
            return Bytes.toString(bytes);
        }
    }

    /*
     * used when directly create a Value
	 */
    public static Value TypeCreate(String value) {
        return new StringValue(value);
    }

}
