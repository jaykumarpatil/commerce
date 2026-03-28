package com.projects.api.core.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    private int page = 0;
    private int size = 20;

    public static PaginationRequest of(int page, int size) {
        return new PaginationRequest(Math.max(page, 0), Math.max(size, 1));
    }
}
