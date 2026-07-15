package com.zhixun.common.api;

import java.util.List;

public record PageResult<T>(
        List<T> records,
        long total,
        long page,
        long size,
        long pages
) {

    public PageResult {
        records = records == null ? List.of() : List.copyOf(records);
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be at least 1");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        if (pages < 0) {
            throw new IllegalArgumentException("pages must not be negative");
        }
    }

    public static <T> PageResult<T> of(List<T> records, long total, long page, long size) {
        long pages = total == 0 ? 0 : total / size + (total % size == 0 ? 0 : 1);
        return new PageResult<>(records, total, page, size, pages);
    }
}
