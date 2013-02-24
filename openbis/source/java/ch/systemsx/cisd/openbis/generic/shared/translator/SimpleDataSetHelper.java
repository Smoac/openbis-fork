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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Utility class working with {@link SimpleDataSetInformationDTO}.
 * 
 * @author Izabela Adamczyk
 */
public class SimpleDataSetHelper
{
    /**
     * filters out all non-file datasets and applies translation on the remaining datasets
     */
    public static final List<SimpleDataSetInformationDTO> filterAndTranslate(
            List<ExternalData> externalData)
    {
        if (externalData == null)
        {
            return null;
        }
        List<SimpleDataSetInformationDTO> result = new ArrayList<SimpleDataSetInformationDTO>();
        for (ExternalData ed : externalData)
        {
            if (ed instanceof DataSet)
            {
                DataSet dataSet = (DataSet) ed;
                result.add(translate(dataSet));
            }
        }
        return result;
    }

    private static SimpleDataSetInformationDTO translate(DataSet data)
    {
        SimpleDataSetInformationDTO result = new SimpleDataSetInformationDTO();
        result.setDataStoreCode(data.getDataStore().getCode());
        result.setDataSetCode(data.getCode());
        result.setSpeedHint(data.getSpeedHint());
        result.setDataSetShareId(data.getShareId());
        result.setDataSetLocation(data.getLocation());
        result.setDataSetSize(data.getSize());
        result.setDatabaseInstanceCode(data.getExperiment().getProject().getSpace().getInstance()
                .getCode());
        result.setExperimentCode(data.getExperiment().getCode());
        result.setProjectCode(data.getExperiment().getProject().getCode());
        result.setSpaceCode(data.getExperiment().getProject().getSpace().getCode());
        result.setSampleCode(data.getSampleCode());
        result.setDataSetType(data.getDataSetType().getCode());
        result.setDataStoreUrl(data.getDataStore().getHostUrl());
        return result;
    }

    public static SimpleDataSetInformationDTO translate(DatasetDescription datasetDescription)
    {
        SimpleDataSetInformationDTO result = new SimpleDataSetInformationDTO();
        result.setDatabaseInstanceCode(datasetDescription.getDatabaseInstanceCode());
        result.setDataSetCode(datasetDescription.getDataSetCode());
        result.setSpeedHint(datasetDescription.getSpeedHint());
        result.setDataSetLocation(datasetDescription.getDataSetLocation());
        result.setDataSetSize(datasetDescription.getDataSetSize());
        result.setDataSetType(datasetDescription.getDataSetTypeCode());
        result.setExperimentCode(datasetDescription.getExperimentCode());
        result.setSpaceCode(datasetDescription.getSpaceCode());
        result.setProjectCode(datasetDescription.getProjectCode());
        result.setSampleCode(datasetDescription.getSampleCode());
        return result;
    }
}
