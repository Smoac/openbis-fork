/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.crowd;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Date;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.TestAppender;

/**
 * Tests for {@link CrowdAuthenticationService}.
 * 
 * @author Franz-Josef Elmer
 */
public class CrowdAuthenticationServiceTest
{

    private static final String URL = "url";

    private static final String APPLICATION = "appli & cation";

    private static final String APPLICATION_ESCAPED = "appli &amp; cation";

    private static final String APPLICATION_PASSWORD = "<password>";

    private static final String APPLICATION_PASSWORD_ESCAPED = "&lt;password&gt;";

    private static final String APPLICATION_TOKEN = "application<&>token";

    private static final String APPLICATION_TOKEN_ESACPED = "application&lt;&amp;&gt;token";

    private static final String USER = "<user>";

    private static final String USER_ESCAPED = "&lt;user&gt;";

    private static final String USER_PASSWORD = "pass\"word\"";

    private static final String USER_PASSWORD_ESCAPED = "pass&quot;word&quot;";

    private Mockery context;

    private IRequestExecutor executor;

    private IAuthenticationService authenticationService;

    private TestAppender logRecorder;

    @BeforeMethod
    public void setup()
    {
        context = new Mockery();
        executor = context.mock(IRequestExecutor.class);
        authenticationService = new CrowdAuthenticationService(URL, APPLICATION, APPLICATION_PASSWORD, executor);
        logRecorder = new TestAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfullApplicationAuthentication()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                        { APPLICATION_ESCAPED, APPLICATION_PASSWORD_ESCAPED };
                    String message = CrowdAuthenticationService.AUTHENTICATE_APPL.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement(CrowdSoapElements.TOKEN, APPLICATION_TOKEN_ESACPED)));
                }

            });
        String result = authenticationService.authenticateApplication();
        assertEquals(APPLICATION_TOKEN, result);
        assertEquals(createDebugLogEntry("?CROWD: application '" + APPLICATION + "' successfully authenticated."),
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedApplicationAuthentication()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                        { APPLICATION_ESCAPED, APPLICATION_PASSWORD_ESCAPED };
                    String message = CrowdAuthenticationService.AUTHENTICATE_APPL.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue("error"));
                }
            });
        String result = authenticationService.authenticateApplication();
        assertEquals(null, result);
        assertEquals(createDebugLogEntry("Element '" + CrowdSoapElements.TOKEN + "' could not be found in 'error'.")
                + OSUtilities.LINE_SEPARATOR
                + createErrorLogEntry("CROWD: application '" + APPLICATION + "' failed to authenticate."), logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfullUserAuthentication()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                        { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED, USER_PASSWORD_ESCAPED };
                    String message = CrowdAuthenticationService.AUTHENTICATE_USER.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement(CrowdSoapElements.OUT, APPLICATION_TOKEN_ESACPED)));
                }
            });
        boolean result = authenticationService.authenticateUser(APPLICATION_TOKEN, USER, USER_PASSWORD);
        assertEquals(true, result);
        assertEquals(createInfoLogEntry("CROWD: authentication of user '" + USER + "', application '" + APPLICATION
                + "': SUCCESS."), logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedUserAuthentication()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                        { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED, USER_PASSWORD_ESCAPED };
                    String message = CrowdAuthenticationService.AUTHENTICATE_USER.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue("error"));
                }
            });
        boolean result = authenticationService.authenticateUser(APPLICATION_TOKEN, USER, USER_PASSWORD);
        assertEquals(false, result);
        assertEquals(createDebugLogEntry("Element '" + CrowdSoapElements.OUT + "' could not be found in 'error'.")
                + OSUtilities.LINE_SEPARATOR
                + createInfoLogEntry("CROWD: authentication of user '" + USER + "', application '" + APPLICATION
                        + "': FAILED."), logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfullPrincipalRetrieval()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                        { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED };
                    String message = CrowdAuthenticationService.FIND_PRINCIPAL_BY_NAME.format(parameters);
                    one(executor).execute(URL, message);
                    String element = createSOAPAttribute("sn", "Stepka");
                    element += createSOAPAttribute("invalidPasswordAttempts", "0");
                    element += createSOAPAttribute("requiresPasswordChange", "false");
                    element += createSOAPAttribute("mail", "justen.stepka@atlassian.com");
                    element += createSOAPAttribute("lastAuthenticated", "1169440408520");
                    element += createSOAPAttribute("givenName", "Justen");
                    element += createSOAPAttribute("passwordLastChanged", "1168995491407");
                    will(returnValue("<a>" + element + "</a>"));
                }

                private String createSOAPAttribute(String name, String value)
                {
                    return "<SOAPAttribute><name>" + name
                            + "</name><values><ns1:string xmlns:ns1=\"urn:SecurityServer\">" + value
                            + "</ns1:string></values></SOAPAttribute>";
                }
            });
        Principal result = authenticationService.getPrincipal(APPLICATION_TOKEN, USER);
        assertEquals("Justen", result.getFirstName());
        assertEquals("Stepka", result.getLastName());
        assertEquals("justen.stepka@atlassian.com", result.getEmail());
        assertEquals(new Integer(0), result.getProperty("invalidPasswordAttempts"));
        assertEquals(Boolean.FALSE, result.getProperty("requiresPasswordChange"));
        assertEquals(new Date(1169440408520L), result.getProperty("lastAuthenticated"));
        assertEquals(new Date(1168995491407L), result.getProperty("passwordLastChanged"));
        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedPrincipalRetrieval()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                        { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED };
                    String message = CrowdAuthenticationService.FIND_PRINCIPAL_BY_NAME.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue("<a></a>"));
                }
            });
        try
        {
            authenticationService.getPrincipal(APPLICATION_TOKEN, USER);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("CROWD: Principal information for user '" + USER + "' could not be obtained.", e.getMessage());
        }

        assertEquals(createDebugLogEntry("No SOAPAttribute element could be found in the SOAP XML response."),
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    private String createDebugLogEntry(String message)
    {
        return createLogEntry("DEBUG", message);
    }

    private String createInfoLogEntry(String message)
    {
        return createLogEntry("INFO ", message);
    }

    private String createErrorLogEntry(String message)
    {
        return createLogEntry("ERROR", message);
    }

    private String createLogEntry(String level, String message)
    {
        return level + " OPERATION." + authenticationService.getClass().getName() + " - " + message;
    }

    private String createXMLElement(String element, String content)
    {
        return "<" + element + ">" + content + "</" + element + ">";
    }
}
