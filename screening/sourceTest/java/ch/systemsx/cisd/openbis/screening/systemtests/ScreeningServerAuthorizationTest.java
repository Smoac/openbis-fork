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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ScreeningServerAuthorizationTest extends AbstractScreeningSystemTestCase
{
    private static final String TEST_USER = "test-user";
    private static final String SPACE_CODE = "CISD";
    
    private ICommonServerForInternalUse commonServer;
    private IScreeningServer screeningServer;
    private IScreeningApiServer screeningApiServer;
    private String userSessionToken;

    @BeforeMethod
    public void setUp()
    {
        commonServer =
                (ICommonServerForInternalUse) applicationContext
                        .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        Object serverBean = applicationContext
                .getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        screeningServer = (IScreeningServer) serverBean;
        screeningApiServer = (IScreeningApiServer) serverBean;
        String sessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
        if (hasSpace(sessionToken, SPACE_CODE) == false)
        {
            commonServer.registerSpace(sessionToken, SPACE_CODE, null);
        }
        if (hasPerson(sessionToken, TEST_USER) == false)
        {
            commonServer.registerPerson(sessionToken, TEST_USER);
            Grantee grantee = Grantee.createPerson(TEST_USER);
            commonServer.registerSpaceRole(sessionToken, RoleCode.OBSERVER, new SpaceIdentifier(
                    SPACE_CODE), grantee);
        }
        userSessionToken = commonServer.tryToAuthenticate(TEST_USER, "abc").getSessionToken();
    }

    private boolean hasSpace(String sessionToken, String spaceCode)
    {
        List<Space> spaces =
                commonServer.listSpaces(sessionToken, DatabaseInstanceIdentifier.HOME_INSTANCE);
        for (Space space : spaces)
        {
            if (space.getCode().equals(spaceCode))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasPerson(String sessionToken, String personID)
    {
        List<Person> persons = commonServer.listPersons(sessionToken);
        for (Person person : persons)
        {
            if (person.getUserId().equals(personID))
            {
                return true;
            }
        }
        return false;
    }
    
    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testSetSessionUserFailsBecauseOfNonAuthorized()
    {
        screeningServer.setSessionUser(userSessionToken, "system");
    }
    
    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testListPlatesFailsBecauseOfAuthorization()
    {
        screeningApiServer.listPlates(userSessionToken, new ExperimentIdentifier("a", "b", "c", "d"));
    }
    
    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testRegisterLibraryFailsBecauseOfNonAuthorized()
    {
        screeningServer.registerLibrary(userSessionToken, "",
                Collections.<NewMaterial> emptyList(), Collections.<NewMaterial> emptyList(),
                Collections.<NewSamplesWithTypes> emptyList());
    }
    
}
