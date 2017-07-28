package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.model.MessageHandler;
import no.obos.util.servicebuilder.model.MessageSender;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.HandlerDescription;
import no.obos.util.servicebuilder.mq.MqHandlerForwarder;
import no.obos.util.servicebuilder.mq.MqHandlerImpl;
import no.obos.util.servicebuilder.mq.MqListener;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Map;

/**
 * Interface for queueing system.
 * Requires additional addon for binding to physical queueing system (e.g. ActiveMqAddon)
 * Store handled messages in servicedescription.
 * Register handled messages with handling classes with listen. Binding of application level handlers is handled in this class.
 * Register required senders. Senders are available for injection in application as MqSender and discriminated by message content type.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MqAddon implements Addon {

    @Wither(AccessLevel.PRIVATE)
    public final ImmutableSet<HandlerDescription<?>> handlers;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableSet<MessageDescription<?>> senders;
    @Wither(AccessLevel.PRIVATE)
    public final MqHandlerForwarder mqHandlerForwarder;


    public static MqAddon defaults = new MqAddon(ImmutableSet.of(), ImmutableSet.of(), null);

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        return this
                .withMqHandlerForwarder(new MqHandlerForwarder(serviceConfig.serviceDefinition.getJsonConfig().get()));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        // Feature is used to start the listeners immediately once dependencies are bound
        serviceConfig.addRegistations(registrator -> registrator
                .register(StartListenersFeature.class)
        );
        serviceConfig.addBinder((binder) -> {
            binder.bind(MqSenderResolver.class).to(JustInTimeInjectionResolver.class);
            handlers.forEach(handlerDescription ->
                    binder.bind(handlerDescription.messageHandlerClass).to(handlerDescription.messageHandlerClass)
            );
            binder.bind(this).to(MqAddon.class);
        });
    }

    public <T> MqAddon listen(MessageDescription<T> messageDescription, Class<? extends MessageHandler<T>> messageHandler) {
        @SuppressWarnings("unchecked")
        HandlerDescription<T> handlerDescription = HandlerDescription.<T>builder()
                .healthCheckEntriesGrace(60)
                .healthCheckEntriesMax(1)
                .messageDescription(messageDescription)
                .messageHandlerClass((Class<MessageHandler<T>>) messageHandler)
                .build();
        return this.withHandlers(GuavaHelper.plus(handlers, handlerDescription));
    }

    public MqAddon send(ServiceDefinition serviceDefinition) {
        return this.withSenders(GuavaHelper.plusAll(senders, serviceDefinition.getHandledMessages()));
    }

    static class MqSenderResolver implements JustInTimeInjectionResolver {
        @Inject
        ServiceLocator serviceLocator;
        @Inject
        Map<String, MessageSender> senderMap;

        @Override
        public boolean justInTimeResolution(Injectee failedInjectionPoint) {
            String typeName = failedInjectionPoint.getRequiredType().getTypeName();
            if (typeName.startsWith(MessageSender.class.getName()) && typeName.contains(">") && typeName.contains("<")) {
                String messageName = typeName.substring(typeName.indexOf('<') + 1, typeName.indexOf('>'));
                ServiceLocatorUtilities.addOneConstant(serviceLocator, senderMap.get(messageName), "null", failedInjectionPoint.getRequiredType());
                return true;
            }
            return false;
        }
    }


    private static class StartListenersFeature implements Feature {
        @Inject
        private ServiceLocator serviceLocator;

        @Override
        public boolean configure(FeatureContext context) {
            // Iterates through all configurations, which contains the names of the listeners and handlers
            MqListener listener = serviceLocator.getService(MqListener.class);
            MqAddon mqAddon = serviceLocator.getService(MqAddon.class);
            ImmutableSet<MqHandlerImpl<?>> handlers = mqAddon.handlers.stream()
                    .map(this::getHandlerImpl)
                    .collect(GuavaHelper.setCollector());
            listener.setHandlers(handlers);
            listener.startListener();
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

}
