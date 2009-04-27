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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadServiceServlet.ISessionFilesSetter;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadServiceServlet.SessionFilesSetter;

/**
 * Tests for {@link UploadServiceServlet}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses =
    { UploadServiceServlet.class, ISessionFilesSetter.class, SessionFilesSetter.class })
public final class UploadServiceServletTest extends AssertJUnit
{

    private static final String SESSION_KEY_VALUE_PREFIX = "sessionKeyValue";

    private static final String SESSION_KEY_PREFIX = "sessionKey_";

    private static final String SESSION_KEYS_NUMBER = "sessionKeysNumber";

    protected Mockery context;

    protected IRequestContextProvider requestContextProvider;

    protected MultipartHttpServletRequest multipartHttpServletRequest;

    protected HttpServletResponse servletResponse;

    protected HttpSession httpSession;

    protected ISessionFilesSetter sessionFilesSetter;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        requestContextProvider = context.mock(IRequestContextProvider.class);
        multipartHttpServletRequest = context.mock(MultipartHttpServletRequest.class);
        servletResponse = context.mock(HttpServletResponse.class);
        httpSession = context.mock(HttpSession.class);
        sessionFilesSetter = context.mock(ISessionFilesSetter.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private UploadServiceServlet createServlet()
    {
        return new UploadServiceServlet(sessionFilesSetter);
    }

    private void expectSendResponse(Expectations exp)
    {
        exp.one(servletResponse).setContentType("text/html");
        exp.one(servletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    private void expectGetSession(Expectations exp)
    {
        exp.one(multipartHttpServletRequest).getSession(false);
        exp.will(Expectations.returnValue(httpSession));
    }

    @Test
    public void testFailHandleWithNoSessionKeysNumber() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    expectGetSession(this);

                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue(null));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        } catch (ServletException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailHandleWithInvalidSessionKeysNumber() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    expectGetSession(this);
                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue("notANumber"));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        } catch (NumberFormatException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailHandleWithOneSessionKeyExpectedNoneSpecified() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    expectGetSession(this);
                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue("1"));
                    one(multipartHttpServletRequest).getParameter(SESSION_KEY_PREFIX + 0);
                    will(returnValue(null));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        } catch (ServletException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailHandleWithManySessionKeysExpectedOneUnspecified() throws Exception
    {
        context.checking(new Expectations()
            {
                {

                    expectGetSession(this);

                    Integer numberOfSessionKeys = 4;
                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue(numberOfSessionKeys.toString()));

                    int numberOfUnspecifiedSessionKey = numberOfSessionKeys - 1;
                    for (int i = 0; i < numberOfUnspecifiedSessionKey; i++)
                    {
                        one(multipartHttpServletRequest).getParameter(SESSION_KEY_PREFIX + i);
                        will(returnValue(SESSION_KEY_VALUE_PREFIX + i));
                    }

                    one(multipartHttpServletRequest).getParameter(
                            SESSION_KEY_PREFIX + numberOfUnspecifiedSessionKey);
                    will(returnValue(null));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        } catch (ServletException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailHandleWithNoFilesSpecified() throws Exception
    {
        context.checking(new Expectations()
            {
                {

                    expectGetSession(this);

                    Integer numberOfSessionKeys = 5;
                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue(numberOfSessionKeys.toString()));

                    for (int i = 0; i < numberOfSessionKeys; i++)
                    {
                        String sessionKey = SESSION_KEY_VALUE_PREFIX + i;

                        one(multipartHttpServletRequest).getParameter(SESSION_KEY_PREFIX + i);
                        will(returnValue(sessionKey));

                        one(sessionFilesSetter).addFilesToSession(httpSession,
                                multipartHttpServletRequest, sessionKey);
                        will(returnValue(false));
                    }

                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testHandleWithAllFilesSpecified() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    expectGetSession(this);

                    Integer numberOfSessionKeys = 3;
                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue(numberOfSessionKeys.toString()));

                    for (int i = 0; i < numberOfSessionKeys; i++)
                    {
                        String sessionKey = SESSION_KEY_VALUE_PREFIX + i;

                        one(multipartHttpServletRequest).getParameter(SESSION_KEY_PREFIX + i);
                        will(returnValue(sessionKey));

                        one(sessionFilesSetter).addFilesToSession(httpSession,
                                multipartHttpServletRequest, sessionKey);
                        will(returnValue(true));
                    }
                    expectSendResponse(this);
                }
            });
        createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        context.assertIsSatisfied();
    }

    @Test
    public void testHandleWithLastSessionKeyWithoutFiles() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    expectGetSession(this);

                    Integer numberOfSessionKeys = 3;
                    one(multipartHttpServletRequest).getParameter(SESSION_KEYS_NUMBER);
                    will(returnValue(numberOfSessionKeys.toString()));

                    for (int i = 0; i < numberOfSessionKeys; i++)
                    {
                        String sessionKey = SESSION_KEY_VALUE_PREFIX + i;

                        one(multipartHttpServletRequest).getParameter(SESSION_KEY_PREFIX + i);
                        will(returnValue(sessionKey));

                        one(sessionFilesSetter).addFilesToSession(httpSession,
                                multipartHttpServletRequest, sessionKey);
                        will(returnValue(i != numberOfSessionKeys - 1));
                    }
                    expectSendResponse(this);
                }
            });
        createServlet().handle(multipartHttpServletRequest, servletResponse, null, null);
        context.assertIsSatisfied();
    }

}
