package com.xxl.tool.response;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据模型
 *
 * <p>字段语义：{@link #getOffset()} 为分页偏移量（页码或起始位置由调用方约定），
 * {@link #getPagesize()} 为每页条数，{@link #getData()} 为当前页数据（无数据时可为 {@code null}），
 * {@link #getTotal()} 为总记录数。本类只是数据载体，不计算总页数，也不校验参数。</p>
 *
 * <p>可变 POJO，<b>非线程安全</b>；可序列化，{@code serialVersionUID = 42L}。</p>
 *
 * @author xuxueli
 */
public class PageModel<T>  implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * 分页偏移量（页码或起始位置，由调用方约定）
     */
    private int offset;
    /**
     * 每页条数，本类不校验取值范围
     */
    private int pagesize;
    /**
     * 当前页数据，可为 {@code null}
     */
    private List<T> data;
    /**
     * 总记录数
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
