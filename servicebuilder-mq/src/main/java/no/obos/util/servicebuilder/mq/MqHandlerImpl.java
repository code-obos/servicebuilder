package no.obos.util.servicebuilder.mq;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.util.servicebuilder.model.MessageHandler;

@Builder
@EqualsAndHashCode
@ToString
public class MqHandlerImpl<T> {
    public final HandlerDescription<T> handlerDescription;
    public final MessageHandler<T> messageHandler;
}
