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
     * page no
     */
    private int pageNo;
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

    /**
     *  // tool
     *  int totalPages = (int) Math.ceil((double) totalCount/pageSize);
     *  int startIndex = (pageNo-1) * pageSize;
     *  int endIndex = Math.min(startIndex + pageSize, totalCount);
     */

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
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
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", pageData=" + pageData +
                ", totalCount=" + totalCount +
                '}';
    }

}
