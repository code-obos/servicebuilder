package no.obos.util.servicebuilder.model;

public interface PropertyProvider {
    String get(String key);

    void failIfNotPresent(String... keys);

    void failIfNotPresent(Iterable<String> keys);
}
