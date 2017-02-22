package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.servicebuilder.applicationtoken.ApplicationTokenFilter;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Legger inn applikasjonsfilter. Avhenger av at TokenServiceAddon er lagt til.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationTokenFilterAddon implements Addon {
    public static final String CONFIG_KEY_ACCEPTED_APP_IDS = "apptoken.accepted.app.ids";

    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<Integer> acceptedAppIds;
    @Wither
    public final Predicate<ContainerRequestContext> fasttrackFilter;

    public static ApplicationTokenFilterAddon defaults = new ApplicationTokenFilterAddon(ImmutableList.of(), it -> false);

    @Override
    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_ACCEPTED_APP_IDS);
        ArrayList<String> acceptedIdStrings = Lists.newArrayList(properties.get(CONFIG_KEY_ACCEPTED_APP_IDS).split(","));
        List<Integer> acceptedIds = acceptedIdStrings.stream()
                .map(Integer::valueOf)
                .collect(toList());
        return this.withAcceptedAppIds(ImmutableList.copyOf(acceptedIds));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator.register(ApplicationTokenFilter.class));
        jerseyConfig.addBinder(binder -> {
            binder.bindFactory(ApplicationTokenAccessValidatorFactory.class).to(ApplicationTokenAccessValidator.class);
            binder.bind(this).to(ApplicationTokenFilterAddon.class);
        });
    }

    private static class ApplicationTokenAccessValidatorFactory implements Factory<ApplicationTokenAccessValidator> {
        final TokenServiceClient tokenServiceClient;
        final ApplicationTokenFilterAddon configuration;

        @Inject
        private ApplicationTokenAccessValidatorFactory(TokenServiceClient tokenServiceClient, ApplicationTokenFilterAddon configuration) {
            this.tokenServiceClient = tokenServiceClient;
            this.configuration = configuration;
        }

        @Override
        public ApplicationTokenAccessValidator provide() {
            ApplicationTokenAccessValidator applicationTokenAccessValidator = new ApplicationTokenAccessValidator();
            applicationTokenAccessValidator.setTokenServiceClient(tokenServiceClient);
            applicationTokenAccessValidator.setAcceptedAppIds(configuration.acceptedAppIds.stream().map(Object::toString).collect(Collectors.toList()));
            return applicationTokenAccessValidator;
        }

        @Override
        public void dispose(ApplicationTokenAccessValidator instance) {

        }
    }

    public ApplicationTokenFilterAddon plusAcceptedAppId(int appId) {
        return this.withAcceptedAppIds(GuavaHelper.plus(acceptedAppIds, appId));
    }
}
