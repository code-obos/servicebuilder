package no.obos.util.servicebuilder;

import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.servicebuilder.client.StringProvider;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;

public class ApplicationTokenIdInjectorAddon implements Addon {

    public static final ApplicationTokenIdInjectorAddon defaults = new ApplicationTokenIdInjectorAddon();

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> binder.bindFactory(ApplicationTokenIdFactory.class).to(StringProvider.class).named(Constants.APPTOKENID_HEADER));
    }

    public static class ApplicationTokenIdFactory implements Factory<StringProvider> {

        @Inject
        TokenServiceClient tokenServiceClient;

        @Override
        public StringProvider provide() {
            return () -> tokenServiceClient.getApplicationToken().getApplicationTokenId();
        }

        @Override
        public void dispose(StringProvider s) {

        }
    }
}
