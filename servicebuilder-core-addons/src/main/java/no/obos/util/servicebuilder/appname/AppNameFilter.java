package no.obos.util.servicebuilder.appname;

import com.google.common.base.Strings;
import no.obos.util.servicebuilder.model.Constants;

import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION)
public class AppNameFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (allwaysAccept(requestContext)) {
            return;
        }
        String appNameHeader = requestContext.getHeaderString(Constants.CLIENT_APPNAME_HEADER);
        if (Strings.isNullOrEmpty(appNameHeader)) {
            throw new BadRequestException("Header specifying client name required: " + Constants.CLIENT_APPNAME_HEADER);
        }
    }


    public boolean allwaysAccept(ContainerRequestContext requestContext) {
        String aboslutePath = requestContext.getUriInfo().getAbsolutePath().toString();
        String requestMethod = requestContext.getMethod();

        return aboslutePath.contains("swagger") ||
                "OPTIONS".equals(requestMethod);
    }
}

