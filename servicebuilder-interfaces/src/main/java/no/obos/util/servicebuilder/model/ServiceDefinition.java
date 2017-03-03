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

    static ServiceDefinition simple(final String name, final String version, final Class... resources) {
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

    static ServiceDefinition simple(final Class... resources) {
        return new ServiceDefinition() {
            ImmutableList<Class> classes = ImmutableList.copyOf(resources);

            @Override
            public String getName() {
                return ANONYMOUS_SERVICE_NAME;
            }

            @Override
            public String getVersion() {
                return "1.0";
            }

            @Override
            public List<Class> getResources() {
                return classes;
            }
        };
    }
}
