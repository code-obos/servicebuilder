package no.obos.util.servicebuilder.applicationtoken;

import no.obos.iam.access.TokenCheckResult;
import no.obos.iam.tokenservice.ApplicationToken;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceClientException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** This is a modification of ApplicationTokenAccessValidator.groovy, which differs in that it strictly requires an app
 * id to be an integer. **/
public class NumericAppIdApplicationTokenAccessValidator {

    private TokenServiceClient tokenServiceClient;
    private Set<Integer> acceptedAppIds = new HashSet<>();

    public NumericAppIdApplicationTokenAccessValidator(TokenServiceClient tokenServiceClient, String acceptedAppIds) {
        this.tokenServiceClient = tokenServiceClient;
        this.setAcceptedAppIds(acceptedAppIds);
    }

    NumericAppIdApplicationTokenAccessValidator() {} // for unit tests

    @Deprecated
    // Bruk metoden checkApplicationTokenId
    TokenCheckResult check(String applicationTokenId) {
        return checkApplicationTokenId(applicationTokenId);
    }

    private void setAcceptedAppIds(String acceptedAppIds) {
        if (acceptedAppIds == null)
            return;
        this.acceptedAppIds = convertAcceptedAppIds(acceptedAppIds);
    }

    TokenCheckResult checkApplicationTokenId(String applicationTokenId) {
        ApplicationToken applicationToken;
        try {
            applicationToken = tokenServiceClient.getApptokenById(applicationTokenId);
        } catch (TokenServiceClientException tsce) {
            if (tsce.getIssue() == TokenServiceClientException.Issue.INVALID_APPLICATION_TOKEN_ID)
                return TokenCheckResult.INVALID_TOKEN;
            else
                throw new RuntimeException("Service call failed", tsce);
        }
        return checkApplicationToken(applicationToken);
    }

    TokenCheckResult checkApplicationToken(ApplicationToken token) {
        if (token == null)
            return TokenCheckResult.UNAUTHORIZED;
        int appId = Integer.valueOf(token.getApplicationId());
        boolean accepted = acceptedAppIds.contains(appId);

        return accepted ? TokenCheckResult.AUTHORIZED : TokenCheckResult.UNAUTHORIZED;
    }

    private static Integer parseAppId(String appId) {
        return Integer.valueOf(appId.trim());
    }

    static Set<Integer> convertAcceptedAppIds(String acceptedAppIdsAsStrings) {
        return new HashSet<Integer>() {{
            if (acceptedAppIdsAsStrings != null) {
                addAll(
                        Arrays.stream(acceptedAppIdsAsStrings.split(","))
                                .map(NumericAppIdApplicationTokenAccessValidator::parseAppId)
                                .collect(Collectors.toSet())
                );
            }
        }};
    }

}
