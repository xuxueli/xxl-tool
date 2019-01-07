package com.xuxueli.poi.excel.util;

import java.lang.reflect.Field;

/**
 * @author liuyanjun
 */
public class FieldForSort {

    private Field field;
    private int index;

    /**
     * @param field
     */
    public FieldForSort(Field field) {
        super();
        this.field = field;
    }

    /**
     * @param field
     * @param index
     */
    public FieldForSort(Field field, int index) {
        super();
        this.field = field;
        this.index = index;
    }

    /**
     * @return the field
     */
    public Field getField() {
        return field;
    }

    /**
     * @param field
     *        the field to set
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index
     *        the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
