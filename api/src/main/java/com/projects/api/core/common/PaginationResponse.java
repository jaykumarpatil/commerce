package com.projects.api.core.common;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private List<T> items = Collections.emptyList();
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;

    public static <T> PaginationResponse<T> of(List<T> items, int page, int size) {
        int safeSize = Math.max(size, 1);
        long total = items == null ? 0L : items.size();
        int pages = (int) Math.ceil(total / (double) safeSize);
        return new PaginationResponse<>(
            items == null ? Collections.emptyList() : items,
            Math.max(page, 0),
            safeSize,
            total,
            pages,
            false
        );
    }
}
