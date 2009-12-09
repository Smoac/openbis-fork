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
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp.IOnlineHelpResourceLocatorService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Friend(toClasses = HelpRedirectServlet.class)
public class HelpRedirectServletTest
{

    static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private HttpServletRequest servletRequest;

    private HttpServletResponse servletResponse;

    private IOnlineHelpResourceLocatorService service;

    /**
     * Internal helper method to mock the the IOnlineHelpResourceLocatorService
     */
    private void prepareServiceExpectations(Expectations exps)
    {
        exps.allowing(service).getOnlineHelpGenericRootURL();
        exps.will(Expectations.returnValue("https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp"));
        exps.allowing(service).getOnlineHelpGenericPageTemplate();
        exps
                .will(Expectations
                        .returnValue("https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title}&linkCreation=true&fromPageId=40633829"));
        exps.allowing(service).getOnlineHelpSpecificRootURL();
        exps.will(Expectations.returnValue("https://irgendwo.li/display/OurDoc/OnlineHelp"));
        exps.allowing(service).getOnlineHelpSpecificPageTemplate();
        exps
                .will(Expectations
                        .returnValue("https://irgendwo.li/pages/createpage.action?spaceKey=OurDoc&title=${title}&linkCreation=true&fromPageId=40633829"));
    }

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        servletRequest = context.mock(HttpServletRequest.class);
        servletResponse = context.mock(HttpServletResponse.class);
        service = context.mock(IOnlineHelpResourceLocatorService.class);
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testGenericHelpTitle() throws Exception
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
    public final void testGenericHelpTitleWithoutAction() throws Exception
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
    public final void testGenericHelpTitleWithoutParameters() throws Exception
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

    @Test
    public final void testGenericHelpPageRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageDomain.EXPERIMENT.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageAction.BROWSE.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue(null));
                    oneOf(servletResponse)
                            .sendRedirect(
                                    with(equal("https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=EXPERIMENT+BROWSE&linkCreation=true&fromPageId=40633829")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testGenericHelpRootRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(null));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(null));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("false"));
                    oneOf(servletResponse).sendRedirect(
                            with(equal("https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSpecificHelpPageRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageDomain.EXPERIMENT.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageAction.BROWSE.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("TRUE"));
                    oneOf(servletResponse)
                            .sendRedirect(
                                    with(equal("https://irgendwo.li/pages/createpage.action?spaceKey=OurDoc&title=EXPERIMENT+BROWSE&linkCreation=true&fromPageId=40633829")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSpecificHelpRootRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(null));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue(null));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("true"));
                    oneOf(servletResponse).sendRedirect(
                            with(equal("https://irgendwo.li/display/OurDoc/OnlineHelp")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testInvalidGenericHelpRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue("junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue("more junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("even more junk"));
                    oneOf(servletResponse).sendRedirect(
                            with(equal("https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testPartiallyInvalidGenericHelpRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue(HelpPageIdentifier.HelpPageDomain.AUTHORIZATION.toString()));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue("junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("even more junk"));
                    oneOf(servletResponse)
                            .sendRedirect(
                                    with(equal("https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=AUTHORIZATION&linkCreation=true&fromPageId=40633829")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testInvalidSpecificHelpRequest() throws Exception
    {
        // An junk request should redirect to the help root
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue("junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue("more junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("true"));
                    oneOf(servletResponse).sendRedirect(
                            with(equal("https://irgendwo.li/display/OurDoc/OnlineHelp")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSpecificHelpRequestWithoutProperty() throws Exception
    {
        // An junk request should redirect to the help root
        context.checking(new Expectations()
            {
                {
                    allowing(service).getOnlineHelpGenericRootURL();
                    will(returnValue("https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp"));
                    allowing(service).getOnlineHelpGenericPageTemplate();
                    will(returnValue("https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title}&linkCreation=true&fromPageId=40633829"));
                    allowing(service).getOnlineHelpSpecificRootURL();
                    will(returnValue(""));
                    allowing(service).getOnlineHelpSpecificPageTemplate();
                    will(returnValue(""));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
                    will(returnValue("junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_ACTION_KEY);
                    will(returnValue("more junk"));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("true"));
                    oneOf(servletResponse).sendRedirect(
                            with(equal("https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp")));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    private final HelpRedirectServlet createServlet()
    {
        return new HelpRedirectServlet(service);
    }
}
