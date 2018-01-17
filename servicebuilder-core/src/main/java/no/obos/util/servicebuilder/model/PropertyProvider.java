package no.obos.util.servicebuilder.model;

public interface PropertyProvider {

    /**
     * Retreives property for given string or null if missing or empty
     */
    String get(String key);


    void failIfNotPresent(String... keys);

    void failIfNotPresent(Iterable<String> keys);

    /**
     * Retreives property for given string, returning fallback on missing or empty. Fallback may be null.
     */
    String getWithFallback(String key, String fallback);

    /**
     * Retreives property for given string, returning fallback on missing or empty.
     *
     * @Throws RuntimeException if property for key null or empty AND fallbak null or empty
     */
    String requireWithFallback(String key, String fallback);
}
