package no.obos.util.servicebuilder.appname;

import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Operation;
import io.swagger.models.parameters.HeaderParameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.Constants;

import java.lang.reflect.Method;
import java.util.Iterator;

@AllArgsConstructor
@Slf4j
public class SwaggerImplicitAppNameHeader extends AbstractSwaggerExtension {

    public void decorateOperation(Operation operation, Method method, Iterator<SwaggerExtension> chain) {
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.setName(Constants.CLIENT_APPNAME_HEADER);
        headerParameter.setType("String");
        headerParameter.setAllowEmptyValue(false);
        headerParameter.setDescription("User token id");
        headerParameter.setDefaultValue("swagger");
        operation.addParameter(headerParameter);
        super.decorateOperation(operation, method, chain);
    }
}
