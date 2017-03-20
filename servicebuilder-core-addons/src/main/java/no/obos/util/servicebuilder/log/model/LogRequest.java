package no.obos.util.servicebuilder.log.model;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LogRequest {
    public final String uri;
    public final ImmutableMap<String, String> headers;
    public final String entity;
    public final String user;
    public final String clientApplication;
}
