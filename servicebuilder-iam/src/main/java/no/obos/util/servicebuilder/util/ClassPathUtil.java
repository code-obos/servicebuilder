package no.obos.util.servicebuilder.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class ClassPathUtil {

    public static Set<ClassInfo> getTopLevelClassesRecursive(String packageName) throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClassesRecursive(packageName);
    }

    public static <T extends Annotation> Set<T> findDeclaredAnnotations(Class<?> clazz, Class<T> annotationClass) {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();

        T declaredAnnotation = clazz.getDeclaredAnnotation(annotationClass);

        if (declaredAnnotation != null) {
            builder.add(declaredAnnotation);
        }

        builder.addAll(getDeclaredAnnotationsIgnoringNoClassDefFoundErrors(clazz::getDeclaredConstructors, annotationClass));
        builder.addAll(getDeclaredAnnotationsIgnoringNoClassDefFoundErrors(clazz::getDeclaredFields, annotationClass));

        Method[] declaredMethods = getDeclaredMethodsIgnoringNoClassDefFoundErrors(clazz);
        builder.addAll(getDeclaredAnnotationsIgnoringNoClassDefFoundErrors(() -> declaredMethods, annotationClass));
        builder.addAll(getParameterAnnotationsIgnoringNoClassDefFoundErrors(() -> declaredMethods, annotationClass));

        builder.addAll(
                Arrays.stream(getDeclaredClassesIgnoringNoClassDefFoundErrors(clazz))
                        .map(klass -> findDeclaredAnnotations(klass, annotationClass))
                        .flatMap(Collection::stream)
                        .iterator()
        );

        return builder.build();
    }

    private static <T extends Annotation> Set<T> getDeclaredAnnotationsIgnoringNoClassDefFoundErrors(
            Supplier<AnnotatedElement[]> annotatedElementsSupplier,
            Class<T> annotationClass)
    {
        try {
            return getDeclaredAnnotations(annotatedElementsSupplier.get(), annotationClass);
        } catch (NoClassDefFoundError error) {
            return emptySet();
        }
    }

    private static <T extends Annotation> Set<T> getDeclaredAnnotations(
            AnnotatedElement[] annotatedElements,
            Class<T> annotationClass)
    {
        return Arrays.stream(annotatedElements)
                .map(method -> method.getDeclaredAnnotation(annotationClass))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private static <T extends Annotation> Set<T> getParameterAnnotationsIgnoringNoClassDefFoundErrors(
            Supplier<Method[]> methodsSupplier,
            Class<T> annotationClass)
    {
        try {
            return getParameterAnnotations(methodsSupplier.get(), annotationClass);
        } catch (NoClassDefFoundError error) {
            return emptySet();
        }
    }

    private static <T extends Annotation> Set<T> getParameterAnnotations(
            Method[] methods,
            Class<T> annotationClass)
    {
        return Arrays.stream(methods)
                .map(Method::getParameterAnnotations)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(annotation -> annotation.annotationType().equals(annotationClass))
                .map(annotationClass::cast)
                .collect(toSet());
    }

    private static Method[] getDeclaredMethodsIgnoringNoClassDefFoundErrors(Class<?> clazz) {
        try {
            return clazz.getDeclaredMethods();
        } catch (NoClassDefFoundError error) {
            // Typisk et problem ved lasting av f.eks. no.obos.util.servicebuilder.Addons
            return new Method[0];
        }
    }

    private static Class<?>[] getDeclaredClassesIgnoringNoClassDefFoundErrors(Class<?> clazz) {
        try {
            return clazz.getDeclaredClasses();
        } catch (NoClassDefFoundError error) {
            return new Class[0];
        }
    }
}
