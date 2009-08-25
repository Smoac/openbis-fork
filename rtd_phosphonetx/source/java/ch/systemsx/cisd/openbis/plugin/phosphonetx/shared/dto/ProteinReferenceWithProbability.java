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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto;

import net.lemnik.eodsql.ResultColumn;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinReferenceWithProbability extends ProteinReference
{
    @ResultColumn("data_set_id")
    private long dataSetID;
    
    @ResultColumn("probability")
    private double probability;
    
    @ResultColumn("value")
    private double abundance;
    
    @ResultColumn("sample_id")
    private Long sampleID;

    public final long getDataSetID()
    {
        return dataSetID;
    }

    public final void setDataSetID(long dataSetID)
    {
        this.dataSetID = dataSetID;
    }

    public final double getProbability()
    {
        return probability;
    }

    public final void setProbability(double probability)
    {
        this.probability = probability;
    }

    public final double getAbundance()
    {
        return abundance;
    }

    public final void setAbundance(double abundance)
    {
        this.abundance = abundance;
    }

    public final Long getSampleID()
    {
        return sampleID;
    }

    public final void setSampleID(Long sampleID)
    {
        this.sampleID = sampleID;
    }

}
