package no.obos.util.servicebuilder;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AddonDocumentationTest {

    static ServiceBuilder serviceBuilderWithDefaults;

    public AddonDocumentationTest() {
        System.setProperty(ServiceBuilder.APPCONFIG_KEY, "src/test/resources/service-junit.properties");
    }

    @Test
    public void addons_provide_configuration_environments_for_standard_functionality_including_jersey_and_jetty_setup() {
        serviceBuilderWithDefaults
                .with(SwaggerAddon.defaults());
    }

    @Test
    public void standard_way_of_adding_addon() {
        SwaggerAddon basicDatasourceAddon = serviceBuilderWithDefaults
                .newAddon(SwaggerAddon.defaults());
        serviceBuilderWithDefaults.with(basicDatasourceAddon);
        //or
        serviceBuilderWithDefaults.with(SwaggerAddon.defaults());
    }


    @Test
    public void standard_way_of_adding_addon_with_configuration() {
        SwaggerAddon basicDatasourceAddon = serviceBuilderWithDefaults
                .newAddon(SwaggerAddon.config(cfg -> cfg
                        .apiBasePath("quite some basepath")
                        .apiVersion("quite some version")
                ));
        serviceBuilderWithDefaults.with(basicDatasourceAddon);
        //or
        serviceBuilderWithDefaults
                .with(SwaggerAddon.config(cfg -> cfg
                        .apiBasePath("quite some basepath")
                        .apiVersion("quite some version")
                ));
    }


    @Test
    public void addons_expose_config() {
        String parameterFromConfig = serviceBuilderWithDefaults.getAppConfig().get(SwaggerAddon.CONFIG_KEY_API_BASEURL);
        SwaggerAddon basicDatasourceAddon = serviceBuilderWithDefaults
                .newAddon(SwaggerAddon.defaults());
        assertEquals(parameterFromConfig, basicDatasourceAddon.config.apiBasePath);
    }

    @Test
    public void addons_should_expose_default_configuration_options() {
        SwaggerAddon.Config config = SwaggerAddon.defaultConfig().build();
        assertEquals(SwaggerAddon.DEFAULT_API_VERSION, config.apiVersion);
        assertEquals(SwaggerAddon.DEFAULT_PATH_SPEC, config.pathSpec);
    }


    @Test
    public void addons_may_retreive_config_from_properties() {
        SwaggerAddon.Config.ConfigBuilder addonConfigBuilder = SwaggerAddon.defaultConfig();

        SwaggerAddon.configFromAppConfig(serviceBuilderWithDefaults.getAppConfig(), addonConfigBuilder);
    }

    @Test
    public void addons_may_retreive_config_environment_for_example_server_ports() {
        SwaggerAddon.Config.ConfigBuilder addonConfigBuilder = SwaggerAddon.defaultConfig();

        SwaggerAddon.configFromContext(serviceBuilderWithDefaults, addonConfigBuilder);
    }

    @Test
    public void standard_configuration_of_addon_uses_defaults_then_appconfig_and_environment_then_applies_user_options() {
        String parameterFromUser = "quite some basepath";
        String parameterFromConfig = serviceBuilderWithDefaults.getAppConfig().get(SwaggerAddon.CONFIG_KEY_API_BASEURL);
        SwaggerAddon basicDatasourceAddon = serviceBuilderWithDefaults
                .newAddon(SwaggerAddon.config(cfg -> cfg
                        .apiBasePath(parameterFromUser)
                ));

        assertEquals(parameterFromUser, basicDatasourceAddon.config.apiBasePath);
    }


    @Test
    public void addons_may_be_manually_configured_but_should_only_be_for_test_use() {
        SwaggerAddon.Config.ConfigBuilder addonConfigBuilder = SwaggerAddon.defaultConfig();
        SwaggerAddon.Config config = addonConfigBuilder
                .apiVersion("quite some version")
                .build();
        SwaggerAddon addon = new SwaggerAddon(config);
        serviceBuilderWithDefaults.with(addon);
    }



    @Before
    public void init() {
        serviceBuilderWithDefaults = ServiceBuilder
                .defaults()
                .configJersey(JerseyConfig.defaults())
                .configJettyServer(JettyServer.defaults());
    }

}
