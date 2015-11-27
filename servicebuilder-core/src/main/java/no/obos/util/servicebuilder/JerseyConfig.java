package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.List;

public class JerseyConfig {

    @Getter
    final ResourceConfig resourceConfig = new ResourceConfig();

    final List<Binder> binders = Lists.newArrayList();

    final InjectionBinder injectionBinder = new InjectionBinder();

    public JerseyConfig() {
        resourceConfig.register(injectionBinder);
    }

    public JerseyConfig addBinder(Binder binder) {
        binders.add(binder);
        return this;
    }

    public JerseyConfig addRegistations(Registrator registrator) {
        registrator.applyRegistations(resourceConfig);
        return this;
    }

    public JerseyConfig addHk2ConfigModule(Hk2ConfigModule hk2ConfigModule) {
        addRegistations(hk2ConfigModule);
        addBinder(hk2ConfigModule);
        return this;
    }

    public JerseyConfig with(ServiceAddon addon) {
        addon.addToJerseyConfig(this);
        return this;
    }

    public JerseyConfig with(ServiceAddonConfig<?> addonConfig) {
        ServiceAddon addon = addonConfig.init();

        addon.addToJerseyConfig(this);
        return this;
    }

    public interface Binder {
        void addBindings(AbstractBinder binder);
    }


    public interface Registrator {
        void applyRegistations(ResourceConfig resourceConfig);
    }

    public interface Hk2ConfigModule extends Binder, Registrator{ }


    final class InjectionBinder extends AbstractBinder {
        @Override protected void configure() {
            for (Binder binder : binders) {
                binder.addBindings(this);
            }
        }
    }

    public static Configurator defaults() {
        return cfg -> cfg;
    }

    public interface Configurator {
        JerseyConfig apply(JerseyConfig cfg);
    }
}
