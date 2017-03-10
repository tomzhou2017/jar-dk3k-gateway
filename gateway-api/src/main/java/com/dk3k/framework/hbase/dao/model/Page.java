package com.dk3k.framework.hbase.dao.model;

import java.util.List;

/**
 * Created by lilin on 2017/2/13.
 */
public class Page<T> {

    // 返回的结果集list
    private List<T> list;

    // 下一页查询需要传入的值
    private String nextIndex;

    // 查询记录数
    private int pageSize;

    public Page(int pageSize) {
        if (pageSize <= 0)
            pageSize = 20;
        if (pageSize > 1000)
            pageSize = 1000;

        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public String getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(String nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
