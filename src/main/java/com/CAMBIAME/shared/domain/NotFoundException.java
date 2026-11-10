package com.CAMBIAME.shared.domain;

/** Base de las excepciones que indican que un agregado no existe. */
public abstract class NotFoundException extends DomainException {

  protected NotFoundException(final String message) {
    super(message);
  }
}
