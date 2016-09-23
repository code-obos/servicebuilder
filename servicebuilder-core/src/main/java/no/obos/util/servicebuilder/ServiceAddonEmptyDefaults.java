package no.obos.util.servicebuilder;

public abstract class ServiceAddonEmptyDefaults implements ServiceAddon {
    public static <T> void configFromProperties(PropertyProvider properties, T configBuilder) {
    }

    public static <T> void configFromContext(ServiceBuilder serviceBuilder, T configBuilder) {
    }


    @Override
    public void addToJettyServer(JettyServer jettyServer) {
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
    }
}
