package no.obos.util.servicebuilder;

public class BasicService {

    public static ServiceConfig.ServiceConfigBuilder defaults() {
        return ServiceConfig.builder()
                .addon(SwaggerAddon.builder().build())
                .addon(CorsFilterAddon.builder().build())
                .addon(MetricsAddon.builder().build())
                .addon(ObosLogFilterAddon.builder().build())
                .addon(ExceptionMapperAddon.builder().build())
                ;
    }
}
