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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
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

    private MultipartFile multipartFile;

    static final String SESSION_TOKEN = "session-token";

    protected Mockery context;

    protected IGenericServer genericServer;

    protected GenericClientService genericClientService;

    protected HttpServletRequest servletRequest;

    protected HttpSession httpSession;

    protected Session session;

    private IRequestContextProvider requestContextProvider;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        genericServer = context.mock(IGenericServer.class);
        requestContextProvider = context.mock(IRequestContextProvider.class);
        servletRequest = context.mock(HttpServletRequest.class);
        httpSession = context.mock(HttpSession.class);
        multipartFile = context.mock(MultipartFile.class);
        genericClientService = new GenericClientService(genericServer, requestContextProvider);
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

    private final static NewSample createNewSample(final String sampleIdentifier,
            final String sampleTypeCode, final List<SampleProperty> properties,
            final String parent, final String container)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier);
        final SampleType sampleType = createSampleType(sampleTypeCode);
        newSample.setSampleType(sampleType);
        newSample.setProperties(properties);
        newSample.setParentIdentifier(parent);
        newSample.setContainerIdentifier(container);
        return newSample;
    }

    private final static SampleType createSampleType(final String sampleTypeCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);
        return sampleType;
    }

    protected final void prepareGetSession(final Expectations expectations)
    {
        expectations.allowing(requestContextProvider).getHttpServletRequest();
        expectations.will(Expectations.returnValue(servletRequest));

        expectations.allowing(servletRequest).getSession(false);
        expectations.will(Expectations.returnValue(httpSession));
    }

    protected final void prepareGetSessionToken(final Expectations expectations)
    {
        prepareGetSession(expectations);

        expectations.allowing(httpSession).getAttribute(
                SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
        expectations.will(Expectations.returnValue(session));
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

                private final NewSample getTranslatedSample()
                {
                    return with(any(NewSample.class));
                }

            });
        genericClientService.registerSample(newSample);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamples() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        uploadedFilesBean.addMultipartFile(multipartFile);
        final String sessionKey = "uploaded-files";
        final NewSample newSample = new NewSample();
        newSample.setIdentifier("MP1");
        newSample.setContainerIdentifier("MP2");
        newSample.setParentIdentifier("MP3");
        context.checking(new Expectations()
            {
                {
                    prepareGetSession(this);
                    prepareGetSessionToken(this);

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    one(multipartFile).getBytes();
                    will(returnValue("identifier\tcontainer\tparent\nMP1\tMP2\tMP3".getBytes()));

                    one(genericServer).registerSamples(with(SESSION_TOKEN),
                            with(new NewSampleListMatcher(Collections.singletonList(newSample))));
                }
            });
        final SampleType sampleType = createSampleType("MASTER_PLATE");
        genericClientService.registerSamples(sampleType, sessionKey);
        context.assertIsSatisfied();
    }

    //
    // Helper classes
    //

    private final static class NewSampleListMatcher extends BaseMatcher<List<NewSample>>
    {
        private final List<NewSample> newSamples;

        NewSampleListMatcher(final List<NewSample> newSamples)
        {
            this.newSamples = newSamples;
        }

        private final boolean equals(final NewSample newSample, final NewSample thatNewSample)
        {
            return thatNewSample.equals(newSample)
                    && newSample.getContainerIdentifier().equals(
                            thatNewSample.getContainerIdentifier())
                    && newSample.getParentIdentifier().equals(thatNewSample.getParentIdentifier());
        }

        //
        // BaseMatcher
        //

        public final void describeTo(final Description description)
        {
            description.appendValue(newSamples);
        }

        @SuppressWarnings("unchecked")
        public final boolean matches(final Object item)
        {
            final List<NewSample> thisNewSamples = (List<NewSample>) item;
            if (thisNewSamples.size() == newSamples.size() == false)
            {
                return false;
            }
            int i = 0;
            for (final NewSample newSample : thisNewSamples)
            {
                final NewSample thatNewSample = newSamples.get(i++);
                if (equals(newSample, thatNewSample) == false)
                {
                    return false;
                }
            }
            return true;
        }
    }
}
