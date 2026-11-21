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

    /**
     * 获取分页偏移量
     *
     * @return 分页偏移量（页码或起始位置，由调用方约定）
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 设置分页偏移量
     *
     * @param offset 分页偏移量（页码或起始位置，由调用方约定）
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * 获取每页条数
     *
     * @return 每页条数
     */
    public int getPagesize() {
        return pagesize;
    }

    /**
     * 设置每页条数
     *
     * @param pagesize 每页条数
     */
    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    /**
     * 获取当前页数据
     *
     * @return 当前页数据，可能为 {@code null}
     */
    public List<T> getData() {
        return data;
    }

    /**
     * 设置当前页数据
     *
     * @param data 当前页数据，可为 {@code null}
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 获取总记录数
     *
     * @return 总记录数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 设置总记录数
     *
     * @param total 总记录数
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 返回分页模型字段的调试字符串
     *
     * @return 包含 offset、pagesize、data、total 的字符串
     */
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
