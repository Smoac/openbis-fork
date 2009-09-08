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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListProteinByExperimentCriteria extends DefaultResultSetConfig<String, ProteinInfo>
        implements IsSerializable
{
    private TechId experimentID;
    private double falseDiscoveryRate;
    private AggregateFunction aggregateFunction;
    private String treatmentTypeCode;
    private boolean aggregateOriginal;

    public final TechId getExperimentID()
    {
        return experimentID;
    }

    public final void setExperimentID(TechId experimentID)
    {
        this.experimentID = experimentID;
    }

    public final double getFalseDiscoveryRate()
    {
        return falseDiscoveryRate;
    }

    public final void setFalseDiscoveryRate(double falseDiscoveryRate)
    {
        this.falseDiscoveryRate = falseDiscoveryRate;
    }
    
    public final AggregateFunction getAggregateFunction()
    {
        return aggregateFunction;
    }

    public void setAggregateFunction(AggregateFunction aggregateFunction)
    {
        this.aggregateFunction = aggregateFunction;
    }

    public final String getTreatmentTypeCode()
    {
        return treatmentTypeCode;
    }

    public final void setTreatmentTypeCode(String treatmentTypeCode)
    {
        this.treatmentTypeCode = treatmentTypeCode;
    }

    public final boolean isAggregateOriginal()
    {
        return aggregateOriginal;
    }

    public final void setAggregateOriginal(boolean aggregateOriginal)
    {
        this.aggregateOriginal = aggregateOriginal;
    }
    
}
