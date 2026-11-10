package com.CHANGEME.shared.application;

import java.util.List;
import java.util.Objects;

// Paginated result of a read port. Create it in shared.application with the first listing.
public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

  public PageResult {
    items = List.copyOf(Objects.requireNonNull(items));
    if (page < 0) throw new IllegalArgumentException("Page must not be negative: " + page);
    if (size < 1) throw new IllegalArgumentException("Size must be positive: " + size);
    if (totalItems < 0) throw new IllegalArgumentException("Total must not be negative");
  }
}
