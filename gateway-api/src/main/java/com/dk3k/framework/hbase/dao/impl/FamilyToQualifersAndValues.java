package com.dk3k.framework.hbase.dao.impl;

import com.dk3k.framework.hbase.dao.impl.value.Value;
import org.apache.hadoop.hbase.client.Put;

import java.util.HashMap;
import java.util.Map;

/**
 * like a map from a family to a list of qualifers
 * <p>
 */
public class FamilyToQualifersAndValues {

    private byte[] family;
    private Map<byte[], Value> qualifierValues = new HashMap<byte[], Value>();

    public FamilyToQualifersAndValues() {
    }

    public FamilyToQualifersAndValues(byte[] family, Map<byte[], Value> qualiferValues) {
        this.family = family;
        this.qualifierValues = qualiferValues;
    }

    public byte[] getFamily() {
        return family;
    }

    public void setFamily(byte[] family) {
        this.family = family;
    }

    public Map<byte[], Value> getQualifierValues() {
        return qualifierValues;
    }

    public void setQualifierValues(Map<byte[], Value> qualifierValues) {
        this.qualifierValues = qualifierValues;
    }

    /**
     * add qualifier and value to the map
     *
     * @param qualifier
     * @param value
     */
    public void addQualifierValue(byte[] qualifier, Value value) {
        if (this.qualifierValues == null)
            this.qualifierValues = new HashMap<byte[], Value>();
        this.qualifierValues.put(qualifier, value);
    }

    public void addQualifierValues(Map<byte[], Value> qualifierValues) {
        if (this.qualifierValues == null)
            return;
        this.qualifierValues.putAll(qualifierValues);
    }

    /**
     * add family:qualifier->value to a Put
     *
     * @param put
     * @return
     */
    public Put addToPut(Put put) {
        if (put == null) {
            return null;
        }
        for (byte[] qualifier : qualifierValues.keySet()) {
            try {
                Value value = qualifierValues.get(qualifier);
                if (!"Null Value".equals(value.getType())) {
                    put.addColumn(family, qualifier, value.toBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return put;
    }

}
