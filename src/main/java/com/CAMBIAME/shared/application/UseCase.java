package com.CAMBIAME.shared.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una clase como caso de uso. Es Java puro: la capa de aplicación no importa Spring.
 * UseCaseConfig registra estas clases como beans y hace transaccionales sus métodos públicos.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCase {}
