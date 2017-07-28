package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.BetweenTestsAddon;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.MessageSender;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.MessageSenderImpl;
import no.obos.util.servicebuilder.mq.MqListener;
import no.obos.util.servicebuilder.mq.SenderDescription;
import no.obos.util.servicebuilder.mq.mock.MqMock;
import org.glassfish.hk2.api.TypeLiteral;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MqMockAddon implements BetweenTestsAddon {
    @Wither(AccessLevel.PRIVATE)
    public final MqMock mqMock;

    @Wither(AccessLevel.PRIVATE)
    public final MqAddon mqAddon;

    @Wither(AccessLevel.PRIVATE)
    public final ServiceDefinition serviceDefinition;

    public static final MqMockAddon defaults = new MqMockAddon(null, null, null);

    @Override
    public MqMockAddon initialize(ServiceConfig serviceConfig) {
        MqAddon mqAddon = serviceConfig.requireAddonInstance(MqAddon.class);

        MqMock mqMock = MqMock.builder()
                .mqHandlerForwarder(mqAddon.mqHandlerForwarder)
                .listenMessageDescriptions(
                        mqAddon.handlers.stream()
                                .map(it -> it.messageDescription)
                                .collect(Collectors.toList())
                )
                .senderDescriptions(mqAddon.senders)
                .build();

        return this
                .withMqMock(mqMock)
                .withMqAddon(mqAddon)
                .withServiceDefinition(serviceConfig.serviceDefinition)
                ;
    }

    @Override
    public void cleanUp() {
        mqMock.stop();
    }


    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        serviceConfig.addBinder((binder) -> {
            binder.bind(this.mqMock).to(MqMock.class);
            binder.bind(this.mqMock).to(MqListener.class);
            ImmutableMap<String, MessageSender> senderMap = ImmutableMap.copyOf(
                    mqAddon.senders.stream()
                            .collect(Collectors.toMap(
                                    sd -> sd.messageDescription.MessageType.getName(),
                                    this::getMqSender
                            ))
            );
            binder.bind(senderMap).to(new TypeLiteral<Map<String, MessageSender>>() {});
        });
    }


    private <T> MessageSenderImpl<T> getMqSender(SenderDescription<T> senderDescription) {
        return MessageSenderImpl.<T>builder()
                .messageDescription(senderDescription.messageDescription)
                .mqTextSender(mqMock)
                .senderName(serviceDefinition.getName())
                .objectMapper(serviceDefinition.getJsonConfig().get())
                .build();
    }

    @Override
    public void beforeNextTest() {
        mqMock.finishWork();
    }

    @Override
    public Set<Class<?>> initializeAfter() {return ImmutableSet.of(MqAddon.class);}
}
