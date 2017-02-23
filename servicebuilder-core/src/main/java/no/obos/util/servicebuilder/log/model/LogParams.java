package no.obos.util.servicebuilder.log.model;

import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class LogParams {
    /**
     * Skal det utføres logging for kallet? Hvis nei, overstyrer de alle andre parametre.
     */
    public final boolean enableLogging;

    /**
     * Hvilket nivå skal kallet logges med?
     */
    public final LogLevel logLevel;

    /**
     * Logger vi headere generelt?
     * Overstyres av whitelistHeaders og blacklistHeaders
     */
    public final boolean logHeaders;

    /**
     * Disse headerne logges alltid. Overstyres av blacklistHeaders
     */
    @Singular
    public ImmutableSet<String> whitelistHeaders;

    /**
     * Disse headerne logges aldri.
     */
    @Singular
    public ImmutableSet<String> blacklistHeaders;

    /**
     * Skal vi som default logge returobjekter?
     */
    public final boolean logResponseEntity;

    /**
     * Skal vi som default logge inputobjekter?
     */
    public final boolean logRequestEntity;

    /**
     * Skal vi logge informasjon om avsender (ip/port)
     */
    public final boolean logSender;

    /**
     * Skal vi logge innlogget bruker(hvis tilgjengelig)
     */
    public final boolean logUser;

    /**
     * Skal vi logge avsenderapplikasjon (hvis tilgjengelig)
     */
    public final boolean logClientApplication;

    /**
     * Skal vi logge metode/klasse vi går mot?
     */
    public final boolean logTargetMethod;

    /**
     * Skal vi logge url?
     */
    public final boolean logUrl;

    public final static LogParams defaults = builder()
            .enableLogging(true)
            .logLevel(LogLevel.DEBUG)
            .logHeaders(true)
            .logClientApplication(true)
            .logResponseEntity(true)
            .logRequestEntity(true)
            .logSender(true)
            .logTargetMethod(true)
            .logUrl(true)
            .logUser(true)
            .build();

    public final static String DEFAULT_SETTINGS = defaults.toString();

    /**
     * Should logging headers even be considered?
     */
    public boolean considerHeaders() {
        return logHeaders || whitelistHeaders.size() > 0;
    }

    public boolean shouldLogHeader(String headerName) {
        boolean blacklisted = blacklistHeaders.contains(headerName);
        boolean whitelisted = whitelistHeaders.contains(headerName);
        return ! blacklisted && (logHeaders || whitelisted);
    }
}
