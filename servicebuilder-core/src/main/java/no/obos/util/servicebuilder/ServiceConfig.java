package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig.Binder;
import no.obos.util.servicebuilder.JerseyConfig.Registrator;
import no.obos.util.servicebuilder.addon.NamedAddon;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConfig {
    @Wither(AccessLevel.PACKAGE)
    final ImmutableList<Addon> addons;
    public final ServiceDefinition serviceDefinition;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableList<Binder> binders;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableList<Registrator> registrators;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableList<Function<PropertyProvider, JerseyConfig.Hk2ConfigModule>> hk2ConfigProp;


    public static ServiceConfig defaults(ServiceDefinition serviceDefinition) {
        return new ServiceConfig(ImmutableList.of(), serviceDefinition, ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    }

    public <T> ServiceConfig bind(Class<? extends T> toBind, Class<T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> ServiceConfig bind(T toBind, Class<? super T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> ServiceConfig bind(Class<T> toBind) {
        return bind(binder -> binder.bindAsContract(toBind));
    }

    public ServiceConfig bind(Binder binder) {
        return withBinders(GuavaHelper.plus(binders, binder));
    }

    public ServiceConfig bindWithProps(BiConsumer<PropertyProvider, AbstractBinder> propertyBinder) {
        return hk2ConfigModule(props ->
                new JerseyConfig.Hk2ConfigModule() {
                    @Override
                    public void addBindings(AbstractBinder binder) {
                        propertyBinder.accept(props, binder);

                    }

                    @Override
                    public void applyRegistations(ResourceConfig resourceConfig) {
                    }
                }
        );
    }

    public ServiceConfig registerInstance(Object toRegister) {
        return register(registrator -> registrator.register(toRegister));
    }

    public ServiceConfig register(Class toRegister) {
        return register(registrator -> registrator.register(toRegister));
    }


    public ServiceConfig register(Registrator registrator) {
        return withRegistrators(GuavaHelper.plus(registrators, registrator));
    }

    public ServiceConfig hk2ConfigModule(JerseyConfig.Hk2ConfigModule hk2ConfigModule) {
        return register(hk2ConfigModule)
                .bind(hk2ConfigModule);
    }

    public ServiceConfig hk2ConfigModule(Function<PropertyProvider, JerseyConfig.Hk2ConfigModule> prop2Hk2) {
        return withHk2ConfigProp(GuavaHelper.plus(hk2ConfigProp, prop2Hk2));
    }

    ServiceConfig addPropertiesAndApplyToBindings(PropertyProvider properties) {
        ServiceConfig ret = this.withHk2ConfigProp(ImmutableList.of());
        for (Function<PropertyProvider, JerseyConfig.Hk2ConfigModule> i : hk2ConfigProp) {
            ret = ret.hk2ConfigModule(i.apply(properties));
        }
        return ret.bind(properties, PropertyProvider.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends Addon> List<T> addonInstances(Class<T> clazz) {
        return (List<T>) this.addons.stream()
                .filter(clazz::isInstance)
                .collect(toList());
    }

    public <T extends Addon> List<T> requireAddonInstanceAtLeastOne(Class<T> clazz) {
        List<T> addons = addonInstances(clazz);
        if (addons.isEmpty()) {
            throw new RuntimeException("Required addon not found. Check config or priorities. " + clazz.getCanonicalName());
        }
        return addons;
    }

    public <T extends Addon> T addonInstance(Class<T> clazz) {
        List<T> ret = addonInstances(clazz);
        if (ret.isEmpty()) {
            return null;
        }
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public <T extends NamedAddon> T addonInstanceNamed(Class<T> clazz, String name) {
        List<T> ret = addonInstances(clazz);
        if (name != null) {
            ret = ret.stream().filter(it -> name.equals(it.getName())).collect(toList());
        } else {
            ret = ret.stream().filter(it -> it.getName() == null).collect(toList());
        }

        if (ret.isEmpty()) {
            return null;
        }
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public <T extends Addon> T requireAddonInstance(Class<T> clazz) {
        List<T> ret = requireAddonInstanceAtLeastOne(clazz);
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public ServiceConfig removeAddon(Class<? extends Addon> addon) {
        return this
                .withAddons(ImmutableList.copyOf(addons.stream()
                        .filter(existingAddon -> ! addon.isInstance(existingAddon))
                        .collect(toList()))
                );
    }

    public ServiceConfig clearAddons() {
        return this.withAddons(ImmutableList.of());
    }

    public ServiceConfig addon(Addon addon) {
        return withAddons(GuavaHelper.plus(addons, addon));
    }

    public boolean isAddonPresent(Class<? extends Addon> swaggerAddonClass) {
        return addonInstance(swaggerAddonClass) != null;
    }
}
