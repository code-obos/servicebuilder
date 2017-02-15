package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.JerseyConfig.Binder;
import no.obos.util.servicebuilder.JerseyConfig.Registrator;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConfig {
    final ImmutableList<Addon> addons;
    final ServiceDefinition serviceDefinition;
    final ImmutableList<Binder> binders;
    final ImmutableList<Registrator> registrators;

    public static ServiceConfig defaults(ServiceDefinition serviceDefinition) {
        return new ServiceConfig(ImmutableList.of(), serviceDefinition, ImmutableList.of(), ImmutableList.of());
    }

    public <T> ServiceConfig bind(Class<? extends T> toBind, Class<T> bindTo) {
        return binder(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> ServiceConfig bind(T toBind, Class<? super T> bindTo) {
        return binder(binder -> binder.bind(toBind).to(bindTo));
    }

    public ServiceConfig register(Class toRegister) {
        return registrator(registrator -> registrator.register(toRegister));
    }

    public ServiceConfig addHk2ConfigModule(JerseyConfig.Hk2ConfigModule hk2ConfigModule) {
        return registrator(hk2ConfigModule)
                .binder(hk2ConfigModule);
    }

    @SuppressWarnings("unchecked")
    public <T extends Addon> List<T> getAddons(Class<T> clazz) {
        return (List<T>) this.addons.stream()
                .filter(addon -> addon.getClass().equals(clazz))
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
                .addons(addons.stream()
                        .filter(existingAddon -> ! existingAddon.getClass().equals(addon.getClass()))
                        .collect(toList())
                ).addon(addon);
    }


    public ServiceConfig binder(Binder binder) {
        return binders(GuavaHelper.plus(binders, binder));
    }

    public ServiceConfig addon(Addon addon) {
        return addons(GuavaHelper.plus(addons, addon));
    }

    public ServiceConfig registrator(Registrator registrator) {
        return registrators(GuavaHelper.plus(registrators, registrator));
    }

    public ServiceConfig addons(List<Addon> addons) {
        return new ServiceConfig(ImmutableList.copyOf(addons), serviceDefinition, binders, registrators);
    }

    public ServiceConfig binders(List<Binder> binders) {
        return new ServiceConfig(addons, serviceDefinition, ImmutableList.copyOf(binders), registrators);
    }

    public ServiceConfig registrators(List<Registrator> registrators) {
        return new ServiceConfig(addons, serviceDefinition, binders, ImmutableList.copyOf(registrators));
    }
}
