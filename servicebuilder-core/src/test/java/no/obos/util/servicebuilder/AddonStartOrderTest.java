package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AddonStartOrderTest {

    @Test
    public void addons_are_started_in_config_order_when_no_order_specified() {
        //Given
        final List<Integer> startOrder = Lists.newArrayList();
        ServiceConfig config = ServiceConfig.defaults(ServiceDefinition.simple())
                .addon(new Addon() {
                    @Override
                    public Addon withDependencies(ServiceConfig serviceConfig) {
                        startOrder.add(1);
                        return this;
                    }
                })
                .addon(new Addon() {
                    @Override
                    public Addon withDependencies(ServiceConfig serviceConfig) {
                        startOrder.add(2);
                        return this;
                    }
                });

        //When
        ServiceConfigInitializer.addContext(config);

        assertThat(startOrder).isEqualTo(Lists.newArrayList(1, 2));
    }

    @Test
    public void addons_may_specify_startup_order() {
        //Given
        final List<Integer> startOrder = Lists.newArrayList();

        class Dependee implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(1);
                return this;
            }
        }
        class Dependent implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(2);
                return this;
            }

            @Override
            public Set<Class<?>> startAfter() {return ImmutableSet.of(Dependee.class);}
        }

        ServiceConfig config = ServiceConfig.defaults(ServiceDefinition.simple())
                .addon(new Dependent())
                .addon(new Dependee());

        //When
        ServiceConfigInitializer.addContext(config);

        assertThat(startOrder).isEqualTo(Lists.newArrayList(1, 2));
    }

    @Test
    public void startup_order_is_not_transient_so_dont_mess_up() {
        //Given
        final List<Integer> startOrder = Lists.newArrayList();

        class Dependee implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(1);
                return this;
            }
        }
        class Immediate implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(2);
                return this;
            }

            @Override
            public Set<Class<?>> startAfter() {return ImmutableSet.of(Dependee.class);}
        }

        class Dependent implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(3);
                return this;
            }

            @Override
            public Set<Class<?>> startAfter() {return ImmutableSet.of(Immediate.class);}
        }

        ServiceConfig config = ServiceConfig.defaults(ServiceDefinition.simple())
                .addon(new Immediate())
                .addon(new Dependent())
                .addon(new Dependee())
                ;

        //When
        ServiceConfigInitializer.addContext(config);

        assertThat(startOrder).isEqualTo(Lists.newArrayList(2, 3, 1));
    }

    @Test
    public void if_dependencies_have_several_layers_use_multiple_dependencies() {
        //Given
        final List<Integer> startOrder = Lists.newArrayList();

        class Dependee implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(1);
                return this;
            }
        }
        class Immediate implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(2);
                return this;
            }

            @Override
            public Set<Class<?>> startAfter() {return ImmutableSet.of(Dependee.class);}
        }

        class Dependent implements Addon {
            @Override
            public Addon withDependencies(ServiceConfig serviceConfig) {
                startOrder.add(3);
                return this;
            }

            @Override
            public Set<Class<?>> startAfter() {return ImmutableSet.of(Immediate.class, Dependee.class);}
        }

        ServiceConfig config = ServiceConfig.defaults(ServiceDefinition.simple())
                .addon(new Dependent())
                .addon(new Dependee())
                .addon(new Immediate());

        //When
        ServiceConfigInitializer.addContext(config);

        assertThat(startOrder).isEqualTo(Lists.newArrayList(1, 2, 3));
    }
}

