package no.obos.util.servicebuilder.addon;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.annotations.Transactional;
import org.glassfish.hk2.api.ServiceLocator;
import org.skife.jdbi.v2.Handle;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class JdbiAddonTransactionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private final ResourceInfo requestInfo;
    private final ServiceLocator serviceLocator;

    @Inject
    public JdbiAddonTransactionFilter(@Context ResourceInfo requestInfo, ServiceLocator serviceLocator) {
        this.requestInfo = requestInfo;
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        Arrays.stream(requestInfo.getResourceMethod().getDeclaredAnnotations())
                .filter(it -> it instanceof Transactional)
                .map(it -> (Transactional) it)
                .map(Transactional::name)
                .forEach(name -> {
                    Handle handle;
                    if ("".equals(name)) {
                        handle = serviceLocator.getService(Handle.class);
                    } else {
                        handle = serviceLocator.getService(Handle.class, name);
                    }
                    handle.begin();
                });
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException
    {
        serviceLocator.getAllServices(Handle.class)
                .forEach(handle -> {
                    if (handle.isInTransaction()) {
                        if (responseContext.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                            handle.commit();
                        } else {
                            log.debug("Rolling back transaction");
                            handle.rollback();
                        }

                    }
                });
    }
}
