package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import java.util.function.Supplier;

public interface JsonConfig extends Supplier<ObjectMapper> {


    JsonConfig standard = () -> {
        JSR310Module module = new JSR310Module();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        return mapper;
    };
}
