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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Tomasz Pylak
 */
public class BatchDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private final Properties properties;

    public BatchDataSetInfoExtractor(final Properties globalProperties)
    {
        this.properties = ExtendedProperties.getSubset(globalProperties, EXTRACTOR_KEY + '.', true);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetMappingInformation plainInfo =
                DatasetMappingUtil.tryGetDatasetMapping(incomingDataSetPath);
        if (plainInfo != null)
        {
            DataSetInformationYeastX info = new DataSetInformationYeastX();
            info.setComplete(true);
            info.setDataSetProperties(plainInfo.getProperties());
            String sampleCode =
                    getSampleCode(plainInfo, openbisService, incomingDataSetPath.getParentFile());
            info.setSampleCode(sampleCode);
            info.setGroupCode(plainInfo.getGroupCode());
            MLConversionType conversion = getConversion(plainInfo.getConversion());
            info.setConversion(conversion);
            return info;
        } else
        {
            // this should not happen if dataset handler passes here only files with the mapping
            throw new UserFailureException("No mapping found for the dataset file "
                    + incomingDataSetPath.getPath());
        }
    }

    private static MLConversionType getConversion(String conversion)
    {
        MLConversionType conversionType = MLConversionType.tryCreate(conversion);
        if (conversionType == null)
        {
            // this should not happen if dataset handler ensures that conversion is valid
            throw new UserFailureException("Unknown value in conversion column " + conversion);
        }
        return conversionType;
    }

    private String getSampleCode(DataSetMappingInformation mapping,
            IEncapsulatedOpenBISService openbisService, File logDir)
    {
        String sampleCode =
                new DatasetMappingResolver(properties, openbisService).tryFigureSampleCode(mapping,
                        new LogUtils(logDir));
        if (sampleCode == null)
        {
            // should not happen, the dataset handler should skip datasets with incorrect mapping
            throw UserFailureException.fromTemplate("Cannot find a sample for the file '%s'.",
                    mapping.getFileName());
        }
        return sampleCode;
    }
}
