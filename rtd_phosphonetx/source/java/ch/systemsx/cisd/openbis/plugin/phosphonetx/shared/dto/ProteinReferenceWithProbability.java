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

import java.io.Serializable;

import net.lemnik.eodsql.ResultColumn;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinReferenceWithProbability extends ProteinReference implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ResultColumn("data_set_id")
    private long dataSetID;
    
    @ResultColumn("probability")
    private double probability;
    
    @ResultColumn("coverage")
    private double coverage;
    
    @ResultColumn("abundance")
    private Double abundance;
    
    @ResultColumn("sample_perm_id")
    private String samplePermID;

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

    public final Double getAbundance()
    {
        return abundance;
    }

    public final void setAbundance(Double abundance)
    {
        this.abundance = abundance;
    }

    public final String getSamplePermID()
    {
        return samplePermID;
    }

    public final void setSamplePermID(String samplePermID)
    {
        this.samplePermID = samplePermID;
    }

    public void setCoverage(double coverage)
    {
        this.coverage = coverage;
    }

    public double getCoverage()
    {
        return coverage;
    }

}
