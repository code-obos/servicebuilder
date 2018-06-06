package no.obos.util.servicebuilder.config;

import lombok.AllArgsConstructor;
import no.obos.util.config.AppConfig;
import no.obos.util.config.AppConfigLoader;

import static no.obos.util.servicebuilder.model.Constants.APPCONFIG_KEY;
import static no.obos.util.servicebuilder.model.Constants.CONFIG_KEY_SERVICE_VERSION;
import static no.obos.util.servicebuilder.model.Constants.CONFIG_KEY_SERVICE_VERSION_MAJOR;
import static no.obos.util.servicebuilder.model.Constants.CONFIG_KEY_SERVICE_VERSION_MINOR;
import static no.obos.util.servicebuilder.util.ApiVersionUtil.getApiVersion;
import static no.obos.util.servicebuilder.util.ApiVersionUtil.getMajorVersion;
import static no.obos.util.servicebuilder.util.ApiVersionUtil.getMinorVersion;

@AllArgsConstructor
public class AppConfigBackedPropertyProvider extends RecursiveExpansionPropertyProvider {
    private final AppConfig appConfig;

    @Override
    public String getNoExpansion(String key) {
        return appConfig.getValueNoExpansion(key);
    }

    @Override
    public void failIfNotPresent(String... keys) {
        appConfig.failIfNotPresent(keys);
    }

    @Override
    public void failIfNotPresent(Iterable<String> keys) {
        appConfig.failIfNotPresent(keys);
    }

    public static AppConfigBackedPropertyProvider fromJvmArgs(Class<?> versionedClass) {
        AppConfig appConfig = new AppConfigLoader().load(APPCONFIG_KEY);

        if (! appConfig.present(CONFIG_KEY_SERVICE_VERSION)) {
            appConfig.put(CONFIG_KEY_SERVICE_VERSION, getApiVersion(versionedClass));
        }
        if (! appConfig.present(CONFIG_KEY_SERVICE_VERSION_MAJOR)) {
            appConfig.put(CONFIG_KEY_SERVICE_VERSION_MAJOR, getMajorVersion(versionedClass));
        }
        if (! appConfig.present(CONFIG_KEY_SERVICE_VERSION_MINOR)) {
            appConfig.put(CONFIG_KEY_SERVICE_VERSION_MINOR, getMinorVersion(versionedClass));
        }

        return new AppConfigBackedPropertyProvider(appConfig);
    }
}
