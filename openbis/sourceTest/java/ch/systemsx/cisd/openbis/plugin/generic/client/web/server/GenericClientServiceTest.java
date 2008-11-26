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

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleToRegister;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DefaultResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

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

    private IResultSetManager<String> resultSetManager;

    private Session session;

    private final static ListSampleCriteria createListCriteria()
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        final SampleType sampleType = createSampleType("MASTER_PLATE", "DB1");
        criteria.setSampleType(sampleType);
        return criteria;
    }

    private final static SampleType createSampleType(String code, String dbCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        final DatabaseInstance databaseInstance = new DatabaseInstance();
        databaseInstance.setCode(dbCode);
        sampleType.setDatabaseInstance(databaseInstance);
        return sampleType;
    }

    private final static SampleToRegister createSampleToRegister(String sampleIdentifier,
            String type, List<SampleProperty> properties, String generatorParent,
            String containerParent)
    {
        SampleToRegister s = new SampleToRegister();
        s.setSampleIdentifier(sampleIdentifier);
        s.setType(type);
        s.setProperties(properties);
        s.setGeneratorParent(generatorParent);
        s.setContainerParent(containerParent);
        return s;
    }

    private final void prepareGetSession(final Expectations expectations)
    {
        expectations.one(requestContextProvider).getHttpServletRequest();
        expectations.will(Expectations.returnValue(servletRequest));

        expectations.one(servletRequest).getSession(false);
        expectations.will(Expectations.returnValue(httpSession));
    }

    private void prepareGetSessionToken(Expectations expectations)
    {
        prepareGetSession(expectations);

        expectations.one(httpSession).getAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
        expectations.will(Expectations.returnValue(session));
    }

    private final void prepareGetResultSetManager(final Expectations expectations)
    {
        expectations.one(httpSession).getAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
        expectations.will(Expectations.returnValue(resultSetManager));
    }

    private final static List<Sample> createSampleList()
    {
        return Collections.emptyList();
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
        resultSetManager = context.mock(IResultSetManager.class);
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
    public final void testListSamples()
    {
        final String resultSetKey = "1";
        final DefaultResultSet<String, Sample> defaultResultSet =
                new DefaultResultSet<String, Sample>(resultSetKey, createSampleList(), 0);
        final ListSampleCriteria listCriteria = createListCriteria();
        context.checking(new Expectations()
            {
                {
                    prepareGetSession(this);
                    prepareGetResultSetManager(this);

                    one(resultSetManager).getResultSet(with(listCriteria),
                            getOriginalDataProvider());
                    will(returnValue(defaultResultSet));
                }

                @SuppressWarnings("unchecked")
                private final IOriginalDataProvider<Sample> getOriginalDataProvider()
                {
                    return with(any(IOriginalDataProvider.class));
                }

            });
        final ResultSet<Sample> resultSet = genericClientService.listSamples(listCriteria);
        assertEquals(0, resultSet.getList().size());
        assertEquals(resultSetKey, resultSet.getResultSetKey());
        assertEquals(0, resultSet.getTotalLength());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSample()
    {
        SampleToRegister newSample =
                createSampleToRegister("/group1/sample1", "MASTER_PLATE",
                        new ArrayList<SampleProperty>(), null, null);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    one(genericServer).registerSample(with(SESSION_TOKEN), getTranslatedSample());
                }

                @SuppressWarnings(
                    { "unchecked" })
                private final SampleToRegisterDTO getTranslatedSample()
                {
                    return with(any(SampleToRegisterDTO.class));
                }

            });
        genericClientService.registerSample(newSample);
        context.assertIsSatisfied();
    }

}
