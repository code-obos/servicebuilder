package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import org.elasticsearch.common.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class JerseyConfigDocumentationTest {

    static ServiceBuilder serviceBuilderWithDefaults;

    public JerseyConfigDocumentationTest() {
        System.setProperty(ServiceBuilder.APPCONFIG_KEY, "src/test/resources/service-junit.properties");
    }

    @Test
    public void standard_configuration_contains_no_bindings() {
        serviceBuilderWithDefaults.configJersey(JerseyConfig.defaults());
    }

    @Test
    public void bindings_and_registrations_are_standard_hk2_in_interfaces() {
        JerseyConfig.Binder binder = new JerseyConfig.Binder() {
            @Override public void addBindings(AbstractBinder binder) {
                binder.bind("tekst").to(String.class).named("prosa");
                binder.bind(Integer.class).to(Integer.class).in(Singleton.class);
            }
        };

        JerseyConfig.Registrator registrator = new JerseyConfig.Registrator() {
            @Override public void applyRegistations(ResourceConfig resourceConfig) {
                resourceConfig
                        .register(Boolean.class)
                        .register(Double.class);
            }
        };

        serviceBuilderWithDefaults.configJersey(cfg ->
                cfg.addBinder(binder)
                        .addRegistations(registrator)
        );

        //or

        serviceBuilderWithDefaults.configJersey(JerseyConfig.defaults());
        serviceBuilderWithDefaults.getJerseyConfig().addBinder(binder);
        serviceBuilderWithDefaults.getJerseyConfig().addRegistations(registrator);
    }

    @Test
    public void bindings_and_registrations_may_be_combined_into_hk2Addin() {
        JerseyConfig.Hk2ConfigModule hk2ConfigModule = new JerseyConfig.Hk2ConfigModule() {
            @Override public void addBindings(AbstractBinder binder) {
                binder.bind("tekst").to(String.class);
                binder.bind(Integer.class).to(Integer.class);
            }

            @Override public void applyRegistations(ResourceConfig resourceConfig) {
                resourceConfig
                        .register(Boolean.class)
                        .register(Double.class);
            }
        };

        serviceBuilderWithDefaults.configJersey(cfg -> cfg
                .addHk2ConfigModule(hk2ConfigModule)
        );
    }

    @Test
    public void configuration_is_cumulative_not_replacing() {
        JerseyConfig.Binder stringBindings = binder -> {
            binder.bind("nacht").to(String.class).named("natt");
            binder.bind("niebel").to(String.class).named("tåke");
        };

        JerseyConfig.Binder doubleBinder = binder -> {
            binder.bind(3.14).to(Double.class).named("pi");
            binder.bind(42.0).to(Double.class).named("theanswer");
        };


        serviceBuilderWithDefaults.configJersey(cfg -> cfg
                .addBinder(stringBindings)
                .addBinder(doubleBinder)
        );

        //or

        serviceBuilderWithDefaults.configJersey(JerseyConfig.defaults());
        serviceBuilderWithDefaults.getJerseyConfig().addBinder(stringBindings);
        serviceBuilderWithDefaults.getJerseyConfig().addBinder(doubleBinder);
    }

    @Test
    public void registrations_are_added_to_request_context_on_registration_to_keep_causality() {
        final List<Boolean> modifiedOnConfig = Lists.newArrayList();


        serviceBuilderWithDefaults.configJersey(cfg -> cfg
                .addRegistations(regstrator ->
                        modifiedOnConfig.add(true)
                )
        );

        assertEquals(1, modifiedOnConfig.size());
    }

    @Test
    public void bindings_are_applied_at_startup_because_of_hk2_stuff() {
        final List<Boolean> modifiedOnConfig = Lists.newArrayList();

        serviceBuilderWithDefaults.configJersey(cfg -> cfg
                .addBinder(binder ->
                        modifiedOnConfig.add(true)
                )
        );

        assertEquals(0, modifiedOnConfig.size());
        serviceBuilderWithDefaults
                .configJettyServer(JettyServer.defaults())
                .start();
        assertEquals(1, modifiedOnConfig.size());
        serviceBuilderWithDefaults.stop();
    }

    @Test
    public void addons_can_be_added_to_jerseyconfig_without_servicebuilder_for_testing_purposes() {
        SwaggerAddon.Configuration.ConfigurationBuilder addonConfigurationBuilder = SwaggerAddon.defaultConfiguration();
        SwaggerAddon.Configuration configuration = addonConfigurationBuilder
                .apiVersion("quite some version")
                .build();
        SwaggerAddon addon = new SwaggerAddon(configuration);
        serviceBuilderWithDefaults.configJersey(JerseyConfig.defaults());
        serviceBuilderWithDefaults.getJerseyConfig().with(addon);

        //or


        serviceBuilderWithDefaults.configJersey(cfg -> cfg
                .with(SwaggerAddon.defaults())
        );
    }


    @Before
    public void init() {
        serviceBuilderWithDefaults = ServiceBuilder
                .defaults(JerseyConfigDocumentationTest.class);
    }
}
