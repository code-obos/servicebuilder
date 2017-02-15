package no.obos.util.servicebuilder;

public class BasicService {

    public static ServiceConfig defaults(ServiceDefinition serviceDefinition) {
        return ServiceConfig.defaults(serviceDefinition)
                .withAddon(SwaggerAddon.builder().build())
                .withAddon(CorsFilterAddon.builder().build())
                .withAddon(MetricsAddon.builder().build())
                .withAddon(ObosLogFilterAddon.builder().build())
                .withAddon(ExceptionMapperAddon.builder().build())
                ;
    }
}
