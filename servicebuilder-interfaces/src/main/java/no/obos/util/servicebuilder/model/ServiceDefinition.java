package no.obos.util.servicebuilder.model;

import java.util.List;

public interface ServiceDefinition {

    String getName();

    List<Class> getResources();

    default JsonConfig getJsonConfig() {
        return JsonConfig.standard;
    }

    default Boolean isSpringboot() {
        return false;
    }

}
