package no.obos.util.servicebuilder.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface AppIdWhiteList {

    /**
     * Array av appIds for applikasjoner som godtas av endepunktet.
     */
    int[] value();

    /**
     * Dersom {@link #exclusive()} er true (default) vil annotasjonen ekskludere alle appIds som ikke
     * finnes i {@link #value()}, ellers vil alle på annen måte autoriserte appIds også godtas.
     */
    boolean exclusive() default true;
}
