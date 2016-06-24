package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;

public class GetSessionInformationTest extends AbstractSampleTest
{
    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SessionInformation sessionInformation = v3api.getSessionInformation(sessionToken);
        assertTrue(sessionInformation != null);
        assertTrue(sessionInformation.getCreatorPerson() != null);
        assertTrue(sessionInformation.getPerson() != null);
        v3api.logout(sessionToken);
    }
}
