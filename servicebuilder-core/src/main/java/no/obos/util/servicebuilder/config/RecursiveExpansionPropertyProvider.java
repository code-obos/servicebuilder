package no.obos.util.servicebuilder.config;

import no.obos.util.servicebuilder.model.PropertyProvider;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public abstract class RecursiveExpansionPropertyProvider implements PropertyProvider {

    @Override
    public String get(String key) {
        String noExpansion = getNoExpansion(key);
        if (isNullOrEmpty(noExpansion)) {
            return null;
        } else {
            return getWithExpandedPropertiesRecursor(this, key, new ArrayList<>());
        }
    }

    abstract String getNoExpansion(String key);

    @Override
    public String getWithFallback(String key, String fallback) {
        String prop = get(key);
        return !isNullOrEmpty(prop)
                ? prop
                : fallback;
    }

    @Override
    public String requireWithFallback(String key, String fallback) {
        String prop = get(key);
        if (!isNullOrEmpty(prop)) {
            return prop;
        } else if (!isNullOrEmpty(fallback)) {
            return fallback;
        } else {
            throw new RuntimeException("missing property: " + key);
        }
    }

    private String getWithExpandedPropertiesRecursor(RecursiveExpansionPropertyProvider propertyProvider, String key, List<String> alreadyExpanded) {
        if (alreadyExpanded.contains(key)) {
            throw new IllegalStateException("Loop in variable expansion. Offending key: " + key + " previous values: " + alreadyExpanded);
        }
        alreadyExpanded.add(key);
        if (propertyProvider.getNoExpansion(key) == null) {
            throw new IllegalStateException("Missing value when expanding variable. Path: " + alreadyExpanded);
        }
        String valueUnexpanded = propertyProvider.getNoExpansion(key);
        String result = "";
        String leftToParse = valueUnexpanded;
        while (leftToParse.length() != 0) {
            int tokenBegin = leftToParse.indexOf("${");
            if (tokenBegin == -1) {
                result += leftToParse;
                leftToParse = "";
            } else {
                result += leftToParse.substring(0, tokenBegin);
                String fromVariableStart = leftToParse.substring(tokenBegin + 2);
                int tokenEnd = fromVariableStart.indexOf('}');
                if (tokenEnd == -1) {
                    throw new IllegalStateException("For property " + key + " Missing end of variable: " + leftToParse.substring(tokenBegin));
                } else {
                    leftToParse = fromVariableStart.substring(tokenEnd + 1);
                    String nestedKey = fromVariableStart.substring(0, tokenEnd);
                    result += getWithExpandedPropertiesRecursor(propertyProvider, nestedKey, new ArrayList<>(alreadyExpanded));
                }
            }
        }
        return result;
    }

}
