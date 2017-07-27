package no.obos.util.servicebuilder.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface ServiceDefinition {

    String getName();

    List<Class> getResources();

    default List<MessageDescription<?>> getHandledMessages() {
        return ImmutableList.of();
    }

    default JsonConfig getJsonConfig() {
        return JsonConfig.standard;
    }


}
