package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.model.JsonConfig;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientGenerator {
    public static final String SERVICE_DEFINITION_INJECTION = "servicedefinition";
    @Wither
    public final ClientConfig clientConfigBase;
    public final ServiceDefinition serviceDefinition;

    public static ClientGenerator defaults(ServiceDefinition serviceDefinition) {
        return new ClientGenerator(null, serviceDefinition);
    }

    public Client generate() {
        ClientConfig clientConfig = clientConfigBase != null
                ? clientConfigBase
                : new ClientConfig();
        final List<JerseyConfig.Binder> binders = new ArrayList<>();
        binders.add(binder -> binder.bind(serviceDefinition).to(ServiceDefinition.class).named(SERVICE_DEFINITION_INJECTION));

        JsonConfig jsonConfig = serviceDefinition.getJsonConfig();
        ObjectMapper mapper = jsonConfig.get();
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        clientConfig.register(provider);
        binders.add(binder -> binder.bind(mapper).to(ObjectMapper.class));

        clientConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                binders.forEach(it -> it.addBindings(this));
            }
        });

        return ClientBuilder.newClient(clientConfig);
    }
}
