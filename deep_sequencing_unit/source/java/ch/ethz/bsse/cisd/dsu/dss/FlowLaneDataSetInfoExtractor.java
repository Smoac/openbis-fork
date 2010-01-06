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

package ch.ethz.bsse.cisd.dsu.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class FlowLaneDataSetInfoExtractor implements IDataSetInfoExtractor
{
    static final String FLOW_LANE_NUMBER_SEPARATOR = ".";

    private final IDataSetInfoExtractor dataSetInfoExtractor;

    public FlowLaneDataSetInfoExtractor(Properties properties)
    {
        dataSetInfoExtractor = new DefaultDataSetInfoExtractor(properties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation dataSetInformation =
                dataSetInfoExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        String sampleCode = dataSetInformation.getSampleCode();
        if (sampleCode != null)
        {
            dataSetInformation.setSampleCode(sampleCode.replace(FLOW_LANE_NUMBER_SEPARATOR,
                    SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING));
        }
        return dataSetInformation;
    }

}
