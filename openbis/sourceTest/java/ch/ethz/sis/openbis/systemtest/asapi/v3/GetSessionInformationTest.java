package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

public class GetSessionInformationTest extends AbstractTest
{
    @SuppressWarnings("null")
    @Test
    public void testGet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SessionInformation sessionInformation = v3api.getSessionInformation(sessionToken);
        assertTrue(sessionInformation != null);
        assertTrue(sessionInformation.getCreatorPerson() != null);
        assertTrue(sessionInformation.getPerson() != null);
        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.getSessionInformation(sessionToken);
                    }
                });
        } else
        {
            SessionInformation sessionInformation = v3api.getSessionInformation(sessionToken);
            assertEquals(sessionInformation.getPerson().getUserId(), user.getUserId());
        }

        v3api.logout(sessionToken);
    }

}
