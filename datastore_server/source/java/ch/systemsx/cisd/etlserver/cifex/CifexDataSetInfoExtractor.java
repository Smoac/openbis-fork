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

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

import static ch.systemsx.cisd.etlserver.AbstractDataSetInfoExtractor.extractDataSetProperties;

/**
 * {@link IDataSetInfoExtractor} extracting group and sample from CIFEX comment.
 * 
 * @author Izabela Adamczyk
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

        DataSetUploadInfo info = CifexExtratorHelper.getDataSetUploadInfo(incomingDataSetPath);
        final DataSetInformation dataSetInformation = new DataSetInformation();
        SampleIdentifier identifier = SampleIdentifierFactory.parse(info.getSample());
        dataSetInformation.setSampleCode(identifier.getSampleCode());
        if (identifier.isGroupLevel())
        {
            dataSetInformation.setGroupCode(identifier.getGroupLevel().getGroupCode());
        }
        dataSetInformation.setUploadingUserEmail(CifexExtratorHelper
                .getUploadingUserEmail(incomingDataSetPath));
        dataSetInformation.setDataSetProperties(extractDataSetProperties(incomingDataSetPath,
                dataSetPropertiesFileNameOrNull));
        return dataSetInformation;
    }

}
