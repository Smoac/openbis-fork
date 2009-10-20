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

package ch.systemsx.cisd.yeastx.quant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.yeastx.quant.dto.MSConcentrationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationsDTO;
import ch.systemsx.cisd.yeastx.utils.JaxbXmlParser;

/**
 * A default data set info extractor enriched with parent dataset codes.
 * 
 * @author Tomasz Pylak
 */
public class QuantMLDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private final IDataSetInfoExtractor defaultExtractor;

    public QuantMLDataSetInfoExtractor(final Properties globalProperties)
    {
        this.defaultExtractor = new DefaultDataSetInfoExtractor(globalProperties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation dataSetInformation =
                defaultExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        List<String> parentDatasetCodes = extractParentDatasetCodes(incomingDataSetPath);
        dataSetInformation.setParentDataSetCodes(parentDatasetCodes);
        return dataSetInformation;
    }

    private static List<String> extractParentDatasetCodes(File incomingDataSetPath)
    {
        MSQuantificationsDTO quantifications =
                JaxbXmlParser.parse(MSQuantificationsDTO.class, incomingDataSetPath, true);
        List<String> parentDatasets = new ArrayList<String>();
        for (MSQuantificationDTO q : quantifications.getQuantifications())
        {
            for (MSConcentrationDTO c : q.getConcentrations())
            {
                parentDatasets.add(c.getParentDatasetCode());
            }
        }
        return parentDatasets;
    }

}
