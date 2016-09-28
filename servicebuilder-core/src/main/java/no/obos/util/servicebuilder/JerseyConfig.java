package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import lombok.Getter;
import no.obos.util.config.AppConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.List;
import java.util.function.Function;

public class JerseyConfig {

    /**
     * Is this is stateful service (with session manager)
     */
    public final boolean DEFAULT_STATEFUL_SERVICE = false;

    @Getter
    final ResourceConfig resourceConfig = new ResourceConfig();

    final List<Binder> binders = Lists.newArrayList();

    final InjectionBinder injectionBinder = new InjectionBinder();

    final ServiceBuilder serviceBuilder;

    boolean stateful = DEFAULT_STATEFUL_SERVICE;

    public JerseyConfig(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
        resourceConfig.register(injectionBinder);
    }

    public JerseyConfig() {
        this.serviceBuilder = null;
        resourceConfig.register(injectionBinder);
    }

    public JerseyConfig enableStateful() {
        stateful = true;
        return this;
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

    public JerseyConfig addHk2ConfigModuleWithProps(Function<AppConfig, Hk2ConfigModule> confFromProps) {
        Hk2ConfigModule conf = confFromProps.apply(serviceBuilder.appConfig);
        addRegistations(conf);
        addBinder(conf);
        return this;
    }

    public JerseyConfig with(ServiceAddon addon) {
        addon.addToJerseyConfig(this);
        return this;
    }

    public JerseyConfig with(ServiceAddonConfig<?> addonConfig) {
        if(serviceBuilder != null) {
            if(serviceBuilder.appConfig != null) {
                addonConfig.addAppConfig(serviceBuilder.appConfig);
            }
            addonConfig.addContext(serviceBuilder);
        }
        ServiceAddon addon = addonConfig.init();
        addon.addToJerseyConfig(this);
        return this;
    }

    public <T extends ServiceAddon> T with2(ServiceAddonConfig<T> addonConfig) {
        if(serviceBuilder != null) {
            if(serviceBuilder.appConfig != null) {
                addonConfig.addAppConfig(serviceBuilder.appConfig);
            }
            addonConfig.addContext(serviceBuilder);
        }
        T addon = addonConfig.init();

        addon.addToJerseyConfig(this);
        return addon;
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
