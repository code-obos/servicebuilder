package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
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

        final StringBuilder descriptions = new StringBuilder();

        handledMessages.values().forEach(md -> {
                    ObjectMapper mapper = md.jsonConfig.get();
                    String schema;
                    try {
                        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
                        visitor.setVisitorContext(new VisitorContext() {
                            @Override
                            public String javaTypeToUrn(JavaType jt) {
                                return null;
                            }
                        });
                        mapper.acceptJsonFormatVisitor(md.MessageType, visitor);
                        JsonSchema jsonSchema = visitor.finalSchema();
                        schema = mapper.writeValueAsString(jsonSchema);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    descriptions.append(String.format(
                            "\n\n###%s\n\n_%s_\n\nQueue name: %s\n\n%s",
                            md.name,
                            md.description,
                            md.getQueueName(),
                            schema));
                }

        );

        Info info = swagger.getInfo();
        if (info == null) {
            info = new Info();
        }

        String messageDescriptions = "#Handled messages\n"
                + descriptions;
        if (info.getDescription() != null) {

            info.description(info.getDescription() + "\n\n" + messageDescriptions);
        } else {
            info.description(messageDescriptions);
        }
    }
}
