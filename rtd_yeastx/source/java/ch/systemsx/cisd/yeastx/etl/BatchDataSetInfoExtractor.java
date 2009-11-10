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
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Tomasz Pylak
 */
public class BatchDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private final Properties properties;

    private final DataSetInfoFileNameDecorator fileNameDecorator;

    public BatchDataSetInfoExtractor(final Properties globalProperties)
    {
        this.properties = ExtendedProperties.getSubset(globalProperties, EXTRACTOR_KEY + '.', true);
        this.fileNameDecorator = new DataSetInfoFileNameDecorator(properties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        LogUtils log = new LogUtils(incomingDataSetPath.getParentFile());
        DataSetMappingInformation plainInfo =
                DatasetMappingUtil.tryGetDatasetMapping(incomingDataSetPath, log);
        if (plainInfo != null)
        {
            DataSetInformationYeastX info = new DataSetInformationYeastX();
            info.setComplete(true);
            info.setDataSetProperties(plainInfo.getProperties());
            setSampleOrExperiment(openbisService, log, plainInfo, info);
            info.setGroupCode(plainInfo.getGroupCode());
            MLConversionType conversion = getConversion(plainInfo.getConversion());
            info.setConversion(conversion);
            String parentDataSetCodes = plainInfo.getParentDataSetCodes();
            if (StringUtils.isNotBlank(parentDataSetCodes))
            {
                String[] parentCodes =
                        parentDataSetCodes
                                .split(DataSetMappingInformation.PARENT_DATASETS_SEPARATOR);
                info.setParentDataSetCodes(Arrays.asList(parentCodes));
            }
            fileNameDecorator.enrich(info, incomingDataSetPath);
            return info;
        } else
        {
            // this should not happen if dataset handler passes here only files with the mapping
            throw new UserFailureException("No mapping found for the dataset file "
                    + incomingDataSetPath.getPath());
        }
    }

    private void setSampleOrExperiment(IEncapsulatedOpenBISService openbisService, LogUtils log,
            DataSetMappingInformation mapping, DataSetInformationYeastX info)
    {
        DatasetMappingResolver mappingResolver =
                new DatasetMappingResolver(properties, openbisService);
        String sampleCode = mappingResolver.tryFigureSampleCode(mapping, log);
        if (sampleCode != null)
        {
            info.setSampleCode(sampleCode);
        } else
        {
            ExperimentIdentifier experimentIdentifier =
                    DatasetMappingResolver.tryFigureExperimentIdentifier(mapping);
            if (experimentIdentifier == null)
            {
                throw new UserFailureException(
                        "Both sample and experiment are not provided for the file "
                                + mapping.getFileName());
            }
            info.setExperimentIdentifier(experimentIdentifier);
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
}
