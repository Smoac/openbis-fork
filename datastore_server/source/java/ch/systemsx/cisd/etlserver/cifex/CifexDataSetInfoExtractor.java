/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.cifex;

import static ch.systemsx.cisd.etlserver.AbstractDataSetInfoExtractor.extractDataSetProperties;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * {@link IDataSetInfoExtractor} extracting data from CIFEX comment.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class CifexDataSetInfoExtractor implements IDataSetInfoExtractor
{

    @Private
    static final String DATA_SET_PROPERTIES_FILE_NAME_KEY =
            "data-set-info-extractor.data-set-properties-file-name";

    private final String dataSetPropertiesFileNameOrNull;

    public CifexDataSetInfoExtractor(final Properties globalProperties)
    {
        dataSetPropertiesFileNameOrNull =
                globalProperties.getProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        assert incomingDataSetPath != null : "Incoming data set path can not be null.";

        DataSetUploadInfo info = CifexExtractorHelper.getDataSetUploadInfo(incomingDataSetPath);
        final DataSetInformation dataSetInformation = new DataSetInformation();

        // either sample is specified or experiment (with optional data set parents)

        SampleIdentifier sampleIdentifierOrNull = tryGetSampleIdentifier(info);
        if (sampleIdentifierOrNull != null)
        {
            dataSetInformation.setSampleCode(sampleIdentifierOrNull.getSampleCode());
            if (sampleIdentifierOrNull.isGroupLevel())
            {
                dataSetInformation.setGroupCode(sampleIdentifierOrNull.getGroupLevel()
                        .getGroupCode());
            }
        } else
        {
            ExperimentIdentifier experimentIdentifier =
                    new ExperimentIdentifierFactory(info.getExperiment()).createIdentifier();
            dataSetInformation.setExperimentIdentifier(experimentIdentifier);
            dataSetInformation.setGroupCode(experimentIdentifier.getGroupCode());
            String[] parents = info.getParents() != null ? info.getParents() : new String[0];
            dataSetInformation.setParentDataSetCodes(Arrays.asList(parents));
        }

        dataSetInformation.setUploadingUserEmail(CifexExtractorHelper
                .getUploadingUserEmail(incomingDataSetPath));
        dataSetInformation.setDataSetProperties(extractDataSetProperties(incomingDataSetPath,
                dataSetPropertiesFileNameOrNull));
        return dataSetInformation;
    }

    private SampleIdentifier tryGetSampleIdentifier(DataSetUploadInfo info)
    {
        return (info.getSample() == null) ? null : SampleIdentifierFactory.parse(info.getSample());
    }

}
