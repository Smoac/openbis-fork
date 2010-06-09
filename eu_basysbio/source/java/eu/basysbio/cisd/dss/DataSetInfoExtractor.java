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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.cifex.CifexDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.cifex.CifexTypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractor implements IDataSetInfoExtractor
{
    private final CifexDataSetInfoExtractor infoExtractor;

    private final CifexTypeExtractor typeExtractor;

    private final Map<String, IDataSetPropertiesExtractor> propertiesExtractors =
            new HashMap<String, IDataSetPropertiesExtractor>();

    private IDataSetPropertiesExtractor defaultExtractor;

    public DataSetInfoExtractor(Properties properties)
    {
        infoExtractor = new CifexDataSetInfoExtractor(properties);
        typeExtractor = new CifexTypeExtractor(properties);
        defaultExtractor = new DataSetPropertiesExtractor(properties, true);
        DataSetPropertiesExtractor timeSeriesExtractor =
                new DataSetPropertiesExtractor(properties, false);
        propertiesExtractors.put(DataSetHandler.TIME_SERIES, timeSeriesExtractor);
        propertiesExtractors.put(DataSetHandler.LCA_MTP_TIME_SERIES, timeSeriesExtractor);
        propertiesExtractors.put(DataSetHandler.LCA_MTP_PCAV_TIME_SERIES, timeSeriesExtractor);
        propertiesExtractors.put(DataSetHandler.LCA_MIC_TIME_SERIES, timeSeriesExtractor);

        propertiesExtractors.put(DataSetHandler.LCA_MIC, new LcaMicDataSetPropertiesExtractor(
                properties));
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation info =
                infoExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        String email = info.tryGetUploadingUserEmail();
        if (email != null)
        {
            info.getDataSetProperties().add(
                    new NewProperty(TimeSeriesPropertyType.UPLOADER_EMAIL.toString(), email));
        }
        DataSetType dataSetType = typeExtractor.getDataSetType(incomingDataSetPath);
        IDataSetPropertiesExtractor extractor = propertiesExtractors.get(dataSetType.getCode());
        if (extractor == null)
        {
            extractor = defaultExtractor;
        }
        List<NewProperty> headerProperties =
                extractor.extractDataSetProperties(incomingDataSetPath);
        info.getDataSetProperties().addAll(headerProperties);
        return info;
    }
}
