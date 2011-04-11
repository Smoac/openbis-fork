/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Tomasz Pylak
 */
public class MaterialSummarySettings implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<String> featureCodes;

    private String replicaMatrialTypePattern;

    private List<String> subgroupPropertyTypeCodes;

    private MaterialReplicaSummaryAggregationType aggregationType;

    public List<String> getFeatureCodes()
    {
        return featureCodes;
    }

    public void setFeatureCodes(List<String> featureCodes)
    {
        this.featureCodes = featureCodes;
    }

    public String getReplicaMatrialTypePattern()
    {
        return replicaMatrialTypePattern;
    }

    public void setReplicaMatrialTypePatterns(String replicaMatrialTypePattern)
    {
        this.replicaMatrialTypePattern = replicaMatrialTypePattern;
    }

    public List<String> getSubgroupPropertyTypeCodes()
    {
        return subgroupPropertyTypeCodes;
    }

    public void setSubgroupPropertyTypeCodes(String... subgroupPropertyTypeCodes)
    {
        this.subgroupPropertyTypeCodes = Arrays.asList(subgroupPropertyTypeCodes);
    }

    public MaterialReplicaSummaryAggregationType getAggregationType()
    {
        return aggregationType;
    }

    public void setAggregationType(MaterialReplicaSummaryAggregationType aggregationType)
    {
        this.aggregationType = aggregationType;
    }
}
