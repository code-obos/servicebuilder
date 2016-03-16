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
                .newAddon(SwaggerAddon.configure(cfg -> cfg
                        .apiBasePath("quite some basepath")
                        .apiVersion("quite some version")
                ));
        serviceBuilderWithDefaults.with(basicDatasourceAddon);
        //or
        serviceBuilderWithDefaults
                .with(SwaggerAddon.configure(cfg -> cfg
                        .apiBasePath("quite some basepath")
                        .apiVersion("quite some version")
                ));
    }


    @Test
    public void addons_expose_config() {
        String parameterFromConfig = serviceBuilderWithDefaults.getAppConfig().get(SwaggerAddon.CONFIG_KEY_API_BASEURL);
        SwaggerAddon basicDatasourceAddon = serviceBuilderWithDefaults
                .newAddon(SwaggerAddon.defaults());
        assertEquals(parameterFromConfig, basicDatasourceAddon.configuration.apiBasePath);
    }

    @Test
    public void addons_should_expose_default_configuration_options() {
        SwaggerAddon.Configuration configuration = SwaggerAddon.defaultConfiguration().build();
        assertEquals(SwaggerAddon.DEFAULT_PATH_SPEC, configuration.pathSpec);
    }


    @Test
    public void addons_may_retreive_config_from_properties() {
        SwaggerAddon.Configuration.ConfigurationBuilder addonConfigurationBuilder = SwaggerAddon.defaultConfiguration();

        SwaggerAddon.configFromAppConfig(serviceBuilderWithDefaults.getAppConfig(), addonConfigurationBuilder);
    }

    @Test
    public void addons_may_retreive_config_environment_for_example_server_ports() {
        SwaggerAddon.Configuration.ConfigurationBuilder addonConfigurationBuilder = SwaggerAddon.defaultConfiguration();

        SwaggerAddon.configFromContext(serviceBuilderWithDefaults, addonConfigurationBuilder);
    }

    @Test
    public void standard_configuration_of_addon_uses_defaults_then_appconfig_and_environment_then_applies_user_options() {
        String parameterFromUser = "quite some basepath";
        String parameterFromConfig = serviceBuilderWithDefaults.getAppConfig().get(SwaggerAddon.CONFIG_KEY_API_BASEURL);
        SwaggerAddon basicDatasourceAddon = serviceBuilderWithDefaults
                .newAddon(SwaggerAddon.configure(cfg -> cfg
                        .apiBasePath(parameterFromUser)
                ));

        assertEquals(parameterFromUser, basicDatasourceAddon.configuration.apiBasePath);
    }


    @Test
    public void addons_may_be_manually_configured_but_should_only_be_for_test_use() {
        SwaggerAddon.Configuration.ConfigurationBuilder addonConfigurationBuilder = SwaggerAddon.defaultConfiguration();
        SwaggerAddon.Configuration configuration = addonConfigurationBuilder
                .apiVersion("quite some version")
                .build();
        SwaggerAddon addon = new SwaggerAddon(configuration);
        serviceBuilderWithDefaults.with(addon);
    }



    @Before
    public void init() {
        serviceBuilderWithDefaults = ServiceBuilder
                .defaults(AddonDocumentationTest.class)
                .configJersey(JerseyConfig.defaults())
                .configJettyServer(JettyServer.defaults());
    }

}
