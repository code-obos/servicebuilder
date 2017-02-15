package no.obos.util.servicebuilder;

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
