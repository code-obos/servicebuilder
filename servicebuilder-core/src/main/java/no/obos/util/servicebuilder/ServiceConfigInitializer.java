package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.exception.DependenceException;

import java.util.List;

public class ServiceConfigInitializer {
    public static ServiceConfig addContext(ServiceConfig serviceConfig) {
        List<Addon> uninitializedAddons = Lists.newArrayList(serviceConfig.addons);
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
}
