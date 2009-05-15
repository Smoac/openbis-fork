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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractFileDownloadServlet.FileContent;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link AttachmentDownloadServlet} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = AttachmentDownloadServlet.class)
public final class AttachmentDownloadServletTest
{
    static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IGenericServer genericServer;

    private HttpServletRequest servletRequest;

    private HttpSession httpSession;

    protected Session session;

    private AttachmentDownloadServlet createServlet()
    {
        return new AttachmentDownloadServlet(genericServer);
    }

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        genericServer = context.mock(IGenericServer.class);
        servletRequest = context.mock(HttpServletRequest.class);
        httpSession = context.mock(HttpSession.class);
        session = createSessionMock();
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    protected final Session createSessionMock()
    {
        return new Session("user", SESSION_TOKEN, new Principal("user", "FirstName", "LastName",
                "email@users.ch"), "remote-host", System.currentTimeMillis() - 1);
    }

    @Test
    public final void testListSamples() throws Exception
    {

        final AttachmentPE attachment = CommonTestUtils.createAttachment();
        context.checking(new Expectations()
            {
                {
                    one(servletRequest).getParameter(GenericConstants.VERSION_PARAMETER);
                    will(returnValue(CommonTestUtils.VERSION_22 + ""));
                    one(servletRequest).getParameter(GenericConstants.FILE_NAME_PARAMETER);
                    will(returnValue(CommonTestUtils.FILENAME));
                    one(servletRequest).getParameter(GenericConstants.ATTACHMENT_HOLDER_PARAMETER);
                    will(returnValue(AttachmentHolderKind.EXPERIMENT.name()));
                    one(servletRequest).getParameter(GenericConstants.TECH_ID_PARAMETER);
                    will(returnValue(CommonTestUtils.DUMMY_TECH_ID.toString()));

                    one(servletRequest).getSession(false);
                    will(Expectations.returnValue(httpSession));

                    one(httpSession).getAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
                    will(Expectations.returnValue(session));

                    one(genericServer).getExperimentFileAttachment(session.getSessionToken(),
                            CommonTestUtils.DUMMY_TECH_ID, CommonTestUtils.FILENAME,
                            CommonTestUtils.VERSION_22);
                    will(returnValue(attachment));

                }
            });
        FileContent fileContent = createServlet().getFileContent(servletRequest);
        AssertJUnit.assertEquals(attachment.getFileName(), fileContent.getFileName());
        AssertJUnit.assertEquals(attachment.getAttachmentContent().getValue(), fileContent
                .getContent());
        context.assertIsSatisfied();
    }
}
