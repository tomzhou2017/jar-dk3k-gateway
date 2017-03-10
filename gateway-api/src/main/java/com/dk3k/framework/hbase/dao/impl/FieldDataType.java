package com.dk3k.framework.hbase.dao.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldDataType {

    // both in table class and sub-level family class
    public static final int SKIP = -1;
    public static final int PRIMITIVE = 0;
    public static final int LIST = 1;
    public static final int MAP = 2;
    // only in table class
    public static final int SUBLEVELCLASS = 3;

    /**
     * specific type, the data type of field
     * <p>
     * field data type supports primitive {@link FieldDataType#PRIMITIVE},
     * list {@link FieldDataType#LIST}, map {@link FieldDataType#MAP},
     * class {@link FieldDataType#SUBLEVELCLASS} now.
     *
     * if no annotation {@link @DatabaseField} on field, data type resolved as skip {@link FieldDataType#SKIP}
     */
    protected int dataType;

    /**
     * the class type of field
     */
    protected Class<?> clazz;

    /**
     * used only for sub level class
     */
    protected Map<Field, FieldDataType> subLevelDataTypes = null;

    public FieldDataType(int dataType, Class<?> clazz) {
        this.dataType = dataType;
        this.clazz = clazz;
    }

    public boolean isSkip() {
        return dataType == SKIP;
    }

    public boolean isPrimitive() {
        return dataType == PRIMITIVE;
    }

    public boolean isList() {
        return dataType == LIST;
    }

    public boolean isMap() {
        return dataType == MAP;
    }

    public boolean isSubLevelClass() {
        return dataType == SUBLEVELCLASS;
    }

    public void addSubLevelDataType(Field field, FieldDataType fieldDataType) {
        if (subLevelDataTypes == null)
            subLevelDataTypes = new HashMap<Field, FieldDataType>();
        subLevelDataTypes.put(field, fieldDataType);
    }

    public FieldDataType getSubLevelDataType(Field field) {
        return subLevelDataTypes.get(field);
    }

    public String toString() {
        String string = "";
        switch (dataType) {
            case -1:
                string += "Skip. \t " + clazz;
                break;
            case 0:
                string += "Primitive. \t " + clazz;
                break;
            case 1:
                string += "List. \t " + clazz;
                break;
            case 2:
                string += "Map. \t " + clazz;
                break;
            case 3:
                string += "Sub level class. \t " + clazz + ": \n";
                if (subLevelDataTypes != null) {
                    for (Field field : subLevelDataTypes.keySet()) {
                        string += field.getName() + " -> " + subLevelDataTypes.get(field) + "\n\t\t";
                    }
                }
                break;
        }
        return string + "\n";
    }

}
