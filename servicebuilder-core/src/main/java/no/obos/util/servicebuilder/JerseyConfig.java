package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.Lists;
import lombok.Getter;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.JsonUtil;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.List;

public class JerseyConfig {

    @Getter
    final ResourceConfig resourceConfig = new ResourceConfig();

    final List<JerseyConfig.Binder> binders = Lists.newArrayList();

    final JerseyConfig.InjectionBinder injectionBinder = new JerseyConfig.InjectionBinder();

    private void registerServiceDefintion(ServiceDefinition serviceDefinition) {
        serviceDefinition.getResources().forEach(resourceConfig::register);

        ObjectMapper mapper = JsonUtil.createObjectMapper(serviceDefinition.getSerializationSpec());
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(provider);
    }

    public JerseyConfig(ServiceDefinition serviceDefinition) {
        resourceConfig.property("jersey.config.server.wadl.disableWadl", "true");
        registerServiceDefintion(serviceDefinition);
        resourceConfig.register(injectionBinder);
    }

    public JerseyConfig() {
        resourceConfig.register(injectionBinder);
    }

    public JerseyConfig addBinder(JerseyConfig.Binder binder) {
        binders.add(binder);
        return this;
    }

    public JerseyConfig addRegistations(JerseyConfig.Registrator registrator) {
        registrator.applyRegistations(resourceConfig);
        return this;
    }

    public JerseyConfig addRegistrators(Iterable<Registrator> registrators) {
        JerseyConfig jerseyConfig = this;
        for (Registrator registrator : registrators) {
            jerseyConfig = jerseyConfig.addRegistations(registrator);
        }
        return jerseyConfig;
    }

    public JerseyConfig addBinders(Iterable<Binder> binders) {
        JerseyConfig jerseyConfig = this;
        for (Binder binder : binders) {
            jerseyConfig = jerseyConfig.addBinder(binder);
        }
        return jerseyConfig;
    }

    public interface Binder {
        void addBindings(AbstractBinder binder);
    }


    public interface Registrator {
        void applyRegistations(ResourceConfig resourceConfig);
    }


    public interface Hk2ConfigModule extends Binder, Registrator {
    }


    class InjectionBinder extends AbstractBinder {
        @Override
        protected void configure() {
            for (Binder binder : binders) {
                binder.addBindings(this);
            }
        }
    }
}
