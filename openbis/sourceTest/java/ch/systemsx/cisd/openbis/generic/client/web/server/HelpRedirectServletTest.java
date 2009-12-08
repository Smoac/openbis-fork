/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Friend(toClasses = HelpRedirectServlet.class)
public class HelpRedirectServletTest
{

    static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private HttpServletRequest servletRequest;

    // TODO: Not sure how best to test the session
    // private HttpSession httpSession;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        servletRequest = context.mock(HttpServletRequest.class);
        // httpSession = context.mock(HttpSession.class);
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testRedirectHelpURL() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageDomain.DATA_SET.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageAction.BROWSE.toString()));
                }
            });
        String helpPageTitle = createServlet().tryGetHelpPageTitleForRequest(servletRequest);
        AssertJUnit.assertEquals("DATA+SET+BROWSE", helpPageTitle);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRedirectHelpURLWithoutAction() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageDomain.EXPERIMENT.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(null));
                }
            });
        String helpPageTitle = createServlet().tryGetHelpPageTitleForRequest(servletRequest);
        AssertJUnit.assertEquals("EXPERIMENT", helpPageTitle);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRedirectHelpURLWithoutParameters() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(null));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(null));
                }
            });
        String helpPageTitle = createServlet().tryGetHelpPageTitleForRequest(servletRequest);
        AssertJUnit.assertEquals(null, helpPageTitle);
        context.assertIsSatisfied();
    }

    private HelpRedirectServlet createServlet()
    {
        return new HelpRedirectServlet();
    }
}
