package no.obos.util.servicebuilder.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {

    public static <T extends Annotation> T getAnnotation(Class<T> annotation, Method method) {
        T methodAnnotation = method.getAnnotation(annotation);
        T classAnnotation = method.getDeclaringClass().getAnnotation(annotation);
        if (methodAnnotation != null) {
            return methodAnnotation;
        } else if (classAnnotation != null) {
            return classAnnotation;
        } else {
            return null;
        }
    }
}
