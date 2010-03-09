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

import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Package-visible helper class to extract information from the XML metadata file and register a new
 * sample using this data.
 * <p>
 * This class assumes that the sample type CINA_SAMPLE_TYPE has been registered.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleMetadataExtractor
{
    // Keys expected in metadata properties file

    public static final String PROJECT_CODE_KEY = ExperimentMetadataExtractor.PROJECT_CODE_KEY;

    public static final String EXPERIMENT_IDENTIFIER_KEY = "experiment.identifier";

    public static final String EXPERIMENT_OWNER_EMAIL_KEY =
            ExperimentMetadataExtractor.EXPERIMENT_OWNER_EMAIL_KEY;

    public static final String SAMPLE_CODE_PREFIX_KEY = "sample.code-prefix";

    // Instance state
    private final DataSetInformation dataSetInformation;

    private final Map<String, String> sampleMetadata;

    private final String sampleCodeSuffix;

    private final IEncapsulatedOpenBISService openbisService;

    private ExperimentIdentifier experimentIdentifier;

    private String sampleCodePrefix;

    private String emailAddress;

    private static final String SAMPLE_TYPE_CODE = "CINA_SAMPLE_TYPE";

    SampleMetadataExtractor(DataSetInformation dataSetInformation,
            Map<String, String> sampleMetadata, String sampleCodeSuffix,
            IEncapsulatedOpenBISService openbisService)
    {
        this.dataSetInformation = dataSetInformation;
        this.sampleMetadata = sampleMetadata;
        this.sampleCodeSuffix = sampleCodeSuffix;
        this.openbisService = openbisService;
    }

    /**
     * Takes the project code, experiment code, and email address from the properties file and
     * creates a new experiment.
     * <p>
     * This method is package-visible for testing purposes.
     */
    void processMetadataAndFillDataSetInformation()
    {
        extractMetadata();

        // Set the email address as early as possible because it will be used later to notify the
        // user.
        dataSetInformation.setUploadingUserEmail(emailAddress);

        // Check that that the required data was specified in the properties file.
        verifyRequiredMetadataDataHasBeenProvided();

        SampleIdentifier sampleId = this.createSample();
        dataSetInformation.setExperimentIdentifier(experimentIdentifier);
        dataSetInformation.setSampleCode(sampleId.getSampleCode());
        dataSetInformation.setUploadingUserEmail(emailAddress);
    }

    private void extractMetadata()
    {
        experimentIdentifier =
                new ExperimentIdentifierFactory(sampleMetadata.get(EXPERIMENT_IDENTIFIER_KEY))
                        .createIdentifier();
        sampleCodePrefix = sampleMetadata.get(SAMPLE_CODE_PREFIX_KEY);
        emailAddress = sampleMetadata.get(EXPERIMENT_OWNER_EMAIL_KEY);
    }

    /**
     * Create a new experiment. If a sample already exists, alter the identifier to make a unique
     * one.
     */
    private SampleIdentifier createSample()
    {
        SampleIdentifier sampleId =
                SampleIdentifier.createOwnedBy(new SampleOwnerIdentifier(experimentIdentifier),
                        sampleCodePrefix + "-" + sampleCodeSuffix);

        NewSample sample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode(SAMPLE_TYPE_CODE);
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(experimentIdentifier.toString());
        sample.setIdentifier(sampleId.toString());

        Sample dbSample = openbisService.tryGetSampleWithExperiment(sampleId);
        if (dbSample == null)
        {
            openbisService.registerSample(sample, null);
        }
        return sampleId;
    }

    /**
     * Ensure the required user-provided data has been specified. Throws an exception if this is not
     * the case.
     */
    private void verifyRequiredMetadataDataHasBeenProvided() throws UserFailureException
    {
        if (null == experimentIdentifier)
        {
            throw new UserFailureException(
                    "An experiment code must be specified to register an experiment.");
        }

        if (null == sampleCodePrefix)
        {
            throw new UserFailureException(
                    "An sample code must be specified to register an experiment.");
        }

        if (null == emailAddress)
        {
            throw new UserFailureException(
                    "An email address must be specified to register an experiment.");
        }
    }
}