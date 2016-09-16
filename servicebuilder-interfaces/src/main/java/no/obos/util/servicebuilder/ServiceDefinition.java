package no.obos.util.servicebuilder;

import java.util.List;

public interface ServiceDefinition {
    String getName();

    String getDescription();

    List<Class> getResources();

    default JsonConfig getJsonConfig() {
        return JsonConfig.standard;
    }
}
