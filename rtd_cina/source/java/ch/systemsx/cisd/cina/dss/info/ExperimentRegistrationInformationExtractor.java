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
 * Package-visible helper class to extract information from the XML metadata file and register a new
 * experiment using this data.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ExperimentRegistrationInformationExtractor
{
    // Keys expected in metadata properties file
    public static final String PROJECT_IDENTIFIER_KEY = "project.identifier";

    public static final String EXPERIMENT_CODE_KEY = "experiment.code-prefix";

    public static final String EXPERIMENT_OWNER_EMAIL_KEY = "experiment.owner-email";

    // Instance state
    private final DataSetInformation dataSetInformation;

    private final Map<String, String> experimentMetadata;

    private final String experimentCodeSuffix;

    private final IEncapsulatedOpenBISService openbisService;

    // Internal state used during extraction
    private ProjectIdentifier projectIdentifier;

    private String experimentCodePrefix;

    private String emailAddress;

    ExperimentRegistrationInformationExtractor(DataSetInformation dataSetInformation,
            Map<String, String> experimentMetadata, String experimentCodeSuffix,
            IEncapsulatedOpenBISService openbisService)
    {
        this.dataSetInformation = dataSetInformation;
        this.experimentMetadata = experimentMetadata;
        this.experimentCodeSuffix = experimentCodeSuffix;
        this.openbisService = openbisService;
    }

    /**
     * Takes the project code, experiment code, and email address from the properties file and
     * creates a new experiment.
     * <p>
     * This method is package-visible for testing purposes.
     */
    void processMetadataAndFillDataSetInformation() throws UserFailureException
    {
        extractMetadata();

        // Set the email address as early as possible because it will be used later to notify the
        // user.
        dataSetInformation.setUploadingUserEmail(emailAddress);

        // Check that that the required data was specified in the properties file.
        verifyRequiredMetadataDataHasBeenProvided();

        ExperimentIdentifier experimentId = this.createExperiment();
        dataSetInformation.setExperimentIdentifier(experimentId);
    }

    private void extractMetadata()
    {
        String projectIdString = experimentMetadata.get(PROJECT_IDENTIFIER_KEY);
        if (projectIdString != null)
        {
            projectIdentifier = new ProjectIdentifierFactory(projectIdString).createIdentifier();
        }
        experimentCodePrefix = experimentMetadata.get(EXPERIMENT_CODE_KEY);
        emailAddress = experimentMetadata.get(EXPERIMENT_OWNER_EMAIL_KEY);
    }

    /**
     * Create a new experiment. If an experiment already exists, raise an error.
     */
    private ExperimentIdentifier createExperiment() throws EnvironmentFailureException
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(projectIdentifier, experimentCodePrefix + "-"
                        + experimentCodeSuffix);

        // Check that the identifier is unique
        Experiment experiment = openbisService.tryToGetExperiment(experimentIdentifier);
        if (experiment != null)
        {
            throw new EnvironmentFailureException(
                    "The generated experiment identifer, which must be unique, is already in the database. This should not happen: Please contact the administrator.");
        }

        openbisService.registerExperiment(new NewExperiment(experimentIdentifier.toString(),
                EntityTypes.ExperimentTypes.CINA_EXP_TYPE.toString()));

        return experimentIdentifier;
    }

    /**
     * Ensure the required user-provided data has been specified. Throws an exception if this is not
     * the case.
     */
    private void verifyRequiredMetadataDataHasBeenProvided() throws UserFailureException
    {
        if (null == projectIdentifier)
        {
            throw new UserFailureException(
                    "A project identifier must be specified to register an experiment.");
        }

        if (null == experimentCodePrefix)
        {
            throw new UserFailureException(
                    "An experiment code prefix must be specified to register an experiment.");
        }

        if (null == emailAddress)
        {
            throw new UserFailureException(
                    "An email address must be specified to register an experiment.");
        }
    }
    /**
     * Automatically generates the code.
     */
    // Need to use something like this.
    // public void generateCode()
    // {
    // viewContext.getCommonService().generateCode(codePrefix,
    // new GenerateCodeCallback(viewContext));
    // }
    //
    // private final class GenerateCodeCallback extends AbstractAsyncCallback<String>
    // {
    //
    // GenerateCodeCallback(final IViewContext<?> viewContext)
    // {
    // super(viewContext);
    // }
    //
    // @Override
    // protected final void process(final String result)
    // {
    // setValue(result);
    // }
    //
    // }

}