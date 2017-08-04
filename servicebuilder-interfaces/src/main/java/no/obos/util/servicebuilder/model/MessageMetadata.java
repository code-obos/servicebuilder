package no.obos.util.servicebuilder.model;

import lombok.Builder;
import lombok.ToString;

/**
 * Metadata for message received from message queue
 */
@Builder
@ToString
public class MessageMetadata {
    public final String requestId;
    public final String sourceApp;
}
