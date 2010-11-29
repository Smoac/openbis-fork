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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Utility class working with {@link SimpleDataSetInformationDTO}.
 * 
 * @author Izabela Adamczyk
 */
public class SimpleDataSetHelper
{
    public static final List<SimpleDataSetInformationDTO> translate(
            List<ExternalDataPE> externalData)
    {
        if (externalData == null)
        {
            return null;
        }
        List<SimpleDataSetInformationDTO> result = new ArrayList<SimpleDataSetInformationDTO>();
        for (ExternalDataPE ed : externalData)
        {
            result.add(translate(ed));
        }
        return result;
    }

    private static SimpleDataSetInformationDTO translate(ExternalDataPE data)
    {
        SimpleDataSetInformationDTO result = new SimpleDataSetInformationDTO();
        result.setDataSetCode(data.getCode());
        result.setDataSetLocation(data.getLocation());
        result.setDatabaseInstanceCode(data.getExperiment().getProject().getSpace()
                .getDatabaseInstance().getCode());
        result.setExperimentCode(data.getExperiment().getCode());
        result.setProjectCode(data.getExperiment().getProject().getCode());
        result.setGroupCode(data.getExperiment().getProject().getSpace().getCode());
        SamplePE sampleOrNull = data.tryGetSample();
        result.setSampleCode(sampleOrNull == null ? null : sampleOrNull.getCode());
        result.setDataSetType(data.getDataSetType().getCode());

        HashSet<String> parentCodes = new HashSet<String>();
        for (DataPE parent : data.getParents())
        {
            parentCodes.add(parent.getCode());
        }

        result.setParentDataSetCodes(parentCodes);

        return result;
    }
}
