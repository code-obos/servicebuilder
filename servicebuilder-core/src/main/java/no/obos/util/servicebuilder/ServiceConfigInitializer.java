package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.model.Addon;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

class ServiceConfigInitializer {
    public static ServiceConfig finalize(ServiceConfig serviceConfig) {
        List<Addon> unFinalizedAddons = Lists.newArrayList(serviceConfig.addons);
        unFinalizedAddons.sort(new StartupOrderComparator());
        ServiceConfig withFinalizedAddons = serviceConfig.withAddons(ImmutableList.of());
        for (Addon addon : unFinalizedAddons) {
            withFinalizedAddons = withFinalizedAddons.addon(addon.finalize(withFinalizedAddons));
        }
        return withFinalizedAddons;
    }

    static class StartupOrderComparator implements Comparator<Addon> {
        @Override
        public int compare(Addon o1, Addon o2) {
            Set<Class<?>> o1StartAfter = o1.finalizeAfter();
            Set<Class<?>> o2StartAfter = o2.finalizeAfter();
            if (o1StartAfter.stream().anyMatch(clazz -> clazz.isInstance(o2))) {
                return 1;
            }
            if (o2StartAfter.stream().anyMatch(clazz -> clazz.isInstance(o1))) {
                return - 1;
            }
            return 0;
        }

    }
}
