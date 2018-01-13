package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.model.Version;


public class ServiceDefinitionUtil {
    public static final String ANONYMOUS_SERVICE_NAME = "anonymous_service";

    public static ServiceDefinition simple(final String name, final Class... resources) {
        ImmutableList<Class> classes = ImmutableList.copyOf(resources);
        return new TestServiceDefinition(name, classes);
    }

    public static ServiceDefinition simple(final Class... resources) {
        ImmutableList<Class> classes = ImmutableList.copyOf(resources);
        return new TestServiceDefinition(classes);
    }

    @AllArgsConstructor
    public static class TestServiceDefinition implements ServiceDefinition {
        @Getter
        final String name;
        @Getter
        final ImmutableList<Class> resources;
        @Getter
        final Version version = new Version(1, 0, 0);

        TestServiceDefinition(ImmutableList<Class> resources) {

            this.name = ANONYMOUS_SERVICE_NAME;
            this.resources = resources;
        }

    }

}
