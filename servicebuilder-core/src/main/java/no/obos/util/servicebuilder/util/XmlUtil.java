package no.obos.util.servicebuilder.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.SerializationSpec;

import static no.obos.util.servicebuilder.model.SerializationSpec.*;


@Slf4j
public final class XmlUtil {
    public static ObjectMapper createObjectMapper(SerializationSpec xmlConfig) {
        JacksonXmlModule
                module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        ObjectMapper underConstruction = new XmlMapper(module)
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());
        for (String option : xmlConfig.getOptions()) {
            switch (option) {
                case PRETTY_PRINT:
                    underConstruction = underConstruction.enable(SerializationFeature.INDENT_OUTPUT);
                    break;
                case TOLERATE_UNRECOGNIZED_FIELDS:
                    underConstruction = underConstruction.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    break;
                case TOLERATE_MISSING_FIELDS:
                    underConstruction = underConstruction.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                    break;
                case ISO_DATES:
                    underConstruction = underConstruction.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    break;
                case GUAVA_TYPES:
                    underConstruction = underConstruction.registerModule(new GuavaModule());
                    break;
                default:
                    log.warn("Unhandled json option: " + option);
                    break;
            }
        }
        return underConstruction;
    }
}
