package no.obos.util.servicebuilder.util;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassPathUtilTest {

    @Test
    public void findDeclaredAnnotations() {
        assertThat(ClassPathUtil.findDeclaredAnnotations(IAmAnnotated.class, DummyAnnotation.class))
                .containsExactlyInAnyOrder(
                        createDummyAnnotation(1, 3),
                        createDummyAnnotation(2, 4),
                        createDummyAnnotation(4, 6),
                        createDummyAnnotation(3, 5),
                        createDummyAnnotation(false, 4, 6),
                        createDummyAnnotation(11),
                        createDummyAnnotation(22),
                        createDummyAnnotation(33)
                );
    }

    private DummyAnnotation createDummyAnnotation(int... appIds) {
        return createDummyAnnotation(true, appIds);
    }

    private DummyAnnotation createDummyAnnotation(boolean bool, int... values) {
        return new DummyAnnotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation.class;
            }

            @Override
            public int[] value() {
                return values;
            }

            @Override
            public boolean bool() {
                return bool;
            }
        };
    }

    @SuppressWarnings("unused")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({
            ElementType.METHOD,
            ElementType.TYPE,
            ElementType.CONSTRUCTOR,
            ElementType.FIELD,
            ElementType.PARAMETER
    })
    @Inherited
    public @interface DummyAnnotation {

        int[] value();

        boolean bool() default true;
    }


    @SuppressWarnings("unused")
    @DummyAnnotation({1, 3})
    private class IAmAnnotated {

        @DummyAnnotation({11})
        public IAmAnnotated() {
        }

        @DummyAnnotation({22})
        private String metoo;

        @DummyAnnotation({2, 4})
        String fooBar(@DummyAnnotation({33}) String barFoo) {
            return barFoo.toLowerCase();
        }

        @DummyAnnotation({4, 6})
        Integer barFoo(String barFoo) {
            return Integer.valueOf(barFoo);
        }

        @DummyAnnotation({3, 5})
        private class IAmAlsoAnnotated {

            @DummyAnnotation({4, 6})
            void fooBar2() {
            }

            @DummyAnnotation(value = {4, 6}, bool = false)
            void fooBar3() {
            }
        }
    }

}
