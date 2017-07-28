package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import no.obos.util.servicebuilder.model.MessageDescription;

@SwaggerDefinition
public class MqSwaggerDecorator implements ReaderListener {
    //Yeah, yeah global mutable. Only way to get around swagger not instantiating from hk2
    public static ImmutableMap<String, MessageDescription> handledMessages = ImmutableMap.of();


    @Override
    public void beforeScan(Reader reader, Swagger swagger) {
    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {

        final StringBuilder description = new StringBuilder();

        handledMessages.values().forEach(md -> {
                    JsonSchema jsonSchema;
                    try {
                        jsonSchema = md.jsonConfig.get().generateJsonSchema(md.MessageType);
                    } catch (JsonMappingException e) {
                        throw new RuntimeException(e);
                    }
                    description.append("\n\n"
                            + "<b>" + md.name + "</b><br>\n"
                            + "<it>" + md.description + "</it><br>\n"
                            + "Queue name: " + md.getQueueName() + "<br>\n"
                            + jsonSchema
                    );
                }

        );

        Info info = swagger.getInfo();
        if (info == null) {
            info = new Info();
        }
        info.description(info.getDescription() + "Handled messages" + description);
    }
}
