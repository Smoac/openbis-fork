/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.client.util.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaUtilitiesFacadeTest extends AssertJUnit
{
    private final static String USER_ID = "userid";

    private final static String PASSWORD = "password";

    private final static String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private IGeneralInformationService service;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        final SearchCriteria searchCriteria = new SearchCriteria();
        context.checking(new Expectations()
            {
                {
                    final ArrayList<Sample> samples = new ArrayList<Sample>();

                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).searchForSamples(SESSION_TOKEN, searchCriteria);
                    will(returnValue(samples));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        CinaUtilitiesFacade facade = createFacade(service, USER_ID, PASSWORD);
        assertEquals(facade.getSessionToken(), SESSION_TOKEN);
        List<Sample> result = facade.searchForSamples(searchCriteria);
        assertEquals(0, result.size());
        facade.logout();
        context.assertIsSatisfied();
    }

    /**
     * Utility method to create a CinaUtilitiesFacade object for testing.
     */
    public static CinaUtilitiesFacade createFacade(IGeneralInformationService service,
            String userId, String password)
    {
        CinaUtilitiesFacade facade = new CinaUtilitiesFacade(service, null);
        facade.login(userId, password);
        return facade;
    }
}
