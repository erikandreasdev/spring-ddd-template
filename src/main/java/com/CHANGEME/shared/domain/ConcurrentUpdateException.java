package com.CHANGEME.shared.domain;

/** Another request modified the aggregate after it was read (optimistic locking). */
public final class ConcurrentUpdateException extends DomainException {

  public ConcurrentUpdateException(final String aggregateName, final long id) {
    super(aggregateName + " " + id + " was modified by another request");
  }
}
