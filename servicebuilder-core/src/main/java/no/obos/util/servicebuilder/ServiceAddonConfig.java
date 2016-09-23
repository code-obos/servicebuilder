package no.obos.util.servicebuilder;

public interface ServiceAddonConfig<Addon extends ServiceAddon> {
    void addProperties(PropertyProvider properties);

    void addContext(ServiceBuilder serviceBuilder);

    Addon init();
}
