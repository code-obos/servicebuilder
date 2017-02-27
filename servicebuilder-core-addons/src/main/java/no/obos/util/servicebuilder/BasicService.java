package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.addon.CorsFilterAddon;
import no.obos.util.servicebuilder.addon.ExceptionMapperAddon;
import no.obos.util.servicebuilder.addon.MetricsAddon;
import no.obos.util.servicebuilder.addon.ObosLogFilterAddon;
import no.obos.util.servicebuilder.addon.SwaggerAddon;
import no.obos.util.servicebuilder.model.ServiceDefinition;

public class BasicService {

    public static ServiceConfig defaults(ServiceDefinition serviceDefinition) {
        return ServiceConfig.defaults(serviceDefinition)
                .addon(SwaggerAddon.defaults)
                .addon(CorsFilterAddon.defaults)
                .addon(MetricsAddon.defaults)
                .addon(ObosLogFilterAddon.defaults)
                .addon(ExceptionMapperAddon.defaults)
                ;
    }
}
