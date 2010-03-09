/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.dss.info;

import java.io.File;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaDataSetInfoExtractorTest extends AssertJUnit
{
    Mockery context;

    IEncapsulatedOpenBISService openbisService;

    CinaDataSetInfoExtractor extractor;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        extractor = new CinaDataSetInfoExtractor(new Properties());
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExperiment()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(with(any(ExperimentIdentifier.class)));
                    will(returnValue(null));
                    one(openbisService).registerExperiment(with(any(NewExperiment.class)));
                }
            });

        File experimentFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/experiment-data-folder");
        DataSetInformation dataSetInformation =
                extractor.getDataSetInformation(experimentFolder, openbisService);

        assertTrue("no-one@nowhere.ch".equals(dataSetInformation.tryGetUploadingUserEmail()));
    }

    @Test
    public void testRegisterSample()
    {
        final Experiment existingExperiment = new Experiment();
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(with(any(ExperimentIdentifier.class)));
                    will(returnValue(existingExperiment));
                    one(openbisService).tryGetSampleWithExperiment(
                            with(any(SampleIdentifier.class)));
                    will(returnValue(null));
                    one(openbisService).registerSample(with(any(NewSample.class)),
                            with(aNull(String.class)));
                }
            });

        File sampleFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/sample-data-folder");
        DataSetInformation dataSetInformation =
                extractor.getDataSetInformation(sampleFolder, openbisService);

        assertTrue("no-one@nowhere.ch".equals(dataSetInformation.tryGetUploadingUserEmail()));
    }

    @Test
    public void testRegisterAmbiguousFolder()
    {
        File ambiguousDataFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/ambiguous-data-folder");

        try
        {
            extractor.getDataSetInformation(ambiguousDataFolder, openbisService);
            fail("An ambiguous data folder should result in a UserFailureException.");
        } catch (UserFailureException ex)
        {

        }
    }

    @Test
    public void testRegisterEmptyFolder()
    {
        File emptyDataFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/empty-data-folder");
        extractor.getDataSetInformation(emptyDataFolder, openbisService);
    }
}
