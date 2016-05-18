package no.obos.util.servicebuilder.usertoken;


import com.google.common.collect.ImmutableList;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.APP_ID_STYREROMMET;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ORGID_DEFAULT;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ORGID_SELSKAP;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ROLLENAVN_FK;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ROLLENAVN_REGNSKAP;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ROLLENAVN_RK;

public class BasicUibBrukerTest {


    @Test
    public void testIsUserInRole() {
        UserToken userToken = userTokenWithMockRoles(ROLLENAVN_REGNSKAP, ROLLENAVN_FK);

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, ImmutableList.of(new UibToJavaxRoleMockImpl(ROLLENAVN_FK)));
        assertTrue(basicUibBruker.isUserInRole(ROLLENAVN_FK));
    }

    @Test
    public void testIsUserInRoleSkalReturnereFalseHvisBrukerIkkeHarRollen() {
        UserToken userToken = userTokenWithMockRoles(ROLLENAVN_RK);

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, ImmutableList.of(new UibToJavaxRoleMockImpl(ROLLENAVN_RK)));
        assertFalse(basicUibBruker.isUserInRole(ROLLENAVN_FK));
    }

    private UserToken userTokenWithMockRoles(String... roles) {
        UserToken userToken = new UserToken();
        userToken.setRoles(generateMockRoles());

        for (String role : roles) {
            String orgId = role.equalsIgnoreCase(ROLLENAVN_REGNSKAP) ? ORGID_SELSKAP : ORGID_DEFAULT;
            UserRole userRole = new UserRole(new UserRole.Identity(role, APP_ID_STYREROMMET, orgId));
            userToken.getRoles().add(userRole);
        }

        return userToken;
    }

    private List<UserRole> generateMockRoles() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(new UserRole(new UserRole.Identity("active_directory_user_id"), "JUNIT"));
        return roles;
    }
}
