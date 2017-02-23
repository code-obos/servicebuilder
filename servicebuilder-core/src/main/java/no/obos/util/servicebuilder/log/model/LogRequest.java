package no.obos.util.servicebuilder.log.model;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import java.lang.reflect.Method;
import java.net.URI;

@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LogRequest {
    public final URI uri;
    @Singular
    public final ImmutableSet<LogHeader> headers;
    public final String entity;
    public final String remoteAddr;
    public final String user;
    public final Method handlingMethod;
    public final Class handlingClass;
}
