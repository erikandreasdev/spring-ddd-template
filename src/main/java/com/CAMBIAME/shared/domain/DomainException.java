package com.CAMBIAME.shared.domain;

/** Base de las excepciones que representan una regla de negocio incumplida. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }
}
