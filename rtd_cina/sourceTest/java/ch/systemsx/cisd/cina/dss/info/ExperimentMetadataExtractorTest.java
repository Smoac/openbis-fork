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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ExperimentMetadataExtractorTest extends AssertJUnit
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
     * Creates dummy metadata and invokes the ExperimentMetadataExtrator.
     */
    @Test
    public void testSuccessfulExperimentRegistration()
    {
        final String projectIdString = "/CINA/CINA1";
        final String experimentCodePrefix = "EXP";
        final String experimentCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> experimentMetadata = new HashMap<String, String>();
        experimentMetadata.put("project.identifier", projectIdString);
        experimentMetadata.put("experiment.code-prefix", experimentCodePrefix);
        experimentMetadata.put("experiment.owner-email", ownerEmail);

        final ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(projectIdString).createIdentifier();

        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(projectIdentifier, experimentCodePrefix + "-"
                        + experimentCodeSuffix);
        final NewExperiment newExperiment =
                new NewExperiment(identifier.toString(), "CINA_EXP_TYPE");
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(identifier);
                    will(returnValue(null));
                    // TODO: This does not check the type of the new experiment -- it only tests
                    // that the identifier of the new experiment is the one specified above.
                    one(openbisService).registerExperiment(newExperiment);
                }
            });

        ExperimentRegistrationInformationExtractor extractor =
                new ExperimentRegistrationInformationExtractor(dataSetInformation, experimentMetadata,
                        experimentCodeSuffix, openbisService);
        extractor.processMetadataAndFillDataSetInformation();
        assertEquals(ownerEmail, dataSetInformation.tryGetUploadingUserEmail());

        context.assertIsSatisfied();
    }

    /**
     * Tries to register an experiment that is already in the database (this should not happen in
     * practice)
     */
    @Test
    public void testDuplicateExperimentRegistration()
    {
        final String projectIdString = "/CINA/CINA1";
        final String experimentCodePrefix = "EXP";
        final String experimentCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> experimentMetadata = new HashMap<String, String>();
        experimentMetadata.put("project.identifier", projectIdString);
        experimentMetadata.put("experiment.code-prefix", experimentCodePrefix);
        experimentMetadata.put("experiment.owner-email", ownerEmail);

        final ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(projectIdString).createIdentifier();

        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(projectIdentifier, experimentCodePrefix + "-"
                        + experimentCodeSuffix);
        final Experiment existingExperiment = new Experiment();
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(identifier);
                    will(returnValue(existingExperiment));
                }
            });

        ExperimentRegistrationInformationExtractor extractor =
                new ExperimentRegistrationInformationExtractor(dataSetInformation, experimentMetadata,
                        experimentCodeSuffix, openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Registering a duplicate experiment should throw an exception.");
        } catch (EnvironmentFailureException ex)
        {
            // this should happen
        }
        assertEquals(ownerEmail, dataSetInformation.tryGetUploadingUserEmail());

        context.assertIsSatisfied();
    }

    /**
     * Tries to register an experiment where required metadata is missing.
     */
    @Test
    public void testRegisterExperimentWithMissingProjectCode()
    {
        final String experimentCode = "EXP";
        final String experimentCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> experimentMetadata = new HashMap<String, String>();
        experimentMetadata.put("experiment.code", experimentCode);
        experimentMetadata.put("experiment.owner-email", ownerEmail);

        ExperimentRegistrationInformationExtractor extractor =
                new ExperimentRegistrationInformationExtractor(dataSetInformation, experimentMetadata,
                        experimentCodeSuffix, openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Project code was not specified -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }

    /**
     * Tries to register an experiment where required metadata is missing.
     */
    @Test
    public void testRegisterExperimentWithMissingExperimentCodePrefix()
    {
        final String projectCode = "CINA1";
        final String experimentCodeSuffix = "SUFFIX";
        final String ownerEmail = "no-one@nowhere.ch";

        final HashMap<String, String> experimentMetadata = new HashMap<String, String>();
        experimentMetadata.put("project.code", projectCode);
        experimentMetadata.put("experiment.owner-email", ownerEmail);

        ExperimentRegistrationInformationExtractor extractor =
                new ExperimentRegistrationInformationExtractor(dataSetInformation, experimentMetadata,
                        experimentCodeSuffix, openbisService);
        try
        {
            extractor.processMetadataAndFillDataSetInformation();
            fail("Experiment code prefix was not specified -- this should result in an exception");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }

    /**
     * Tries to register an experiment where required metadata is missing.
     */
    @Test
    public void testRegisterExperimentWithMissingEmailAddress()
    {
        final String projectCode = "CINA1";
        final String experimentCode = "EXP";
        final String experimentCodeSuffix = "SUFFIX";

        final HashMap<String, String> experimentMetadata = new HashMap<String, String>();
        experimentMetadata.put("project.code", projectCode);
        experimentMetadata.put("experiment.code", experimentCode);

        ExperimentRegistrationInformationExtractor extractor =
                new ExperimentRegistrationInformationExtractor(dataSetInformation, experimentMetadata,
                        experimentCodeSuffix, openbisService);
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
