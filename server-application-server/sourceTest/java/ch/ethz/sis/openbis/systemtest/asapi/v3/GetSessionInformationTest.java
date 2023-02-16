/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.junit.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

public class GetSessionInformationTest extends AbstractTest
{
    @SuppressWarnings("null")
    @Test
    public void testGetWithRegularSessionTokenAsSessionToken()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SessionInformation sessionInformation = v3api.getSessionInformation(sessionToken);

        assertNotNull(sessionInformation);
        assertEquals(sessionInformation.getSessionToken(), sessionToken);
        assertEquals(sessionInformation.getUserName(), TEST_USER);
        assertEquals(sessionInformation.getPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getCreatorPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getHomeGroupCode(), "CISD");
        assertFalse(sessionInformation.isPersonalAccessTokenSession());
        assertNull(sessionInformation.getPersonalAccessTokenSessionName());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithRegularSessionTokenInvalid()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        v3api.logout(sessionToken);

        try
        {
            v3api.getSessionInformation(sessionToken);
            fail();
        } catch (InvalidSessionException e)
        {
            // expected
        }
    }

    @Test
    public void testGetWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        SessionInformation sessionInformation = v3api.getSessionInformation(token.getHash());

        assertNotNull(sessionInformation);
        assertNull(sessionInformation.getSessionToken());
        assertEquals(sessionInformation.getUserName(), TEST_USER);
        assertEquals(sessionInformation.getPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getCreatorPerson().getUserId(), TEST_USER);
        assertEquals(sessionInformation.getHomeGroupCode(), "CISD");
        assertTrue(sessionInformation.isPersonalAccessTokenSession());
        assertEquals(sessionInformation.getPersonalAccessTokenSessionName(), token.getSessionName());
    }

    @Test
    public void testGetWithPersonalAccessTokenInvalid() throws InterruptedException
    {
        PersonalAccessTokenCreation tokenCreation = tokenCreation();
        tokenCreation.setValidFromDate(new Date());
        tokenCreation.setValidToDate(new Date(System.currentTimeMillis() + 250));

        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation);

        Thread.sleep(250);

        try
        {
            v3api.getSessionInformation(token.getHash());
            fail();
        } catch (InvalidSessionException e)
        {
            // expected
        }
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

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        v3api.getSessionInformation(sessionToken);

        assertAccessLog("get-session-information");
    }

}
