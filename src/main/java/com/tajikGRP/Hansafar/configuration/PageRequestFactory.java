package com.tajikGRP.Hansafar.configuration;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageRequestFactory {

    private static final int MAX_PAGE_SIZE = 100;

    private PageRequestFactory() {
    }

    public static Pageable create(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Set<String> allowedSortFields
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0."
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field: " + sortBy
            );
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Sort direction must be 'asc' or 'desc'."
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );
    }
}
