package no.obos.util.servicebuilder.log.model;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.model.LogLevel;
import no.obos.util.servicebuilder.util.GuavaHelper;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LogParams {
    /**
     * Skal det utføres logging for kallet? Hvis nei, overstyrer de alle andre parametre.
     */
    @Wither(AccessLevel.PRIVATE)
    public final boolean enableLogging;

    /**
     * Hvilket nivå skal kallet logges med?
     */
    @Wither(AccessLevel.PRIVATE)
    public final LogLevel logLevel;

    /**
     * Logger vi headere generelt?
     * Overstyres av skipHeaders
     */
    @Wither(AccessLevel.PRIVATE)
    public final boolean logHeaders;

    /**
     * Disse headerne logges aldri.
     */
    @Wither(AccessLevel.PRIVATE)
    public ImmutableSet<String> skipHeaders;

    /**
     * Skal vi som default logge returobjekter?
     */
    @Wither(AccessLevel.PRIVATE)
    public final boolean logResponseEntity;

    /**
     * Skal vi som default logge inputobjekter?
     */
    @Wither(AccessLevel.PRIVATE)
    public final boolean logRequestPayload;

    public final static LogParams defaults = new LogParams(true, LogLevel.INFO, false, ImmutableSet.of(), true, true);

    public LogParams enableLogging(boolean enableLogging) {
        return withEnableLogging(enableLogging);
    }

    public LogParams logLevel(LogLevel logLevel) {
        return withLogLevel(logLevel);
    }

    public LogParams logHeaders(boolean logHeaders) {
        return withLogHeaders(logHeaders);
    }

    public LogParams skipHeaders(ImmutableSet<String> skipHeaders) {
        return this.skipHeaders == skipHeaders ? this : new LogParams(this.enableLogging, this.logLevel, this.logHeaders, skipHeaders, this.logResponseEntity, this.logRequestPayload);
    }

    public LogParams clearSkipHeaders() {
        return withSkipHeaders(ImmutableSet.of());
    }

    public LogParams skipHeader(String skipHeader) {
        return withSkipHeaders(GuavaHelper.plus(ImmutableSet.of(), skipHeader));
    }

    public LogParams logResponseEntity(boolean logResponseEntity) {
        return withLogResponseEntity(logResponseEntity);
    }

    public LogParams logRequestPayload(boolean logRequestPayload) {
        return withLogRequestPayload(logRequestPayload);
    }
}
