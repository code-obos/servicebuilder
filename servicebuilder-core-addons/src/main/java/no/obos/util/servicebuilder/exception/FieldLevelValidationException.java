package no.obos.util.servicebuilder.exception;

import java.util.List;
import java.util.Map;

public class FieldLevelValidationException extends RuntimeException {
    private Map<String, List<String>> errorFields;

    public Map<String, List<String>> getErrorFields() {
        return errorFields;
    }
}
