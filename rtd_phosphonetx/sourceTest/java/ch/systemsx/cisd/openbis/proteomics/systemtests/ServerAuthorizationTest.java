/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.proteomics.systemtests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "slow", "systemtest" })
public class ServerAuthorizationTest extends AbstractProteomicsSystemTestCase
{
    private static final String USER_A = "USER_A";

    private static final String USER_INSTANCE_OBSERVER = "USER_B";

    private static final SpaceIdentifier SPACE_A = new SpaceIdentifier("CISD");

    @BeforeClass
    public void createTestUsers()
    {
        assignSpaceRole(registerPerson(USER_A), RoleCode.ETL_SERVER, SPACE_A);
        assignInstanceRole(registerPerson(USER_INSTANCE_OBSERVER), RoleCode.OBSERVER);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testForServerSetSessionUserFailedBecauseOfAuthorization()
    {
        String sessionToken = authenticateAs(USER_A);
        getServer().setSessionUser(sessionToken, "abc");
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testListProteinSummariesByExperimentFailedBecauseOfAuthorization()
    {
        String sessionToken = authenticateAs(USER_A);
        getServer().listProteinSummariesByExperiment(sessionToken, new TechId(42));
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testForDataServiceInternalSetSessionUserFailedBecauseOfAuthorization()
    {
        IProteomicsDataServiceInternal dataServiceInternal = getDataServiceInternal();
        String sessionToken = dataServiceInternal.tryAuthenticate(USER_A, "abc").getSessionToken();
        dataServiceInternal.setSessionUser(sessionToken, "abc");
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testForDataServiceInternalListExperimentsFailedBecauseOfAuthorization()
    {
        IProteomicsDataServiceInternal dataServiceInternal = getDataServiceInternal();
        String sessionToken = dataServiceInternal.tryAuthenticate(USER_A, "abc").getSessionToken();
        dataServiceInternal.listExperiments(sessionToken, "MS_SEARCH");
    }

    @Test
    public void testForDataServiceListExperimentsFailedBecauseOfAuthorization()
    {
        String sessionToken = authenticateAs(USER_A);
        try
        {
            getDataService().listExperiments(sessionToken, USER_A, "MS_SEARCH");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: ERROR: \"None of method roles "
                    + "'[INSTANCE_OBSERVER, INSTANCE_ADMIN]' "
                    + "could be found in roles of user 'USER_A'.\".", ex.getMessage());
        }
    }

    @Test
    public void testForDataServiceListExperimentsFailedBecauseOfAuthorizationOnSecondLevel()
    {
        String sessionToken = authenticateAs(USER_INSTANCE_OBSERVER);
        try
        {
            getDataService().listExperiments(sessionToken, USER_A, "MS_SEARCH");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: ERROR: \"None of method roles "
                    + "'[PROJECT_USER, PROJECT_POWER_USER, PROJECT_ADMIN, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_POWER_USER, SPACE_USER]' "
                    + "could be found in roles of user 'USER_A'.\".", ex.getMessage());
        }
    }
}
