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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link GenericClientService} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = GenericClientService.class)
public final class GenericClientServiceTest
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IGenericServer genericServer;

    private IRequestContextProvider requestContextProvider;

    private GenericClientService genericClientService;

    private HttpServletRequest servletRequest;

    private HttpSession httpSession;

    private Session session;

    private final static NewSample createNewSample(final String sampleIdentifier,
            final String type, final List<SampleProperty> properties, final String parent,
            final String container)
    {
        final NewSample newSample = new NewSample();
        newSample.setSampleIdentifier(sampleIdentifier);
        newSample.setSampleTypeCode(type);
        newSample.setProperties(properties);
        newSample.setParent(parent);
        newSample.setContainer(container);
        return newSample;
    }

    private final void prepareGetSession(final Expectations expectations)
    {
        expectations.one(requestContextProvider).getHttpServletRequest();
        expectations.will(Expectations.returnValue(servletRequest));

        expectations.one(servletRequest).getSession(false);
        expectations.will(Expectations.returnValue(httpSession));
    }

    private void prepareGetSessionToken(final Expectations expectations)
    {
        prepareGetSession(expectations);

        expectations.one(httpSession).getAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
        expectations.will(Expectations.returnValue(session));
    }

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public final void setUp()
    {
        context = new Mockery();
        genericServer = context.mock(IGenericServer.class);
        requestContextProvider = context.mock(IRequestContextProvider.class);
        servletRequest = context.mock(HttpServletRequest.class);
        httpSession = context.mock(HttpSession.class);
        genericClientService = new GenericClientService(genericServer, requestContextProvider);
        session = createSessionMock();
    }

    private Session createSessionMock()
    {
        return new Session("user", SESSION_TOKEN, new Principal("user", "FirstName", "LastName",
                "email@users.ch"), "remote-host", System.currentTimeMillis() - 1);
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSample()
    {
        final NewSample newSample =
                createNewSample("/group1/sample1", "MASTER_PLATE", new ArrayList<SampleProperty>(),
                        null, null);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    one(genericServer).registerSample(with(SESSION_TOKEN), getTranslatedSample());
                }

                @SuppressWarnings(
                    { "unchecked" })
                private final NewSample getTranslatedSample()
                {
                    return with(any(NewSample.class));
                }

            });
        genericClientService.registerSample(newSample);
        context.assertIsSatisfied();
    }

}
