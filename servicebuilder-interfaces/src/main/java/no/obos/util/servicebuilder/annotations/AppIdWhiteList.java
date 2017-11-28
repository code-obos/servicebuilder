package no.obos.util.servicebuilder.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface AppIdWhiteList {
    int[] value();
}
