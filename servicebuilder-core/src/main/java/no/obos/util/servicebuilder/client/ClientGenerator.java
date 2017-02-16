package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.JsonConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientGenerator {
    //    final String appToken;
    public final JsonConfig jsonConfig;
    public final ClientConfig clientConfigBase;
    public final Boolean exceptionMapping;

    public static ClientGenerator defaults = new ClientGenerator(JsonConfig.standard, null, false);

    public Client generate() {
        ClientConfig clientConfig = clientConfigBase != null
                ? clientConfigBase
                : new ClientConfig();
        if (jsonConfig != null) {
            ObjectMapper mapper = jsonConfig.get();
            JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
            provider.setMapper(mapper);
            clientConfig.register(provider);
            clientConfig.register(new AbstractBinder() {

                @Override
                protected void configure() {
                    bind(mapper).to(ObjectMapper.class);
                }
            });
        }
        if (exceptionMapping == null || exceptionMapping) {
            clientConfig.register(ClientErrorResponseFilter.class);
        }

        return ClientBuilder.newClient(clientConfig);
    }

    public ClientGenerator jsonConfig(JsonConfig jsonConfig) {return this.jsonConfig == jsonConfig ? this : new ClientGenerator(jsonConfig, this.clientConfigBase, this.exceptionMapping);}

    public ClientGenerator clientConfigBase(ClientConfig clientConfigBase) {return this.clientConfigBase == clientConfigBase ? this : new ClientGenerator(this.jsonConfig, clientConfigBase, this.exceptionMapping);}

    public ClientGenerator exceptionMapping(Boolean exceptionMapping) {return this.exceptionMapping == exceptionMapping ? this : new ClientGenerator(this.jsonConfig, this.clientConfigBase, exceptionMapping);}
}
