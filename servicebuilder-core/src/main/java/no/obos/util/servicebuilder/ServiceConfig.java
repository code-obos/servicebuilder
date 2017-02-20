package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig.Binder;
import no.obos.util.servicebuilder.JerseyConfig.Registrator;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConfig {
    @Wither
    final ImmutableList<Addon> addons;
    final ServiceDefinition serviceDefinition;
    @Wither
    final ImmutableList<Binder> binders;
    @Wither
    final ImmutableList<Registrator> registrators;

    public static ServiceConfig defaults(ServiceDefinition serviceDefinition) {
        return new ServiceConfig(ImmutableList.of(), serviceDefinition, ImmutableList.of(), ImmutableList.of());
    }

    public <T> ServiceConfig bind(Class<? extends T> toBind, Class<T> bindTo) {
        return withBinder(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> ServiceConfig bind(T toBind, Class<? super T> bindTo) {
        return withBinder(binder -> binder.bind(toBind).to(bindTo));
    }

    public ServiceConfig register(Class toRegister) {
        return withRegistrator(registrator -> registrator.register(toRegister));
    }

    public ServiceConfig addHk2ConfigModule(JerseyConfig.Hk2ConfigModule hk2ConfigModule) {
        return withRegistrator(hk2ConfigModule)
                .withBinder(hk2ConfigModule);
    }

    @SuppressWarnings("unchecked")
    public <T extends Addon> List<T> getAddons(Class<T> clazz) {
        return (List<T>) this.addons.stream()
                .filter(addon -> clazz.isInstance(addon))
                .collect(toList());
    }

    public <T extends Addon> List<T> requireAddons(Class<T> clazz) {
        List<T> addons = getAddons(clazz);
        if (addons.isEmpty()) {
            throw new RuntimeException("Required addon not found. Check config or priorities. " + clazz.getCanonicalName());
        }
        return addons;
    }

    public <T extends Addon> T getAddon(Class<T> clazz) {
        List<T> ret = getAddons(clazz);
        if (ret.isEmpty()) {
            return null;
        }
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public <T extends Addon> T requireAddon(Class<T> clazz) {
        List<T> ret = requireAddons(clazz);
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public ServiceConfig replaceAddon(Addon addon) {
        return this
                .withAddons(ImmutableList.copyOf(addons.stream()
                        .filter(existingAddon -> ! existingAddon.getClass().equals(addon.getClass()))
                        .collect(toList()))
                ).addon(addon);
    }


    public ServiceConfig withBinder(Binder binder) {
        return withBinders(GuavaHelper.plus(binders, binder));
    }

    public ServiceConfig addon(Addon addon) {
        return withAddons(GuavaHelper.plus(addons, addon));
    }

    public ServiceConfig withRegistrator(Registrator registrator) {
        return withRegistrators(GuavaHelper.plus(registrators, registrator));
    }
}
