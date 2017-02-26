package no.obos.util.servicebuilder;

public class Addons {

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static CorsFilterAddon cors() {
        return CorsFilterAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static ExceptionMapperAddon exceptionMapper() {
        return ExceptionMapperAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static JerseyClientAddon jerseyClient(ServiceDefinition serviceDefinition) {
        return JerseyClientAddon.defaults(serviceDefinition);
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static MetricsAddon metrics() {
        return MetricsAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static SwaggerAddon swagger() {
        return SwaggerAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static WebAppAddon webAppAddon() {
        return WebAppAddon.defaults;
    }


    public static H2InMemoryDatasourceAddon h2InMemoryDatasource() {
        return H2InMemoryDatasourceAddon.defaults;
    }
}
