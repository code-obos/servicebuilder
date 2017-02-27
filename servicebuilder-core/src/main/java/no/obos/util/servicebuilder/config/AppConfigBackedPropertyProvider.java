package no.obos.util.servicebuilder.config;

import lombok.AllArgsConstructor;
import no.obos.util.config.AppConfig;
import no.obos.util.config.AppConfigLoader;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.version.Version;
import no.obos.util.version.VersionUtil;

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

    public static AppConfigBackedPropertyProvider fromJvmArgs(ServiceDefinition serviceDefinition) {
        AppConfig appConfig = new AppConfigLoader().load(Constants.APPCONFIG_KEY);
        if (! appConfig.present(Constants.CONFIG_KEY_SERVICE_VERSION)) {
            setServiceVersionProgrammatically(serviceDefinition.getClass(), appConfig);
        }
        return new AppConfigBackedPropertyProvider(appConfig);
    }

    private static void setServiceVersionProgrammatically(Class classOnLocalClassPath, AppConfig appConfig) {
        final Version version = new VersionUtil(classOnLocalClassPath).getVersion();
        appConfig.put(Constants.CONFIG_KEY_SERVICE_VERSION, version == null ? "" : version.getMajor() + "." + version.getMinor());
    }
}
