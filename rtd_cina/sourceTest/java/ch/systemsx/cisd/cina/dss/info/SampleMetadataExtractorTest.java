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

import java.util.HashMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleMetadataExtractorTest extends AssertJUnit
{
    Mockery context;

    DataSetInformation dataSetInformation;

    IEncapsulatedOpenBISService openbisService;

    @BeforeMethod
    public void beforeMethod()
    {
        dataSetInformation = new DataSetInformation();
        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    /**
     * Creates dummy metadata and invokes the SampleMetadataExtrator.
     */
    @Test
    public void testSuccessfulSampleRegistration()
    {
        final String experimentId = "/CINA/CINA1/EXP1";
        final String sampleCodePrefix = "S";
        final String sampleCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("experiment.identifier", experimentId);
        sampleMetadata.put("sample.code-prefix", sampleCodePrefix);
        sampleMetadata.put("experiment.owner-email", ownerEmail);

        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentId).createIdentifier();
        final Experiment experiment = new Experiment();
        experiment.setIdentifier(experimentIdentifier.toString());

        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new GroupIdentifier((String) null, "CINA"), sampleCodePrefix
                        + "-" + sampleCodeSuffix);

        final NewSample newSample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode("CINA_SAMPLE_TYPE");
        newSample.setSampleType(sampleType);
        newSample.setExperimentIdentifier(experimentId.toString());
        newSample.setIdentifier(sampleIdentifier.toString());
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(experimentIdentifier);
                    will(returnValue(experiment));
                    one(openbisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(null));
                    one(openbisService).registerSample(newSample, null);
                }
            });

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);
        extractor.processMetadataAndFillDataSetInformation();
        assertEquals(ownerEmail, dataSetInformation.tryGetUploadingUserEmail());
        assertEquals("/CINA/S-SUFFIX", dataSetInformation.getSampleIdentifier().toString());

        context.assertIsSatisfied();
    }

    /**
     * Tries to register a sample that is already in the database (this should not happen in
     * practice)
     */
    @Test
    public void testDuplicateSampleRegistration()
    {
        final String experimentId = "/CINA/CINA1/EXP1";
        final String sampleCodePrefix = "S";
        final String sampleCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("experiment.identifier", experimentId);
        sampleMetadata.put("sample.code-prefix", sampleCodePrefix);
        sampleMetadata.put("experiment.owner-email", ownerEmail);

        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentId).createIdentifier();
        final Experiment experiment = new Experiment();
        experiment.setIdentifier(experimentIdentifier.toString());

        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new GroupIdentifier((String) null, "CINA"), sampleCodePrefix
                        + "-" + sampleCodeSuffix);

        final Sample existingSample = new Sample();
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(experimentIdentifier);
                    will(returnValue(experiment));
                    one(openbisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(existingSample));
                }
            });

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);

        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Registering a duplicate sample should throw an exception.");
        } catch (EnvironmentFailureException ex)
        {
            // this should happen
        }
        assertEquals(ownerEmail, dataSetInformation.tryGetUploadingUserEmail());

        context.assertIsSatisfied();
    }

    /**
     * Tries to register an sample where required metadata is missing.
     */
    @Test
    public void testRegisterExperimentWithMissingExperimentIdentifier()
    {
        final String sampleCodePrefix = "S";
        final String sampleCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("sample.code-prefix", sampleCodePrefix);
        sampleMetadata.put("experiment.owner-email", ownerEmail);

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Experiment identifier was not specified -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }

    /**
     * Tries to register an sample where required metadata is missing.
     */
    @Test
    public void testRegisterExperimentWithJunkExperimentIdentifier()
    {
        final String experimentId = "/CINA-adf-EXP1";
        final String sampleCodePrefix = "S";
        final String sampleCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("experiment.identifier", experimentId);
        sampleMetadata.put("sample.code-prefix", sampleCodePrefix);
        sampleMetadata.put("experiment.owner-email", ownerEmail);

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Experiment identifier was not in the correct format -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }

    /**
     * Tries to register an sample where required metadata is missing.
     */
    @Test
    public void testRegisterExperimentWithInvalidExperimentIdentifier()
    {
        final String experimentId = "/CINA/CINA1/EXP482";
        final String sampleCodePrefix = "S";
        final String sampleCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("experiment.identifier", experimentId);
        sampleMetadata.put("sample.code-prefix", sampleCodePrefix);
        sampleMetadata.put("experiment.owner-email", ownerEmail);

        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentId).createIdentifier();

        final SampleIdentifier sampleIdentifier =
                SampleIdentifier.createOwnedBy(new SampleOwnerIdentifier(experimentIdentifier),
                        sampleCodePrefix + "-" + sampleCodeSuffix);

        final NewSample newSample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode("CINA_SAMPLE_TYPE");
        newSample.setSampleType(sampleType);
        newSample.setExperimentIdentifier(experimentId.toString());
        newSample.setIdentifier(sampleIdentifier.toString());
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(experimentIdentifier);
                    will(returnValue(null));
                }
            });

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("There is no experiment with the provided identifier -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
        assertEquals(ownerEmail, dataSetInformation.tryGetUploadingUserEmail());

        context.assertIsSatisfied();
    }

    /**
     * Tries to register an sample where required metadata is missing.
     */
    @Test
    public void testRegisterSampleWithMissingSampleCodePrefix()
    {
        final String experimentId = "/CINA/CINA1/EXP1";
        final String sampleCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("experiment.identifier", experimentId);
        sampleMetadata.put("experiment.owner-email", ownerEmail);

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Sample code prefix was not specified -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }

    /**
     * Tries to register an sample where required metadata is missing.
     */
    @Test
    public void testRegisterSampleWithMissingEmailAddress()
    {
        final String experimentId = "/CINA/CINA1/EXP1";
        final String sampleCodePrefix = "S";
        final String sampleCodeSuffix = "SUFFIX";

        final HashMap<String, String> sampleMetadata = new HashMap<String, String>();
        sampleMetadata.put("experiment.identifier", experimentId);
        sampleMetadata.put("sample.code-prefix", sampleCodePrefix);

        SampleRegistrationInformationExtractor extractor =
                new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata, sampleCodeSuffix,
                        openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Owner email address was not specified -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }
}
