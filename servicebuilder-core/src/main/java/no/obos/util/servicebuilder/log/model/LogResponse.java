package no.obos.util.servicebuilder.log.model;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LogResponse {
    @Singular
    public final ImmutableSet<LogHeader> headers;
    public final Integer status;
    public final Object entity;
    public final Class entityClass;
}
