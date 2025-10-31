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
    private int pageSize;
    /**
     * page data
     */
    private List<T> pageData;
    /**
     * total records
     */
    private int totalCount;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getPageData() {
        return pageData;
    }

    public void setPageData(List<T> pageData) {
        this.pageData = pageData;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "PageModel{" +
                "offset=" + offset +
                ", pageSize=" + pageSize +
                ", pageData=" + pageData +
                ", totalCount=" + totalCount +
                '}';
    }

}
