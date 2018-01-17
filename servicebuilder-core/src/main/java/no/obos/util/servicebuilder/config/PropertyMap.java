package no.obos.util.servicebuilder.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Strings.isNullOrEmpty;

public class PropertyMap extends RecursiveExpansionPropertyProvider {
    public final ImmutableMap<String, String> properties;

    public PropertyMap(Map<String, String> properties) {
        this.properties = ImmutableMap.copyOf(properties);
    }

    public static PropertyMap empty = new PropertyMap(ImmutableMap.of());

    public PropertyMap put(String key, String value) {
        return new PropertyMap(GuavaHelper.plus(properties, key, value));
    }

    public PropertyMap putAllProperties(Properties props) {
        return new PropertyMap(GuavaHelper.plus(properties, Maps.fromProperties(props)));
    }

    @Override
    public String getNoExpansion(String key) {
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
        return !properties.containsKey(key) || properties.get(key) == null || properties.get(key).trim().equals("");
    }

    @Override
    public void failIfNotPresent(Iterable<String> keys) {
        for (String key : keys) {
            if (keyIsValid(key)) {
                throw new RuntimeException("missing property: " + key);
            }
        }
    }


    public static PropertyMap fromJvmArgs() {
        if (isNullOrEmpty(System.getProperty(Constants.APPCONFIG_KEY))) {
            throw new IllegalStateException("Set property file in argument " + Constants.APPCONFIG_KEY);
        }

        InputStream input = null;

        try {
            input = new FileInputStream(System.getProperty(Constants.APPCONFIG_KEY));

            // load a properties file
            Properties prop = new Properties();
            prop.load(input);
            return empty.putAllProperties(prop);


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
