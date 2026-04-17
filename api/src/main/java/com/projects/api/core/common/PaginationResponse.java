package com.projects.api.core.common;

import java.util.Collections;
import java.util.List;

public class PaginationResponse<T> {
    private List<T> items = Collections.emptyList();
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;

    public PaginationResponse() {
    }

    public PaginationResponse(List<T> items, int page, int size, long totalItems, int totalPages, boolean hasNext) {
        this.items = items == null ? Collections.emptyList() : items;
        this.page = Math.max(page, 0);
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public static <T> PaginationResponse<T> of(List<T> items, int page, int size) {
        int safeSize = Math.max(size, 1);
        long total = items == null ? 0L : items.size();
        int totalPages = (total == 0) ? 0 : (int) Math.ceil(total / (double) safeSize);
        boolean hasNext = total > safeSize;
        return new PaginationResponse<>(items, page, size, total, totalPages, hasNext);
    }
}
