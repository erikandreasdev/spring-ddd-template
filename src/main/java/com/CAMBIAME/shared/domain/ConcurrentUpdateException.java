package com.CAMBIAME.shared.domain;

/** Otra petición modificó el agregado después de leerlo (bloqueo optimista). */
public final class ConcurrentUpdateException extends DomainException {

  public ConcurrentUpdateException(final String aggregateName, final long id) {
    super(aggregateName + " " + id + " was modified by another request");
  }
}
