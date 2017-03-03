package no.obos.util.servicebuilder.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface ServiceDefinition {

    String ANONYMOUS_SERVICE_NAME = "anonymous_service";

    String getName();

    List<Class> getResources();

    String getVersion();

    default JsonConfig getJsonConfig() {
        return JsonConfig.standard;
    }

    static ServiceDefinition simple(final String name, final String version, final Iterable<Class> resources) {
        return new ServiceDefinition() {
            ImmutableList<Class> classes = ImmutableList.copyOf(resources);

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public List<Class> getResources() {
                return classes;
            }
        };
    }

    static ServiceDefinition simple(final String name, final String version, final Class... resources) {
        ImmutableList<Class> classes = ImmutableList.copyOf(resources);
        return simple(name, version, classes);
    }


    static ServiceDefinition simple(final Class... resources) {
        return simple(ANONYMOUS_SERVICE_NAME, "1.0", resources);
    }
}
