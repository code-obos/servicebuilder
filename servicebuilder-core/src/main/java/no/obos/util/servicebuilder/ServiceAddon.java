package no.obos.util.servicebuilder;

public interface ServiceAddon {
    void addToJerseyConfig(JerseyConfig jerseyConfig);

    void addToJettyServer(JettyServer jettyServer);
}
