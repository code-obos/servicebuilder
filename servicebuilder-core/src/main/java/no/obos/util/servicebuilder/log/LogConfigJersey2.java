package no.obos.util.servicebuilder.log;

import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.RestLogConfiguration;
import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor;

public class LogConfigJersey2 {
    public final static RestLogConfiguration defaults = RestLogConfiguration.builder()
            .blacklistClass(WadlModelProcessor.OptionsHandler.class)
            .defaultLogParams(LogParams.defaults)
            .enableDefault(true)
            .build();

    public final static String DEFAULT_CONFIG = defaults.toString();
}
