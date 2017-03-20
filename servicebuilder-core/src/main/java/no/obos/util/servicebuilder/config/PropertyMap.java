package no.obos.util.servicebuilder.config;

import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.util.Map;

public class PropertyMap implements PropertyProvider {
    public final ImmutableMap<String, String> properties;

    public PropertyMap(Map<String, String> properties) {
        this.properties = ImmutableMap.copyOf(properties);
    }

    public static PropertyMap empty = new PropertyMap(ImmutableMap.of());

    public PropertyMap put(String key, String value) {
        return new PropertyMap(GuavaHelper.plus(properties, key, value));
    }

    @Override
    public String get(String key) {
        return properties.get(key);
    }

    @Override
    public void failIfNotPresent(String... keys) {
        for (String key : keys) {
            if (keyIsValid(key)) {
                throw new RuntimeException("missing property: " + key);
            }
        }
    }

    private boolean keyIsValid(String key) {
        return ! properties.containsKey(key) || properties.get(key) == null || properties.get(key).trim().equals("");
    }

    @Override
    public void failIfNotPresent(Iterable<String> keys) {
        for (String key : keys) {
            if (keyIsValid(key)) {
                throw new RuntimeException("missing property: " + key);
            }
        }
    }
}
