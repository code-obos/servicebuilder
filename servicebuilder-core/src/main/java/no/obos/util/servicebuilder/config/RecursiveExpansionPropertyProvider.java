package no.obos.util.servicebuilder.config;

import no.obos.util.config.AppConfigException;
import no.obos.util.servicebuilder.model.PropertyProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class RecursiveExpansionPropertyProvider implements PropertyProvider {

    @Override
    public String get(String key) {
        return getWithExpandedPropertiesRecursor(this, key, new ArrayList<>());
    }

    abstract String getNoExpansion(String key);

    private String getWithExpandedPropertiesRecursor(RecursiveExpansionPropertyProvider propertyProvider, String key, List<String> alreadyExpanded) {
        if (alreadyExpanded.contains(key)) {
            throw new AppConfigException("Loop in variable expansion. Offending key: " + key + " previous values: " + alreadyExpanded);
        }
        alreadyExpanded.add(key);
        String valueUnexpanded = getValueUnexpanded(propertyProvider, key);
        if (valueUnexpanded == null) {
            throw new AppConfigException("Missing value when expanding variable. Path: " + alreadyExpanded);
        }
        StringBuilder result = new StringBuilder();
        String leftToParse = valueUnexpanded;
        while (leftToParse.length() != 0) {
            int tokenBegin = leftToParse.indexOf("${");
            if (tokenBegin == - 1) {
                result.append(leftToParse);
                leftToParse = "";
            } else {
                result.append(leftToParse.substring(0, tokenBegin));
                String fromVariableStart = leftToParse.substring(tokenBegin + 2);
                int tokenEnd = fromVariableStart.indexOf('}');
                if (tokenEnd == - 1) {
                    throw new AppConfigException("For property " + key + " Missing end of variable: " + leftToParse.substring(tokenBegin));
                } else {
                    leftToParse = fromVariableStart.substring(tokenEnd + 1);
                    String nestedKey = fromVariableStart.substring(0, tokenEnd);
                    result.append(getWithExpandedPropertiesRecursor(propertyProvider, nestedKey, new ArrayList<>(alreadyExpanded)));
                }
            }
        }
        return result.toString();
    }

    private String getValueUnexpanded(RecursiveExpansionPropertyProvider propertyProvider, String key) {
        String property = System.getProperty(key);
        if (property != null) {
            return property;
        }
        String env = System.getenv(key);
        if (env != null) {
            return env;
        }
        return propertyProvider.getNoExpansion(key);
    }

}
