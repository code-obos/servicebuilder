package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import java.util.function.Supplier;

public interface JsonConfig extends Supplier<ObjectMapper> {


    JsonConfig standard = () -> new ObjectMapper()
            .registerModule(new JSR310Module())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
}
