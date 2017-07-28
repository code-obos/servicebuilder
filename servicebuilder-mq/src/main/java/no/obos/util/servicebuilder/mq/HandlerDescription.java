package no.obos.util.servicebuilder.mq;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.model.MessageHandler;

/**
 * Describes various aspects of a message handler. Internal use.
 */
@Builder
@EqualsAndHashCode
@ToString
public class HandlerDescription<T> {
    public final MessageDescription<T> messageDescription;
    public final Class<MessageHandler<T>> messageHandlerClass;
    public final int healthCheckEntriesMax;
    public final int healthCheckEntriesGrace;

}
