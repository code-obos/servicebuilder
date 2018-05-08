package no.obos.util.servicebuilder.applicationtoken;

import lombok.Value;
import no.obos.iam.tokenservice.ApplicationToken;

import java.security.Principal;
import java.util.Optional;

@Value
public class ApplicationPrincipal implements Principal {

    ApplicationToken applicationToken;

    @Override
    public String getName() {
        return Optional.ofNullable(applicationToken)
                .map(ApplicationToken::getApplicationId)
                .orElse(null);
    }

}

