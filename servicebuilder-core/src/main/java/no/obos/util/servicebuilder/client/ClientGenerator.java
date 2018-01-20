package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.model.SerializationSpec;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.JsonUtil;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientGenerator {
    public static final String SERVICE_DEFINITION_INJECTION = "servicedefinition";
    @Wither(AccessLevel.PRIVATE)
    public final ClientConfig clientConfigBase;
    @Wither(AccessLevel.PRIVATE)
    public final ServiceDefinition serviceDefinition;
    @Wither(AccessLevel.PRIVATE)
    public final String clientAppName;

    public static ClientGenerator defaults(ServiceDefinition serviceDefinition) {
        return new ClientGenerator(null, serviceDefinition, null);
    }

    public Client generate() {
        ClientConfig clientConfig = clientConfigBase != null
                ? new ClientConfig().loadFrom(clientConfigBase)
                : new ClientConfig();
        final List<JerseyConfig.Binder> binders = new ArrayList<>();
        binders.add(binder -> binder.bind(serviceDefinition).to(ServiceDefinition.class).named(SERVICE_DEFINITION_INJECTION));

        SerializationSpec serializationSpec = serviceDefinition.getSerializationSpec();
        ObjectMapper mapper = JsonUtil.createObjectMapper(serializationSpec);
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        clientConfig.register(provider);
        binders.add(binder -> binder.bind(mapper).to(ObjectMapper.class));
        if (!Strings.isNullOrEmpty(clientAppName)) {
            binders.add(binder -> binder.bind(clientAppName).to(String.class).named(ClientNameFilter.CLIENT_APPNAME));
        }

        clientConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                binders.forEach(it -> it.addBindings(this));
            }
        });

        return ClientBuilder.newClient(clientConfig);
    }

    public ClientGenerator clientConfigBase(ClientConfig clientConfigBase) {
        return withClientConfigBase(clientConfigBase);
    }

    public ClientGenerator clientAppName(String clientAppName) {
        return withClientAppName(clientAppName);
    }

    public ClientGenerator serviceDefinition(ServiceDefinition serviceDefinition) {
        return this.withServiceDefinition(serviceDefinition);
    }

}
