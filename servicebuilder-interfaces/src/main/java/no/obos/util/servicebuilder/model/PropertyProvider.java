package no.obos.util.servicebuilder.model;

public interface PropertyProvider {
    String get(String key);

    default String getOrDefault(String key, String defaultValue) {
        try {
            return get(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    default String getOrNull(String key) {
        return getOrDefault(key, null);
    }

    void failIfNotPresent(String... keys);

    void failIfNotPresent(Iterable<String> keys);
}
