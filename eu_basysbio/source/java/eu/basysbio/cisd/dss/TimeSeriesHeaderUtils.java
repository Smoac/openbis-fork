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

package eu.basysbio.cisd.dss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.Column;

/**
 * Tools for working with time series headers.
 * 
 * @author Izabela Adamczyk
 */
public class TimeSeriesHeaderUtils
{
    /**
     * All data columns should have the same metadata (except TimePoint).
     */
    public static void assertMetadataConsistent(List<Column> dataColumns)
    {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        for (Column dataColumn : dataColumns)
        {
            DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
            updateMap(map, "ExperimentCode", dataColumnHeader.getExperimentCode());
            updateMap(map, "CultivationMethod", dataColumnHeader.getCultivationMethod());
            updateMap(map, "BiologicalReplicatateCode", dataColumnHeader
                    .getBiologicalReplicateCode());
            updateMap(map, "TimePointType", dataColumnHeader.getTimePointType());
            updateMap(map, "TechnicalReplicateCode", dataColumnHeader.getTechnicalReplicateCode());
            updateMap(map, "CelLoc", dataColumnHeader.getCelLoc());
            updateMap(map, "ValueType", dataColumnHeader.getValueType());
            updateMap(map, "Scale", dataColumnHeader.getScale());
            updateMap(map, "BiID", dataColumnHeader.getBiID());
            updateMap(map, "CG", dataColumnHeader.getControlledGene());
            updateMap(map, "DataSetType", dataColumnHeader.getTimeSeriesDataSetType());
        }
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet())
        {
            if (map.get(key).size() > 1)
            {
                if (sb.length() > 0)
                {
                    sb.append(",");
                }
                sb.append(key);
                sb.append("(");
                sb.append(StringUtils.join(map.get(key), ","));
                sb.append(")");
            }
        }
        if (sb.length() > 0)
        {
            throw new UserFailureException("Inconsistent data column headers: [" + sb + "]");
        }
    }

    private static void updateMap(Map<String, Set<String>> map, String key, String value)
    {
        if (map.containsKey(key) == false)
        {
            map.put(key, new HashSet<String>());
        }
        map.get(key).add(value);
    }
}
