package no.obos.util.servicebuilder.model;

public interface ServiceDefinition {

    String getName();

    Version getVersion();

    Iterable<Class> getResources();

    default SerializationSpec getSerializationSpec() {
        return SerializationSpec.standard;
    }

}
