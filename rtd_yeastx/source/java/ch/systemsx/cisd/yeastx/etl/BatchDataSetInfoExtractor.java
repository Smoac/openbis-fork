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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Tomasz Pylak
 */
public class BatchDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private static final String GROUP_CODE_PROPERTY_NAME = "group-code";

    private final String groupCode;

    public BatchDataSetInfoExtractor(final Properties globalProperties)
    {
        Properties properties =
                ExtendedProperties.getSubset(globalProperties, EXTRACTOR_KEY + '.', true);
        this.groupCode = properties.getProperty(GROUP_CODE_PROPERTY_NAME);
        ensureGroupCodeDefined();
    }

    private void ensureGroupCodeDefined()
    {
        if (groupCode == null)
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "No group code defined in server configuration. Use '%s' property to specify it.",
                            GROUP_CODE_PROPERTY_NAME);
        }
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath)
            throws UserFailureException, EnvironmentFailureException
    {
        DataSetMappingInformation plainInfo =
                DatasetMappingUtil.tryGetPlainDatasetInfo(incomingDataSetPath);
        if (plainInfo != null)
        {
            DataSetInformation info = new DataSetInformation();
            info.setComplete(true);
            info.setDataSetProperties(plainInfo.getProperties());
            info.setSampleCode(getSampleCode(plainInfo));
            info.setGroupCode(groupCode);
            return info;
        } else
        {
            // this should not happen if dataset handler passes here only files with the mapping
            throw new UserFailureException("No mapping found for the dataset file "
                    + incomingDataSetPath.getPath());
        }
    }

    private String getSampleCode(DataSetMappingInformation plainInfo)
    {
        return getSampleCode(plainInfo.getSampleCodeOrLabel(), plainInfo.getExperimentCode());
    }

    // TODO 2009-05-25, Tomasz Pylak: implement sample extraction
    private String getSampleCode(String sampleCodeOrLabel, String experimentCodeOrNull)
    {
        return sampleCodeOrLabel;
    }

}
