package no.obos.util.servicebuilder.mq;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.mq.MessageHandler;
import org.glassfish.hk2.api.TypeLiteral;

@Builder
@EqualsAndHashCode
@ToString
public class SenderDescription<T> {
    public final MessageDescription<T> messageDescription;
    public final TypeLiteral<MessageHandler<T>> typeLiteral;
}
