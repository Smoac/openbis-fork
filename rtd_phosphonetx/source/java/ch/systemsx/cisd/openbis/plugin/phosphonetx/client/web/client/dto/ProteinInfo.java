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

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinInfo implements IsSerializable
{
    private TechId id;
    
    private String dataSetPermID;
    
    private String description;
    
    private double falseDiscoveryRate;

    public final TechId getId()
    {
        return id;
    }

    public final void setId(TechId id)
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

    public final double getFalseDiscoveryRate()
    {
        return falseDiscoveryRate;
    }

    public final void setFalseDiscoveryRate(double falseDiscoveryRate)
    {
        this.falseDiscoveryRate = falseDiscoveryRate;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }


}
