package no.obos.util.servicebuilder.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Builder
@EqualsAndHashCode(of = {"name", "version"})
public class MessageDescription<T> {
    public final String name;
    public final String version;
    public final String description;
    public final Class<T> MessageType;
    private final String overriddenQueueName;
    private final String overriddenErrorQueueName;
    @Builder.Default public final JsonConfig jsonConfig = JsonConfig.standard;

    public String getQueueName() {
        if (overriddenQueueName == null) {
            return name + "_" + "v" + version;
        } else {
            return overriddenQueueName;
        }
    }

    public String getErrorQueueName() {
        if (overriddenErrorQueueName == null) {
            return name + "_" + "v" + version + "_ERROR";
        } else {
            return overriddenErrorQueueName;
        }
    }
}
