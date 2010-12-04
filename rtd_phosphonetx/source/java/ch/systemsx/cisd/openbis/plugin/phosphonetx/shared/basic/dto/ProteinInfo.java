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

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinInfo implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private TechId id;

    private String accessionNumber;

    private TechId experimentID;

    private String dataSetPermID;

    private String description;

    private double coverage;

    private Map<Long, Double> abundances;

    public final TechId getId()
    {
        return id;
    }

    public final void setId(TechId id)
    {
        this.id = id;
    }

    public final TechId getExperimentID()
    {
        return experimentID;
    }

    public final void setExperimentID(TechId experimentID)
    {
        this.experimentID = experimentID;
    }

    public final String getDataSetPermID()
    {
        return dataSetPermID;
    }

    public final void setDataSetPermID(String dataSetPermID)
    {
        this.dataSetPermID = dataSetPermID;
    }

    public final String getAccessionNumber()
    {
        return accessionNumber;
    }

    public final void setAccessionNumber(String accessionNumber)
    {
        this.accessionNumber = accessionNumber;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public final Map<Long, Double> getAbundances()
    {
        return abundances;
    }

    public final void setAbundances(Map<Long, Double> abundances)
    {
        this.abundances = abundances;
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
