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
public class IdentifiedProtein
{
    @ResultColumn("id")
    private long id;
    
    @ResultColumn("data_set_id")
    private long dataSetID;
    
    @ResultColumn("data_set_perm_id")
    private String dataSetPermID;
    
    @ResultColumn("protein_id")
    private long proteinID;
    
    @ResultColumn("description")
    private String description;
    
    @ResultColumn("probability")
    private double probability;
    
    private double falseDiscoveryRate;

    public final long getId()
    {
        return id;
    }

    public final void setId(long id)
    {
        this.id = id;
    }

    public final String getDataSetPermID()
    {
        return dataSetPermID;
    }

    public final void setDataSetPermID(String dataSetPermID)
    {
        this.dataSetPermID = dataSetPermID;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public final long getDataSetID()
    {
        return dataSetID;
    }

    public final void setDataSetID(long dataSetID)
    {
        this.dataSetID = dataSetID;
    }

    public final long getProteinID()
    {
        return proteinID;
    }

    public final void setProteinID(long proteinID)
    {
        this.proteinID = proteinID;
    }

    public final double getProbability()
    {
        return probability;
    }

    public final void setProbability(double probability)
    {
        this.probability = probability;
    }

    public final double getFalseDiscoveryRate()
    {
        return falseDiscoveryRate;
    }

    public final void setFalseDiscoveryRate(double falseDiscoveryRate)
    {
        this.falseDiscoveryRate = falseDiscoveryRate;
    }


}
