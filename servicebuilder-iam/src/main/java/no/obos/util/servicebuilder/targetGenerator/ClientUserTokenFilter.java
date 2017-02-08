package no.obos.util.servicebuilder.targetGenerator;

import no.obos.util.servicebuilder.Constants;
import org.elasticsearch.common.Strings;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;

public class ClientUserTokenFilter implements ClientRequestFilter {
    @Context
    HttpServletRequest servletRequest;


    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String incomingUserToken = servletRequest.getHeader(Constants.USERTOKENID_HEADER);
        if(Strings.isNullOrEmpty(incomingUserToken)) {
            throw new RuntimeException("user token not found in request headers");
        }
        requestContext.getHeaders().add(Constants.USERTOKENID_HEADER, incomingUserToken);
    }
}
