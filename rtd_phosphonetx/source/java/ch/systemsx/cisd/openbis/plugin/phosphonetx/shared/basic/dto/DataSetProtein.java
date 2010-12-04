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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetProtein implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String dataSetPermID;

    private TechId dataSetID;

    private TechId proteinID;

    private String sequenceName;

    private int peptideCount;

    private double falseDiscoveryRate;

    public final String getDataSetPermID()
    {
        return dataSetPermID;
    }

    public final void setDataSetPermID(String dataSetPermID)
    {
        this.dataSetPermID = dataSetPermID;
    }

    public final TechId getDataSetID()
    {
        return dataSetID;
    }

    public final void setDataSetID(TechId dataSetID)
    {
        this.dataSetID = dataSetID;
    }

    public final TechId getProteinID()
    {
        return proteinID;
    }

    public final void setProteinID(TechId proteinID)
    {
        this.proteinID = proteinID;
    }

    public final String getSequenceName()
    {
        return sequenceName;
    }

    public final void setSequenceName(String sequenceName)
    {
        this.sequenceName = sequenceName;
    }

    public final int getPeptideCount()
    {
        return peptideCount;
    }

    public final void setPeptideCount(int peptideCount)
    {
        this.peptideCount = peptideCount;
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
