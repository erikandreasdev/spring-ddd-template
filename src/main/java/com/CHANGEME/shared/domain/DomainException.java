package com.CHANGEME.shared.domain;

/** Base of the exceptions that represent a broken business rule. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }
}
