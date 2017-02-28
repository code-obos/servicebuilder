package no.obos.util.servicebuilder.applicationtoken;

import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Operation;
import io.swagger.models.parameters.HeaderParameter;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.annotations.AppTokenRequired;
import no.obos.util.servicebuilder.model.Constants;

import java.lang.reflect.Method;
import java.util.Iterator;

@AllArgsConstructor
public class SwaggerImplicitAppTokenHeader extends AbstractSwaggerExtension {

    final boolean isAppTokenDefaultRequired;

    public void decorateOperation(Operation operation, Method method, Iterator<SwaggerExtension> chain) {
        boolean requireAppToken = isAppTokenDefaultRequired;
        AppTokenRequired methodAnnotation = method.getAnnotation(AppTokenRequired.class);
        AppTokenRequired classAnnotation = method.getDeclaringClass().getAnnotation(AppTokenRequired.class);
        if (methodAnnotation != null) {
            requireAppToken = methodAnnotation.value();
        } else if (classAnnotation != null) {
            requireAppToken = classAnnotation.value();
        }
        if (requireAppToken) {
            HeaderParameter headerParameter = new HeaderParameter();
            headerParameter.setName(Constants.APPTOKENID_HEADER);
            headerParameter.setType("String");
            headerParameter.setAllowEmptyValue(false);
            headerParameter.setDescription("Application token id");
            operation.addParameter(headerParameter);
        }
        super.decorateOperation(operation, method, chain);
    }

}
