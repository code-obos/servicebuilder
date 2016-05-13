package no.obos.util.servicebuilder.usertoken;


import com.google.common.collect.ImmutableList;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class BasicUibBrukerTest {
    private static final String APP_ID_STYREROMMET = "201";
    private static final String ORGID_DEFAULT = "999";
    private static final String ORGID_SELSKAP = "001";
    private static final String ROLLENAVN_REGNSKAP = "REGNSKAP";
    private static final String ROLLENAVN_FK = "FK";
    private static final String ROLLENAVN_RK = "RK";

    @Test
    public void testHarTilgang() {
        UserToken userToken = userTokenWithMockRoles(ROLLENAVN_REGNSKAP, ROLLENAVN_FK);
        ImmutableList.Builder<UibToJavaxRole> rolleBuilder = ImmutableList.builder();
        rolleBuilder.add(new UibToJavaxRoleMockImpl(ROLLENAVN_FK));

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, rolleBuilder.build());
        assertTrue(basicUibBruker.harTilgang(APP_ID_STYREROMMET, ORGID_SELSKAP, ROLLENAVN_REGNSKAP));
    }

    @Test
    public void testHarTilgangSkalReturnereFalseHvisIkkeTilgang() {
        UserToken userToken = userTokenWithMockRoles(ROLLENAVN_FK);
        ImmutableList.Builder<UibToJavaxRole> rolleBuilder = ImmutableList.builder();
        rolleBuilder.add(new UibToJavaxRoleMockImpl(ROLLENAVN_FK));

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, rolleBuilder.build());
        assertFalse(basicUibBruker.harTilgang(APP_ID_STYREROMMET, ORGID_SELSKAP, ROLLENAVN_REGNSKAP));
    }

    @Test
    public void testIsUserInRole() {
        UserToken userToken = userTokenWithMockRoles(ROLLENAVN_REGNSKAP, ROLLENAVN_FK);
        ImmutableList.Builder<UibToJavaxRole> rolleBuilder = ImmutableList.builder();
        rolleBuilder.add(new UibToJavaxRoleMockImpl(ROLLENAVN_FK));

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, rolleBuilder.build());
        assertTrue(basicUibBruker.isUserInRole(ROLLENAVN_FK));
    }

    @Test
    public void testIsUserInRoleSkalReturnereFalseHvisBrukerIkkeHarRollen() {
        UserToken userToken = userTokenWithMockRoles(ROLLENAVN_RK);
        ImmutableList.Builder<UibToJavaxRole> rolleBuilder = ImmutableList.builder();
        rolleBuilder.add(new UibToJavaxRoleMockImpl(ROLLENAVN_RK));

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, rolleBuilder.build());
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
