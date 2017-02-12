package no.obos.util.servicebuilder.config;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Singular;
import no.obos.util.servicebuilder.PropertyProvider;

@Builder(toBuilder = true)
public class PropertyMap implements PropertyProvider {
    @Singular("property")
    public final ImmutableMap<String, String> properties;

    @Override
    public String get(String key) {
        return properties.get(key);
    }

    @Override
    public void failIfNotPresent(String... keys) {
        for(String key : keys) {
            if(keyIsValid(key)) {
                throw new RuntimeException("missing property: " + key);
            }
        }
    }

    private boolean keyIsValid(String key) {
        return ! properties.containsKey(key) || properties.get(key) == null || properties.get(key).trim().equals("");
    }

    @Override
    public void failIfNotPresent(Iterable<String> keys) {
        for(String key : keys) {
            if(keyIsValid(key)) {
                throw new RuntimeException("missing property: " + key);
            }
        }
    }
}
