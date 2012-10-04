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

package ch.systemsx.cisd.cifex.rpc.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClient;
import ch.systemsx.cisd.cifex.server.AbstractCIFEXService;
import ch.systemsx.cisd.cifex.server.AbstractFileUploadDownloadServlet;
import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.string.Template;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class File2GBDownloadServletTest extends AssertJUnit
{
    private static final String SCHEME = "http";

    private static final String HOST = "server";

    private static final int PORT = 8080;

    private static final String CONTEXT_PATH = "/cifex";

    private static final String BASE_URL = SCHEME + "://" + HOST + ":" + PORT + CONTEXT_PATH;

    private static final String SESSION_ID = "session-id";

    private static final UserDTO USER = createUser("Einstein");

    private static final class MockServletOutputStream extends ServletOutputStream
    {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException
        {
            outputStream.write(b);
        }

        @Override
        public String toString()
        {
            return outputStream.toString();
        }
    }

    private static UserDTO createUser(String userID)
    {
        UserDTO user = new UserDTO();
        user.setUserCode(userID);
        user.setEmail(userID + "@users.org");
        return user;
    }

    private Mockery context;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HttpSession httpSession;

    private IExtendedCIFEXRPCService uploadService;

    private IDomainModel domainModel;

    private IBusinessContext businessContext;

    private MockServletOutputStream outputStream;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        httpSession = context.mock(HttpSession.class);

        uploadService = context.mock(IExtendedCIFEXRPCService.class);
        domainModel = context.mock(IDomainModel.class);
        businessContext = context.mock(IBusinessContext.class);
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));

                    allowing(businessContext).getOverrideURL();
                    will(returnValue(null));
                }
            });
        outputStream = new MockServletOutputStream();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testJNLPFile() throws Exception
    {
        prepareRequest(USER);
        prepareResponse();
        context.checking(new Expectations()
            {
                {
                    one(uploadService).createSession(USER, BASE_URL);
                    will(returnValue(SESSION_ID));
                }
            });

        createServlet().doGet(request, response);

        Template template = AbstractFileUploadDownloadServlet.JNLP_TEMPLATE.createFreshCopy();
        template.bind("title", "CIFEX File Downloader");
        template.bind("description", "CIFEX File Downloader");
        template.bind("base-URL", BASE_URL + "/");
        template.bind("main-class", FileDownloadClient.class.getName());
        template.bind("service-URL", BASE_URL + "/cifex/rpc-service");
        template.bind("session-id", SESSION_ID);
        assertEquals(template.createText(false), outputStream.toString());

        context.assertIsSatisfied();
    }

    private void prepareRequest(final UserDTO user)
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute(AbstractCIFEXService.SESSION_ATTRIBUTE_USER_NAME);
                    will(returnValue(user));

                    allowing(request).getScheme();
                    will(returnValue(SCHEME));

                    allowing(request).getServerName();
                    will(returnValue(HOST));

                    allowing(request).getServerPort();
                    will(returnValue(PORT));

                    allowing(request).getContextPath();
                    will(returnValue(CONTEXT_PATH));
                }
            });
    }

    private void prepareResponse()
    {
        context.checking(new Expectations()
            {
                {
                    one(response).setContentType("application/x-java-jnlp-file");
                    try
                    {
                        one(response).getOutputStream();
                        will(returnValue(outputStream));
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
    }

    private File2GBDownloadServlet createServlet()
    {
        return new File2GBDownloadServlet(uploadService, domainModel);
    }
}
