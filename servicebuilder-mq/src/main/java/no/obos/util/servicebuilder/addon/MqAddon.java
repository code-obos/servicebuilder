package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.mq.HandlerDescription;
import no.obos.util.servicebuilder.mq.MessageHandler;
import no.obos.util.servicebuilder.mq.MqHandlerForwarder;
import no.obos.util.servicebuilder.mq.MqSender;
import no.obos.util.servicebuilder.mq.SenderDescription;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.TypeLiteral;

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
    public final ImmutableSet<SenderDescription<?>> senders;
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
        serviceConfig.addBinder((binder) -> {
            handlers.forEach(handlerDescription ->
                    binder.bind(handlerDescription.messageHandlerClass).to(handlerDescription.messageHandlerClass)
            );
            binder.bind(this).to(MqAddon.class);
        });
    }

    public <T> MqAddon listen(MessageDescription<T> messageDescription, Class<? extends MessageHandler<T>> messageHandler) {
        TypeLiteral<MessageHandler<T>> typeLiteral = new TypeLiteral<MessageHandler<T>>() {};

        @SuppressWarnings("unchecked")
        HandlerDescription<T> handlerDescription = HandlerDescription.<T>builder()
                .healthCheckEntriesGrace(60)
                .healthCheckEntriesMax(1)
                .messageDescription(messageDescription)
                .messageHandlerClass((Class<MessageHandler<T>>) messageHandler)
                .build();
        return this.withHandlers(GuavaHelper.plus(handlers, handlerDescription));
    }

    public <T> MqAddon send(MessageDescription<T> messageDescription, TypeLiteral<MqSender<T>> typeLiteral) {

        SenderDescription<T> senderDescription = SenderDescription.<T>builder()
                .messageDescription(messageDescription)
                .typeLiteral(typeLiteral)
                .build();
        return this.withSenders(GuavaHelper.plus(senders, senderDescription));
    }


}
