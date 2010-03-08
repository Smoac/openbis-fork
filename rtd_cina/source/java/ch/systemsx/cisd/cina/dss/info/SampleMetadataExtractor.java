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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Package-visible helper class to extract information from the XML metadata file and register a new
 * sample using this data.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleMetadataExtractor
{
    // Keys expected in metadata properties file
    public static final String SAMPLE_CODE_KEY = "sample";

    // Instance state
    private final DataSetInformation dataSetInformation;

    private final Map<String, String> sampleMetadata;

    private final IEncapsulatedOpenBISService openbisService;

    // Internal state used during extraction
    private String projectCode;

    private String experimentCode;

    private String sampleCode;

    private String emailAddress;

    static final String SPACE_CODE = "CINA";

    static final String EXPERIMENT_TYPE_CODE = "CINA_EXP_TYPE";

    static final String SAMPLE_TYPE_CODE = "CINA_SAMPLE_TYPE";

    SampleMetadataExtractor(DataSetInformation dataSetInformation,
            Map<String, String> sampleMetadata, IEncapsulatedOpenBISService openbisService)
    {
        this.dataSetInformation = dataSetInformation;
        this.sampleMetadata = sampleMetadata;
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

        // Check that that the required data was specified in the properties file.
        verifyRequiredMetadataDataHasBeenProvided();

        this.createSample();
        dataSetInformation.setSampleCode(sampleCode);
        dataSetInformation.setUploadingUserEmail(emailAddress);
    }

    private void extractMetadata()
    {
        projectCode = sampleMetadata.get(ExperimentMetadataExtractor.PROJECT_CODE_KEY);
        experimentCode = sampleMetadata.get(ExperimentMetadataExtractor.EXPERIMENT_CODE_KEY);
        sampleCode = sampleMetadata.get(SAMPLE_CODE_KEY);
        emailAddress = sampleMetadata.get(ExperimentMetadataExtractor.EXPERIMENT_OWNER_EMAIL_KEY);
    }

    /**
     * Create a new experiment. If a sample already exists, alter the identifier to make a unique
     * one.
     */
    private SampleIdentifier createSample()
    {
        ExperimentIdentifier experimentId =
                new ExperimentIdentifier(null, SPACE_CODE, projectCode, experimentCode);

        SampleIdentifier sampleId =
                new SampleIdentifier(new GroupIdentifier((String) null, SPACE_CODE), sampleCode);

        NewSample sample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode(SAMPLE_TYPE_CODE);
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(experimentId.toString());
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
        if (null == projectCode)
        {
            throw new UserFailureException(
                    "A project code must be specified to register an experiment.");
        }

        if (null == experimentCode)
        {
            throw new UserFailureException(
                    "An experiment code must be specified to register an experiment.");
        }

        if (null == sampleCode)
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