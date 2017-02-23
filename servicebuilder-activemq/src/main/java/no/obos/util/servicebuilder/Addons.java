package no.obos.util.servicebuilder;

public class Addons {

    public static H2InMemoryDatasourceAddon basicDatasourceAddon() {
        return H2InMemoryDatasourceAddon.defaults;
    }

    public static SwaggerAddon swaggerAddon() {
        return SwaggerAddon.defaults;
    }
}
