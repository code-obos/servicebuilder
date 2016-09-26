package no.obos.util.servicebuilder;

import java.util.List;

public interface ServiceDefinition {
    String getName();

    List<Class> getResources();

    default JsonConfig getJsonConfig() {
        return JsonConfig.standard;
    }
}
