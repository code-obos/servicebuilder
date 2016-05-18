package no.obos.util.servicebuilder.usertoken;


import com.google.common.collect.ImmutableList;
import no.obos.iam.tokenservice.UserToken;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.APP_ID_STYREROMMET;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ORGID_SELSKAP;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ROLLENAVN_FK;
import static no.obos.util.servicebuilder.usertoken.UserTokenMockFactory.ROLLENAVN_REGNSKAP;

public class TilgangssjekkerTest {

    @Test
    public void testHarTilgang() {
        UserToken userToken = UserTokenMockFactory.userTokenWithMockRoles(ROLLENAVN_REGNSKAP, ROLLENAVN_FK);

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, ImmutableList.of(new UibToJavaxRoleMockImpl(ROLLENAVN_FK)));
        assertTrue(basicUibBruker.harTilgang(APP_ID_STYREROMMET, ORGID_SELSKAP, ROLLENAVN_REGNSKAP));
    }

    @Test
    public void testHarTilgangSkalReturnereFalseHvisIkkeTilgang() {
        UserToken userToken = UserTokenMockFactory.userTokenWithMockRoles(ROLLENAVN_FK);

        BasicUibBruker basicUibBruker = new BasicUibBruker(userToken, ImmutableList.of(new UibToJavaxRoleMockImpl(ROLLENAVN_FK)));
        assertFalse(basicUibBruker.harTilgang(APP_ID_STYREROMMET, ORGID_SELSKAP, ROLLENAVN_REGNSKAP));
    }
}
