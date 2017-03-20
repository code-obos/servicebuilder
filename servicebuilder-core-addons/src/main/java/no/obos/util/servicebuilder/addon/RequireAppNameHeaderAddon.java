package no.obos.util.servicebuilder.addon;

import io.swagger.jaxrs.ext.SwaggerExtensions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.appname.AppNameFilter;
import no.obos.util.servicebuilder.appname.SwaggerImplicitAppNameHeader;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.Addon;

@AllArgsConstructor
public class RequireAppNameHeaderAddon implements Addon {

    @Wither(AccessLevel.PRIVATE)
    public final boolean swaggerImplicitHeaders;

    public static final RequireAppNameHeaderAddon defaults = new RequireAppNameHeaderAddon(true);

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        if (swaggerImplicitHeaders && ! serviceConfig.isAddonPresent(SwaggerAddon.class)) {
            throw new DependenceException(this.getClass(), SwaggerAddon.class, "swaggerImplicitHeaders specified, SwaggerAddon missing");
        }
        return this;
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator
                .register(AppNameFilter.class)
        );

        if (swaggerImplicitHeaders) {
            SwaggerExtensions.getExtensions().add(new SwaggerImplicitAppNameHeader());
        }
    }


    public RequireAppNameHeaderAddon swaggerImplicitHeaders(boolean swaggerImplicitHeaders) {return withSwaggerImplicitHeaders(swaggerImplicitHeaders);}
}
