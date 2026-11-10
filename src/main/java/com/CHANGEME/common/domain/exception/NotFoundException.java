package com.CHANGEME.common.domain.exception;

/** Base of the exceptions that indicate that an aggregate does not exist. */
public abstract class NotFoundException extends DomainException {

  protected NotFoundException(final String message) {
    super(message);
  }
}
