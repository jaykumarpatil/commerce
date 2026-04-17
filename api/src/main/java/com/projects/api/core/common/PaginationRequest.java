package com.projects.api.core.common;

public class PaginationRequest {
    private int page = 0;
    private int size = 20;

    public PaginationRequest() {
    }

    public PaginationRequest(int page, int size) {
        this.page = Math.max(page, 0);
        this.size = Math.max(size, 1);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static PaginationRequest of(int page, int size) {
        return new PaginationRequest(page, size);
    }
}
