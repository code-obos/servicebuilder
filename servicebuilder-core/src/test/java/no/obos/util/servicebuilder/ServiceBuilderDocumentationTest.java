package no.obos.util.servicebuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceBuilderDocumentationTest {

    static ServiceBuilder serviceBuilderWithDefaults;

    public ServiceBuilderDocumentationTest() {
        System.setProperty(ServiceBuilder.APPCONFIG_KEY, "src/test/resources/service-junit.properties");
    }

    @Test
    public void service_builder_starts_a_jetty_server_with_jersey() {
        System.setProperty(ServiceBuilder.APPCONFIG_KEY, "src/test/resources/service-junit.properties");
        ServiceBuilder serviceBuilder = ServiceBuilder
                .defaults()
                .configJersey(JerseyConfig.defaults())
                .configJettyServer(JettyServer.defaults());

        serviceBuilder.start();
        serviceBuilder.stop();
    }

    @Test
    public void customization_is_done_through_interfaces_and_uses_chaining_pattern() {
        class ServiceBuilderConfigurator implements ServiceBuilder.Configurator {

            @Override public ServiceBuilder.Configuration.ConfigurationBuilder apply(ServiceBuilder.Configuration.ConfigurationBuilder cfg) {
                cfg.appConfigFromJvmArg(false);
                cfg.julLoggingIntegration(true);

                //or

                cfg
                        .appConfigFromJvmArg(false)
                        .julLoggingIntegration(true);


                return cfg;
            }
        }
        ServiceBuilder
                .configure(new ServiceBuilderConfigurator());
    }

    @Test
    public void addons_provide_configuration_environments_for_standard_functionality_including_jersey_and_jetty_setup() {
        serviceBuilderWithDefaults
                .with(SwaggerAddon.defaults());
    }


    @Test
    public void with2_provides_addon_instance_but_disrupts_builder() {
        SwaggerAddon addon = serviceBuilderWithDefaults
                .with2(SwaggerAddon.defaults());
        assertEquals(SwaggerAddon.DEFAULT_PATH_SPEC, addon.configuration.pathSpec);
    }

    @Before
    public void init() {
        serviceBuilderWithDefaults = ServiceBuilder
                .defaults()
                .configJersey(JerseyConfig.defaults())
                .configJettyServer(JettyServer.defaults());
    }

}
