package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath.ClassInfo;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.config.AppConfigException;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.annotations.AppIdWhitelist;
import no.obos.util.servicebuilder.applicationtoken.ApplicationTokenFilter;
import no.obos.util.servicebuilder.applicationtoken.SwaggerImplicitAppTokenHeader;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.util.ClassPathUtil;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Legger inn applikasjonsfilter. Avhenger av at TokenServiceAddon er lagt til.
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationTokenFilterAddon implements Addon {

    public static final String CONFIG_KEY_ACCEPTED_APP_IDS = "apptoken.accepted.app.ids";
    /**
     * Må deklareres ved bruk av app-ID-er i {@link AppIdWhitelist} som ikke fins i
     * {@link #CONFIG_KEY_ACCEPTED_APP_IDS}, ellers overflødig.
     */
    public static final String CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS = "apptoken.resource.whitelisted.app.ids";
    public static final ImmutableSet<String> DEFAULT_PROJECT_PACKAGE_NAMES = ImmutableSet.of("no.obos");

    @Wither(AccessLevel.PRIVATE)
    public final boolean requireAppTokenByDefault;

    @Wither(AccessLevel.PRIVATE)
    public final boolean swaggerImplicitHeaders;

    @Wither(AccessLevel.PRIVATE)
    public final String acceptedAppIds;

    @Wither(AccessLevel.PRIVATE)
    public final Predicate<ContainerRequestContext> fasttrackFilter;

    @Wither(AccessLevel.PRIVATE)
    public final Set<String> projectPackageNames;

    public static ApplicationTokenFilterAddon defaults =
            new ApplicationTokenFilterAddon(true, true, "", it -> false, DEFAULT_PROJECT_PACKAGE_NAMES, null);

    /**
     * Deleted after initialization.
     */
    private PropertyProvider properties;

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        if (swaggerImplicitHeaders && ! serviceConfig.isAddonPresent(SwaggerAddon.class)) {
            throw new DependenceException(this.getClass(), SwaggerAddon.class, "swaggerImplicitHeaders specified, SwaggerAddon missing");
        }

        if (! serviceConfig.isAddonPresent(TokenServiceAddon.class)) {
            throw new DependenceException(this.getClass(), TokenServiceAddon.class);
        }

        if (properties != null) {
            Set<Class> exceptClasses = findJerseyClientResourceClasses(serviceConfig);
            checkAppIdWhitelists(getAppIdWhitelists(projectPackageNames, exceptClasses));
            properties = null;
        } else {
            log.warn("Could not perform check for whitelists. Property provider is null.");
        }

        return this;
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        this.properties = properties;
        properties.failIfNotPresent(CONFIG_KEY_ACCEPTED_APP_IDS);
        String acceptedIds = properties.get(CONFIG_KEY_ACCEPTED_APP_IDS);
        return withAcceptedAppIds(acceptedIds);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator.register(ApplicationTokenFilter.class));
        jerseyConfig.addBinder(binder -> {
            binder.bindFactory(ApplicationTokenAccessValidatorFactory.class).to(ApplicationTokenAccessValidator.class);
            binder.bind(this).to(ApplicationTokenFilterAddon.class);
        });

        if (swaggerImplicitHeaders) {
            SwaggerExtensions.getExtensions().add(new SwaggerImplicitAppTokenHeader(requireAppTokenByDefault));
        }
    }

    @AllArgsConstructor(onConstructor = @__({@Inject}))
    private static class ApplicationTokenAccessValidatorFactory implements Factory<ApplicationTokenAccessValidator> {

        final TokenServiceClient tokenServiceClient;
        final ApplicationTokenFilterAddon configuration;

        @Override
        public ApplicationTokenAccessValidator provide() {
            ApplicationTokenAccessValidator applicationTokenAccessValidator = new ApplicationTokenAccessValidator();
            applicationTokenAccessValidator.setTokenServiceClient(tokenServiceClient);
            applicationTokenAccessValidator.setAcceptedAppIds(configuration.acceptedAppIds);
            return applicationTokenAccessValidator;
        }

        @Override
        public void dispose(ApplicationTokenAccessValidator instance) {
        }
    }

    public ApplicationTokenFilterAddon acceptedAppIds(String acceptedAppIds) {
        return withAcceptedAppIds(acceptedAppIds);
    }

    public ApplicationTokenFilterAddon requireAppTokenByDefault(boolean requireAppTokenByDefault) {
        return withRequireAppTokenByDefault(requireAppTokenByDefault);
    }

    public ApplicationTokenFilterAddon swaggerImplicitHeaders(boolean swaggerImplicitHeaders) {
        return withSwaggerImplicitHeaders(swaggerImplicitHeaders);
    }

    public ApplicationTokenFilterAddon fasttrackFilter(Predicate<ContainerRequestContext> fasttrackFilter) {
        return withFasttrackFilter(fasttrackFilter);
    }

    public ApplicationTokenFilterAddon projectPackageNames(Set<String> projectPackageNames) {
        return withProjectPackageNames(projectPackageNames);
    }

    @Override
    public Set<Class<?>> initializeAfter() {
        return ImmutableSet.of(SwaggerAddon.class, TokenServiceAddon.class, JerseyClientAddon.class);
    }

    void checkAppIdWhitelists(Set<AppIdWhitelist> appIdWhitelists) {
        if (isEmpty(appIdWhitelists)) {
            log.info("{} is not used", AppIdWhitelist.class.getSimpleName());
            return;
        }

        log.info("{} is used and therefore requires appIds to be declared in either property {} or {}",
                AppIdWhitelist.class.getSimpleName(),
                CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS,
                CONFIG_KEY_ACCEPTED_APP_IDS
        );

        Set<Integer> appIdsInWhitelists = appIdWhitelists.stream()
                .map(AppIdWhitelist::value)
                .flatMapToInt(Arrays::stream)
                .boxed()
                .collect(toSet());
        Set<Integer> acceptedAppIdsSet = convertToAppIds(acceptedAppIds);

        Set<Integer> whitelistableAppIds = findWhitelistableAppIds(appIdsInWhitelists, acceptedAppIdsSet);
        Set<Integer> appIdsNotDeclaredInProperty = Sets.difference(appIdsInWhitelists, whitelistableAppIds);

        if (isNotEmpty(appIdsNotDeclaredInProperty)) {
            throw new AppConfigException(
                    String.format("App-IDs %s are used in %s, but are not declared in property %s or %s",
                            appIdsNotDeclaredInProperty,
                            AppIdWhitelist.class.getSimpleName(),
                            CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS,
                            CONFIG_KEY_ACCEPTED_APP_IDS
                    ));
        }
    }

    private Set<Integer> findWhitelistableAppIds(Set<Integer> appIdsInWhitelists, Set<Integer> acceptedAppIdsSet) {
        if (! acceptedAppIdsSet.containsAll(appIdsInWhitelists)) {
            return Sets.union(getResourceWhitelistedAppIds(appIdsInWhitelists), acceptedAppIdsSet);
        }

        return acceptedAppIdsSet;
    }

    private Set<Integer> getResourceWhitelistedAppIds(Set<Integer> appIdsInWhitelists) {
        Set<Integer> resourceWhitelistedAppIds;
        properties.failIfNotPresent(CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS);
        resourceWhitelistedAppIds = convertToAppIds(properties.get(CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS));

        Set<Integer> appIdsNotUsedInWhitelist = Sets.difference(resourceWhitelistedAppIds, appIdsInWhitelists);

        if (! appIdsNotUsedInWhitelist.isEmpty()) {
            log.warn("App-IDs {} are not used in {} and are therefore superfluous in property {}",
                    appIdsNotUsedInWhitelist,
                    AppIdWhitelist.class.getSimpleName(),
                    CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS);
        }
        return resourceWhitelistedAppIds;
    }

    private static Set<AppIdWhitelist> getAppIdWhitelists(Set<String> packageNames, Set<Class> exceptClasses) {
        return packageNames.stream()
                .map(ApplicationTokenFilterAddon::getTopLevelClassesRecursive)
                .flatMap(Collection::stream)
                .map(ApplicationTokenFilterAddon::loadClass)
                .filter(Objects::nonNull)
                .filter(clazz -> ! exceptClasses.contains(clazz))
                .map(ApplicationTokenFilterAddon::getAppIdWhitelists)
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    private static Set<ClassInfo> getTopLevelClassesRecursive(String packageName) {
        try {
            return ClassPathUtil.getTopLevelClassesRecursive(packageName);
        } catch (IOException e) {
            throw new AppConfigException(e);
        }
    }

    private static Class<?> loadClass(ClassInfo classInfo) {
        try {
            return classInfo.load();
        } catch (NoClassDefFoundError error) {
            return null;
        }
    }

    private static Set<AppIdWhitelist> getAppIdWhitelists(Class<?> classInfo) {
        return Optional.ofNullable(classInfo)
                .map(clazz -> ClassPathUtil.findDeclaredAnnotations(clazz, AppIdWhitelist.class))
                .orElse(emptySet());
    }

    private static Set<Integer> convertToAppIds(String resourceWhitelistedAppIds) {
        return Arrays.stream(resourceWhitelistedAppIds.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Integer::valueOf)
                .collect(toSet());
    }

    private Set<Class> findJerseyClientResourceClasses(ServiceConfig serviceConfig) {
        return serviceConfig.addonInstances(JerseyClientAddon.class).stream()
                .map(jerseyClientAddon -> jerseyClientAddon.serviceDefinition)
                .flatMap(serviceDefinition -> serviceDefinition.getResources().stream())
                .collect(toSet());
    }
}
