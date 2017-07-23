package no.obos.util.servicebuilder.mq;

import lombok.Builder;
import lombok.ToString;

/**
 * Metadata for message received from message queue
 */
@Builder
@ToString
public class MessageMeta {
    public final String requestId;
    public final String sourceApp;
}
