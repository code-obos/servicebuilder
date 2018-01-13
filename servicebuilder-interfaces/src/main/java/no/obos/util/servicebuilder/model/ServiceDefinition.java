package no.obos.util.servicebuilder.model;

import java.util.List;

public interface ServiceDefinition {

    String getName();

    Version getVersion();

    List<Class> getResources();

    default JsonConfig getJsonConfig() {
        return JsonConfig.standard;
    }

}
