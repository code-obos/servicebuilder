package no.obos.util.servicebuilder.log.model;

import lombok.Builder;
import lombok.Singular;
import lombok.ToString;

import java.util.Set;

@Builder(toBuilder = true)
@ToString
public class RestLogConfiguration {
    /**
     * Skal vi logge alle endepunkter som default?
     * Overstyres av whitelistMethods og blackListMethods og annotasjoner på endepunktet
     */
    public final boolean enableDefault;

    /**
     * Kall som går mot disse klassene vil aldri bli logget.
     * Overstyres av annotasjoner på endepunktet
     */
    @Singular
    public final Set<Class> blacklistClasses;


    /**
     * Standard konfigurasjon for logging av hvert kall
     * Overstyres av annotasjoner på kallet
     */
    public final LogParams defaultLogParams;
}
