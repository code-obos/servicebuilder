package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.util.servicebuilder.model.MessageDescription;
import org.glassfish.hk2.api.TypeLiteral;

/**
 * Describes various aspects of a message handler. Internal use.
 */
@Builder
@EqualsAndHashCode
@ToString
public class HandlerDescription<T> {
    public final MessageDescription<T> messageDescription;
    public final Class<MessageHandler<T>> messageHandlerClass;
    public final TypeLiteral<MessageHandler<T>> handlerTypeLiteral;
    public final TypeReference<MqMessage<T>> messageTypeReference;
    public final int healthCheckEntriesMax;
    public final int healthCheckEntriesGrace;

}
