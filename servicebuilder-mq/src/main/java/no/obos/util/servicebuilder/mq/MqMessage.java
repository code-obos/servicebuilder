package no.obos.util.servicebuilder.mq;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * Message as sent over wire
 */
@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
public class MqMessage<T> {
    public final String requestId;
    public final String sourceApp;
    public final T content;
}
