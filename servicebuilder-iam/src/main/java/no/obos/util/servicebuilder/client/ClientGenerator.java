package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import lombok.Builder;
import no.obos.util.servicebuilder.JsonConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Builder
public class ClientGenerator {
//    final String appToken;
    public final Boolean exceptionMapping;
    public final JsonConfig jsonConfig;
    public final ClientConfig clientConfigBase;

    public Client generate() {
        ClientConfig clientConfig = clientConfigBase != null
                ? clientConfigBase
                : new ClientConfig();
        if (jsonConfig != null) {
            ObjectMapper mapper = jsonConfig.get();
            JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
            provider.setMapper(mapper);
            clientConfig.register(provider);
            clientConfig.register(JacksonFeature.class);
            clientConfig.register(new AbstractBinder() {

                @Override
                protected void configure() {
                    bind(mapper).to(ObjectMapper.class);
                }
            });
        }
        if(exceptionMapping == null || exceptionMapping) {
            clientConfig.register(ClientErrorResponseFilter.class);
        }

        return ClientBuilder.newClient(clientConfigBase);
    }

    public static class ClientGeneratorBuilder {
        private Boolean exceptionMapping = true;
        private JsonConfig jsonConfig = JsonConfig.standard;
    }
}
