package com.oceansky.teacher.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * User: dengfa
 * Date: 16/9/2
 * Tel:  18500234565
 */
public class MyHashSet<T> extends LinkedHashSet<T> {

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(size() * 16);
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next != this) {
                buffer.append(next);
            } else {
                buffer.append("(this Collection)");
            }
            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }
}
