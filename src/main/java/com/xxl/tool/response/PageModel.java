package com.xxl.tool.response;

import java.io.Serializable;
import java.util.List;

/**
 * page model
 *
 * @author xuxueli
 */
public class PageModel<T>  implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * page offset
     */
    private int offset;
    /**
     * page size
     */
    private int pagesize;
    /**
     * page data
     */
    private List<T> data;
    /**
     * total records
     */
    private int total;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "PageModel{" +
                "offset=" + offset +
                ", pagesize=" + pagesize +
                ", data=" + data +
                ", total=" + total +
                '}';
    }

}
