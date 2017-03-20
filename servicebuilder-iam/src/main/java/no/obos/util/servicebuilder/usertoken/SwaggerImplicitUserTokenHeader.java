package no.obos.util.servicebuilder.usertoken;

import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Operation;
import io.swagger.models.parameters.HeaderParameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.annotations.UserTokenRequired;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.util.AnnotationUtil;

import java.lang.reflect.Method;
import java.util.Iterator;

@AllArgsConstructor
@Slf4j
public class SwaggerImplicitUserTokenHeader extends AbstractSwaggerExtension {

    final boolean isUserTokenDefaultRequired;

    public void decorateOperation(Operation operation, Method method, Iterator<SwaggerExtension> chain) {
        boolean requireUserToken = isUserTokenDefaultRequired;
        UserTokenRequired annotation = AnnotationUtil.getAnnotation(UserTokenRequired.class, method);
        if (annotation != null) {
            requireUserToken = annotation.value();
        }
        if (requireUserToken) {
            HeaderParameter headerParameter = new HeaderParameter();
            headerParameter.setName(Constants.USERTOKENID_HEADER);
            headerParameter.setType("String");
            headerParameter.setAllowEmptyValue(false);
            headerParameter.setDescription("User token id");
            operation.addParameter(headerParameter);
        }
        super.decorateOperation(operation, method, chain);
    }



}
