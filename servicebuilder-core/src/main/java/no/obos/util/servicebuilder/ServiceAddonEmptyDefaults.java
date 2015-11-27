package no.obos.util.servicebuilder;

import no.obos.util.config.AppConfig;

public abstract class ServiceAddonEmptyDefaults implements ServiceAddon {
    public static <T> void configFromAppConfig(AppConfig appConfig, T configBuilder) {
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
