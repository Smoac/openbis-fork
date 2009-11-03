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

package ch.systemsx.cisd.openbis.dss.generic.server;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PluginUtilTest;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link EncapsulatedOpenBISService} class.
 * 
 * @author Basil Neff
 */
public class EncapsulatedOpenBISServiceTest
{
    private static final int PORT = 4711;

    private static final String DOWNLOAD_URL = "download-url";

    private static final String DATA_STORE_CODE = "data-store1";

    private Mockery context;

    private IETLLIMSService limsService;

    private EncapsulatedOpenBISService encapsulatedLimsService;

    private static final String LIMS_USER = "testuser";

    private static final String LIMS_PASSWORD = "testpassword";

    private final DataSetInformation createDataSetInformation()
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setSampleCode("S1");
        return dataSetInformation;
    }

    private void prepareCallGetBaseExperiment(final Expectations exp,
            final DataSetInformation dataSetInformation)
    {
        exp.one(limsService).tryToAuthenticate(LIMS_USER, LIMS_PASSWORD);
        exp.will(Expectations.returnValue(createSession()));
        exp.one(limsService).registerDataStoreServer(exp.with(Expectations.equal("")),
                exp.with(Expectations.any(DataStoreServerInfo.class)));
        exp.one(limsService).tryGetSampleWithExperiment("",
                dataSetInformation.getSampleIdentifier());
    }

    private SessionContextDTO createSession()
    {
        SessionContextDTO session = new SessionContextDTO();
        session.setSessionToken("");
        return session;
    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        limsService = context.mock(IETLLIMSService.class);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryToAuthenticate(LIMS_USER, LIMS_PASSWORD);
                    will(returnValue(createSession()));

                    one(limsService).registerDataStoreServer(with(equal("")),
                            with(new BaseMatcher<DataStoreServerInfo>()
                                {

                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataStoreServerInfo)
                                        {
                                            DataStoreServerInfo info = (DataStoreServerInfo) item;
                                            return DATA_STORE_CODE.equals(info.getDataStoreCode())
                                                    && DOWNLOAD_URL.equals(info.getDownloadUrl())
                                                    && PORT == info.getPort()
                                                    && info.getServicesDescriptions()
                                                            .getProcessingServiceDescriptions()
                                                            .size() == 0
                                                    && info.getServicesDescriptions()
                                                            .getReportingServiceDescriptions()
                                                            .size() == 0;
                                        }
                                        return false;
                                    }

                                    public void describeTo(Description description)
                                    {
                                    }

                                }));
                }
            });
        encapsulatedLimsService =
                new EncapsulatedOpenBISService(new SessionTokenManager(), limsService,
                        PluginUtilTest.createPluginTaskProviders());
        encapsulatedLimsService.setUsername(LIMS_USER);
        encapsulatedLimsService.setPassword(LIMS_PASSWORD);
        encapsulatedLimsService.setDataStoreCode(DATA_STORE_CODE);
        encapsulatedLimsService.setDownloadUrl(DOWNLOAD_URL);
        encapsulatedLimsService.setPort(PORT);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetBaseExperimentReauthentificate()
    {
        final DataSetInformation dataSetInformation = createDataSetInformation();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment("",
                            dataSetInformation.getSampleIdentifier());
                    will(throwException(new InvalidSessionException("error")));
                    prepareCallGetBaseExperiment(this, dataSetInformation);
                }
            });
        encapsulatedLimsService
                .tryGetSampleWithExperiment(dataSetInformation.getSampleIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetBaseExperiment()
    {
        final DataSetInformation dataSetInformation = createDataSetInformation();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment("",
                            dataSetInformation.getSampleIdentifier());
                }
            });
        encapsulatedLimsService
                .tryGetSampleWithExperiment(dataSetInformation.getSampleIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterDataSet()
    {
        final DataSetInformation dataSetInfo = createDataSetInformation();
        final NewExternalData data = new NewExternalData();
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet("", dataSetInfo.getSampleIdentifier(), data);
                }
            });
        encapsulatedLimsService.registerDataSet(dataSetInfo, data);

        context.assertIsSatisfied();
    }

    @Test
    public final void testIsSampleRegisteredForDataSet()
    {
        final SampleIdentifier sampleIdentifier = SampleIdentifier.createHomeGroup("");
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryToGetPropertiesOfTopSampleRegisteredFor("",
                            sampleIdentifier);
                }
            });
        encapsulatedLimsService.getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
        context.assertIsSatisfied();
    }
}
