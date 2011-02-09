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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.server.openbisauth.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link EncapsulatedOpenBISService} class.
 * 
 * @author Basil Neff
 */
public class EncapsulatedOpenBISServiceTest
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IETLLIMSService limsService;

    private EncapsulatedOpenBISService encapsulatedLimsService;

    private OpenBISSessionHolder session;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        limsService = context.mock(IETLLIMSService.class);
        session = new OpenBISSessionHolder();
        session.setToken(SESSION_TOKEN);
        encapsulatedLimsService = new EncapsulatedOpenBISService(limsService, session);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetBaseExperiment()
    {
        final DataSetInformation dataSetInformation = createDataSetInformation();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(SESSION_TOKEN,
                            dataSetInformation.getSampleIdentifier());
                }
            });
        encapsulatedLimsService
                .tryGetSampleWithExperiment(dataSetInformation.getSampleIdentifier());
    }

    @Test
    public final void testRegisterDataSet()
    {
        final DataSetInformation dataSetInfo = createDataSetInformation();
        final NewExternalData data = new NewExternalData();
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet(SESSION_TOKEN,
                            dataSetInfo.getSampleIdentifier(), data);
                }
            });
        encapsulatedLimsService.registerDataSet(dataSetInfo, data);
    }

    @Test
    public final void testIsSampleRegisteredForDataSet()
    {
        final SampleIdentifier sampleIdentifier = SampleIdentifier.createHomeGroup("");
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                            sampleIdentifier);
                }
            });
        encapsulatedLimsService.getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
    }

    private final DataSetInformation createDataSetInformation()
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setSampleCode("S1");
        return dataSetInformation;
    }
}
