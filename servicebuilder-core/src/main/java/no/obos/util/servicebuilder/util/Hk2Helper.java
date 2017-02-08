package no.obos.util.servicebuilder.util;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationData;
import org.glassfish.hk2.api.InstantiationService;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class Hk2Helper {
    static protected String getInjecteeName(InstantiationService instantiationService) {
        InstantiationData instantiationData = instantiationService.getInstantiationData();
        Injectee parentInjectee = instantiationData.getParentInjectee();
        String name = null;
        Optional<Annotation> namedAnnotation = parentInjectee.getRequiredQualifiers().stream().filter(Named.class::isInstance).findFirst();
        if (namedAnnotation.isPresent()) {
            Named named = (Named) namedAnnotation.get();
            name = named.value();
        }

        if (name == null) {
            throw new RuntimeException("Named factory bound without name.");
        }
        return name;
    }
}
