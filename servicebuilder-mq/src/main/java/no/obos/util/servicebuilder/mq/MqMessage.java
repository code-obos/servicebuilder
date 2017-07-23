package no.obos.util.servicebuilder.mq;


import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Message as sent over wire
 */
@Builder(toBuilder = true)
@AllArgsConstructor
public class MqMessage<T> {
    public final String requestId;
    public final String sourceApp;
    public final T content;
}
