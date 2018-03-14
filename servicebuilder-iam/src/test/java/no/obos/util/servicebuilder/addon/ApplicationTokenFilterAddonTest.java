package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import no.obos.util.config.AppConfigException;
import no.obos.util.servicebuilder.annotations.AppIdWhitelist;
import no.obos.util.servicebuilder.config.PropertyMap;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ApplicationTokenFilterAddonTest {

    @Test
    public void whitelist_resourceWhitelistedAppIds() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "123")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123)
                ))
        ).doesNotThrowAnyException();
    }

    @Test
    public void whitelist_acceptedAppIds() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty.put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "123")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123)
                ))
        ).doesNotThrowAnyException();
    }

    @Test
    public void whitelist_resourceWhitelistedAppIds_multipleAppIds() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "123,456,789")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123, 456, 789)
                ))
        ).doesNotThrowAnyException();
    }

    @Test
    public void whitelist_resourceWhitelistedAppIds_multipleAppIds_space() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "123 ,456, 789")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123, 456, 789)
                ))
        ).doesNotThrowAnyException();
    }

    @Test
    public void whitelist_resourceWhitelistedAppIds_multipleAnnotations() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "123,456,789")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123),
                        createAppIdWhitelist(456),
                        createAppIdWhitelist(789)
                ))
        ).doesNotThrowAnyException();
    }

    @Test
    public void whitelist_combo_multipleAnnotations() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "123,333")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "456,789")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123, 456),
                        createAppIdWhitelist(456, 789),
                        createAppIdWhitelist(789),
                        createAppIdWhitelist(333)
                ))
        ).doesNotThrowAnyException();
    }

    @Test
    public void whitelist_combo_multipleAnnotations_appIdNotDeclared() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "123,333")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "456,789")
        );

        assertThatThrownBy(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(123, 456),
                        createAppIdWhitelist(456, 789),
                        createAppIdWhitelist(456, 789, 999),
                        createAppIdWhitelist(789),
                        createAppIdWhitelist(333)
                ))
        ).isInstanceOf(AppConfigException.class);
    }

    @Test
    public void whitelist_missingProperty() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty.put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
        );

        assertThatThrownBy(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(321)
                ))
        ).hasMessageContaining(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS);
    }

    @Test
    public void whitelist_appIdNotDeclared() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
                        .put(ApplicationTokenFilterAddon.CONFIG_KEY_RESOURCE_WHITELISTED_APP_IDS, "456")
        );

        assertThatThrownBy(() ->
                addon.checkAppIdWhitelists(ImmutableSet.of(
                        createAppIdWhitelist(321)
                ))
        ).isInstanceOf(AppConfigException.class);
    }

    @Test
    public void whitelist_noUseOf() {
        ApplicationTokenFilterAddon addon = createApplicationTokenFilterAddon(
                PropertyMap.empty.put(ApplicationTokenFilterAddon.CONFIG_KEY_ACCEPTED_APP_IDS, "0")
        );

        assertThatCode(() ->
                addon.checkAppIdWhitelists(emptySet())
        ).doesNotThrowAnyException();
    }

    private AppIdWhitelist createAppIdWhitelist(int... values) {
        return new AppIdWhitelist() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return AppIdWhitelist.class;
            }

            @Override
            public int[] value() {
                return values;
            }

            @Override
            public boolean exclusive() {
                return true;
            }
        };
    }

    private ApplicationTokenFilterAddon createApplicationTokenFilterAddon(PropertyProvider propertyMap) {
        return (ApplicationTokenFilterAddon) ApplicationTokenFilterAddon.defaults.withProperties(propertyMap);
    }

}