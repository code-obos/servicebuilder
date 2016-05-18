package no.obos.util.servicebuilder.usertoken;

import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;

import java.util.ArrayList;
import java.util.List;


/**
 * Factory klasse for å generere userToken for testene
 */
public class UserTokenMockFactory {
    public static final String APP_ID_STYREROMMET = "201";
    public static final String ORGID_DEFAULT = "999";
    public static final String ORGID_SELSKAP = "001";
    public static final String ROLLENAVN_REGNSKAP = "REGNSKAP";
    public static final String ROLLENAVN_FK = "FK";
    public static final String ROLLENAVN_RK = "RK";

    public static UserToken userTokenWithMockRoles(String... roles) {
        UserToken userToken = new UserToken();
        userToken.setRoles(generateMockRoles());

        for (String role : roles) {
            String orgId = role.equalsIgnoreCase(ROLLENAVN_REGNSKAP) ? ORGID_SELSKAP : ORGID_DEFAULT;
            UserRole userRole = new UserRole(new UserRole.Identity(role, APP_ID_STYREROMMET, orgId));
            userToken.getRoles().add(userRole);
        }

        return userToken;
    }

    private static List<UserRole> generateMockRoles() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(new UserRole(new UserRole.Identity("active_directory_user_id"), "JUNIT"));
        return roles;
    }
}
