package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.BetweenTestsAddon;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.HandlerDescription;
import no.obos.util.servicebuilder.mq.MessageHandler;
import no.obos.util.servicebuilder.mq.MqHandlerImpl;
import no.obos.util.servicebuilder.mq.MqSenderImpl;
import no.obos.util.servicebuilder.mq.SenderDescription;
import no.obos.util.servicebuilder.mq.mock.MqMock;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
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

            mqAddon.senders.forEach(senderDescription -> {
                MqSenderImpl<?> mqSenderImpl = getMqSender(senderDescription);
                binder.bind(mqSenderImpl).to(senderDescription.typeLiteral);
            });
        });

        // Feature is used to start the listeners immediately once dependencies are bound
        serviceConfig.addRegistations(registrator -> registrator
                .register(StartListenersFeature.class)
        );
    }

    private <T> MqSenderImpl<T> getMqSender(SenderDescription<T> senderDescription) {
        return MqSenderImpl.<T>builder()
                .messageDescription(senderDescription.messageDescription)
                .mqTextSender(mqMock)
                .senderName(serviceDefinition.getName())
                .objectMapper(serviceDefinition.getJsonConfig().get())
                .build();
    }

    private static class StartListenersFeature implements Feature {
        @Inject
        private ServiceLocator serviceLocator;

        @Override
        public boolean configure(FeatureContext context) {
            MqMock mqMock = serviceLocator.getService(MqMock.class);
            MqAddon mqAddon = serviceLocator.getService(MqAddon.class);
            ImmutableSet<MqHandlerImpl<?>> handlers = mqAddon.handlers.stream()
                    .map(this::getHandlerImpl)
                    .collect(GuavaHelper.setCollector());
            mqMock.setHandlers(handlers);
            mqMock.launchListenerThread();
            return true;
        }

        private <T> MqHandlerImpl<T> getHandlerImpl(HandlerDescription<T> handlerDescription) {
            MessageHandler<T> service = serviceLocator.getService(handlerDescription.messageHandlerClass);
            return MqHandlerImpl.<T>builder()
                    .handlerDescription(handlerDescription)
                    .messageHandler(service)
                    .build();
        }

    }

    @Override
    public void beforeNextTest() {
        mqMock.finishWork();
    }

    @Override
    public Set<Class<?>> initializeAfter() {return ImmutableSet.of(MqAddon.class);}
}
