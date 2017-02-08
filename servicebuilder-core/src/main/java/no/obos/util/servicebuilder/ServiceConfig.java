package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
public class ServiceConfig {
    @Singular
    final ImmutableList<Addon> addons;

    final ServiceDefinition serviceDefinition;

    @Singular
    final ImmutableList<JerseyConfig.Binder> binders;
    @Singular
    final ImmutableList<JerseyConfig.Registrator> registrators;

    public static class ServiceConfigBuilder {
        public <T> ServiceConfigBuilder bind(Class<? extends T> toBind, Class<T> bindTo) {
            return binder(binder -> binder.bind(toBind).to(bindTo));
        }

        public <T> ServiceConfigBuilder bind(T toBind, Class<T> bindTo) {
            return binder(binder -> binder.bind(toBind).to(bindTo));
        }
        public ServiceConfigBuilder register(Class toRegister) {
            return registrator(registrator -> registrator.register(toRegister));
        }

        public ServiceConfigBuilder addHk2ConfigModule(JerseyConfig.Hk2ConfigModule hk2ConfigModule) {
            registrator(hk2ConfigModule);
            return binder(hk2ConfigModule);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Addon> List<T> getAddons(Class<T> clazz) {
        return (List<T>) this.addons.stream()
                .filter(addon -> addon.getClass().equals(clazz))
                .collect(Collectors.toList());
    }

    public <T extends Addon> List<T> requireAddons(Class<T> clazz) {
        List<T> addons = getAddons(clazz);
        if(addons.isEmpty()) {
            throw new RuntimeException ("Required addon not found. Check config or priorities. " + clazz.getCanonicalName());
        }
        return addons;
    }

    public <T extends Addon> T getAddon(Class<T> clazz) {
        List<T> ret = getAddons(clazz);
        if(ret.isEmpty()) {
            return null;
        }
        if(ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public <T extends Addon> T requireAddon(Class<T> clazz) {
        List<T> ret = requireAddons(clazz);
        if(ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public ServiceConfig replaceAddon(Addon addon) {
        return this.toBuilder()
                .addons(addons.stream()
                        .filter(existingAddon -> ! existingAddon.getClass().equals(addon.getClass()))
                        .collect(Collectors.toList())
                ).addon(addon)
                .build();
    }
}
