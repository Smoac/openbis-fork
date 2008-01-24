/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server;

import static org.testng.AssertJUnit.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.server.business.DomainModel;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * @author Franz-Josef Elmer
 */
public class CIFEXServiceImplTest
{
    private Mockery context;

    private IDAOFactory daoFactory;

    private IMailClient mailClient;

    private IRequestContextProvider requestContextProvider;

    private IAuthenticationService authenticationService;

    private HttpSession httpSession;

    private HttpServletRequest httpServletRequest;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        mailClient = context.mock(IMailClient.class);
        requestContextProvider = context.mock(IRequestContextProvider.class);
        authenticationService = context.mock(IAuthenticationService.class);
        httpSession = context.mock(HttpSession.class);
        httpServletRequest = context.mock(HttpServletRequest.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testIsAuthenticated()
    {
        prepareForGettingUserFromHTTPSession(new UserDTO());

        ICIFEXService service = createService(null);
        assertEquals(true, service.isAuthenticated());

        context.assertIsSatisfied();
    }

    @Test
    public void testIsNotAuthenticated()
    {
        prepareForGettingUserFromHTTPSession(null);

        ICIFEXService service = createService(null);
        assertEquals(false, service.isAuthenticated());

        context.assertIsSatisfied();
    }

    private void prepareForGettingUserFromHTTPSession(final UserDTO userDTO)
    {
        context.checking(new Expectations()
            {
                {
                    one(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));

                    one(httpServletRequest).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute(CIFEXServiceImpl.SESSION_NAME);
                    will(returnValue(userDTO));
                }
            });
    }

    private ICIFEXService createService(IAuthenticationService aService)
    {
        DomainModel domainModel = new DomainModel(daoFactory, mailClient);
        return new CIFEXServiceImpl(domainModel, requestContextProvider, aService);
    }
}
