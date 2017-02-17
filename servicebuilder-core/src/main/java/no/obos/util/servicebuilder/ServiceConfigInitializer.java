package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.exception.DependenceException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ServiceConfigInitializer {
    public static ServiceConfig addContext(ServiceConfig serviceConfig) {
        List<Addon> uninitializedAddons = Lists.newArrayList(serviceConfig.addons);
        uninitializedAddons.sort(new StartupOrderComparator());
        ServiceConfig withContext = serviceConfig.addons(ImmutableList.of());
        while(! uninitializedAddons.isEmpty()) {
            List<Dependency> dependencies = Lists.newArrayList();
            List<Addon> finalizedInThisRun = Lists.newArrayList();
            for (Addon toInitialize : uninitializedAddons) {
                try {
                    Addon finalizedAddon = toInitialize.withDependencies(withContext);
                    withContext = withContext.addon(finalizedAddon);
                    finalizedInThisRun.add(finalizedAddon);
                } catch(DependenceException ex) {
                    dependencies.add(new Dependency(toInitialize.getClass(), ex.dependency));
                }
            }
            if(finalizedInThisRun.isEmpty()) {
                throw new RuntimeException("Missing dependencies while configuring servicebuilder. Dependencies: " + dependencies);
            }
            uninitializedAddons.removeAll(finalizedInThisRun);
        }
        return withContext;
    }

    static class StartupOrderComparator implements Comparator<Addon> {
        @Override
        public int compare(Addon o1, Addon o2) {
            Set<Class<?>> o1StartBefore = o1.startBefore();
            Set<Class<?>> o1StartAfter = o1.startAfter();
            Set<Class<?>> o2StartBefore = o2.startBefore();
            Set<Class<?>> o2StartAfter = o2.startAfter();
            if(o1StartBefore.stream().anyMatch(clazz -> clazz.isInstance(o2))) {
                return -1;
            }
            if(o2StartBefore.stream().anyMatch(clazz -> clazz.isInstance(o1))) {
                return 1;
            }
            if(o1StartAfter.stream().anyMatch(clazz -> clazz.isInstance(o2))) {
                return 1;
            }
            if(o2StartAfter.stream().anyMatch(clazz -> clazz.isInstance(o1))) {
                return -1;
            }
            return 0;
        }

    }
}
