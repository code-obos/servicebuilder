package no.obos.util.servicebuilder.config;

import lombok.AllArgsConstructor;
import no.obos.util.config.AppConfig;
import no.obos.util.config.AppConfigLoader;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.util.ApiVersionUtil;
import no.obos.util.version.Version;
import no.obos.util.version.VersionUtil;

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
        AppConfig appConfig = new AppConfigLoader().load(Constants.APPCONFIG_KEY);
        String apiVersion = ApiVersionUtil.getApiVersion(versionedClass);
        if (! appConfig.present(Constants.CONFIG_KEY_SERVICE_VERSION)) {
            appConfig.put(Constants.CONFIG_KEY_SERVICE_VERSION, apiVersion);
        }

        return new AppConfigBackedPropertyProvider(appConfig);
    }

    private static Version readBuildVersion(Class classOnLocalClassPath) {
        return new VersionUtil(classOnLocalClassPath).getVersion();
    }
}
