package com.ibm.infosvr.restclient.model;

import java.lang.reflect.Field;

public abstract class ObjectPrinter {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(": { ");
        try {
            for (Field f : getClass().getFields()) {
                sb.append(f.getName());
                sb.append("=");
                sb.append(f.get(this));
                sb.append(", ");
            }
            // Get rid of the extra comma left at the end
            sb.deleteCharAt(sb.length() - 2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        sb.append("}");
        return sb.toString();
    }

}
