package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import no.obos.util.config.AppConfig;

@AllArgsConstructor
public class AppConfigBackedPropertyProvider implements PropertyProvider {
    private final AppConfig appConfig;

    public String get(String key) {
        return appConfig.get(key);
    }

    @Override
    public void failIfNotPresent(String... keys) {
        appConfig.failIfNotPresent(keys);
    }

    @Override
    public void failIfNotPresent(Iterable<String> keys) {
        appConfig.failIfNotPresent(keys);
    }

}
