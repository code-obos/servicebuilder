package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.model.Addon;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ServiceConfigInitializer {
    public static ServiceConfig finalize(ServiceConfig serviceConfig) {
        List<Addon> unFinalizedAddons = sortAddonList(serviceConfig.addons);
        ServiceConfig withFinalizedAddons = serviceConfig.withAddons(ImmutableList.of());
        for (Addon addon : unFinalizedAddons) {
            withFinalizedAddons = withFinalizedAddons.addon(addon.initialize(withFinalizedAddons));
        }
        return withFinalizedAddons;
    }

    private static List<Addon> sortAddonList(List<Addon> addons) {
        List<Addon> unSortedList = Lists.newArrayList(addons);
        List<Addon> sortedList = Lists.newArrayList();
        while (unSortedList.size() > 0) {
            List<Addon> addonsWithNoDependencies = unSortedList.stream().filter(possiblyDependent -> {
                Set<Class<?>> dependentOnSet = possiblyDependent.initializeAfter();
                return dependentOnSet.stream().noneMatch(hasDependenciesInList(unSortedList));
            }).collect(Collectors.toList());
            sortedList.addAll(addonsWithNoDependencies);
            unSortedList.removeAll(addonsWithNoDependencies);
            if (addonsWithNoDependencies.isEmpty()) {
                throw new RuntimeException("Dependency loop in addons: " + unSortedList);
            }
        }
        return sortedList;
    }

    private static Predicate<Class<?>> hasDependenciesInList(List<Addon> unSortedList) {
        return dependentOn -> unSortedList.stream().anyMatch(dependentOn::isInstance);
    }

}
