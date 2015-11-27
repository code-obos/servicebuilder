package no.obos.util.servicebuilder;

import no.obos.util.config.AppConfig;


public interface ServiceAddonConfig<Addon extends ServiceAddon> {
    void addAppConfig(AppConfig appConfig);

    void addContext(ServiceBuilder serviceBuilder);

    Addon init();
}
