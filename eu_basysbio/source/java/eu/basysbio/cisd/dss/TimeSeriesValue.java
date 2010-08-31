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

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;

/**
 * @author Franz-Josef Elmer
 */
public class TimeSeriesValue extends AbstractDataValue
{
    private String identifier;

    private String humanReadable;

    private String bsbId;

    private String confidenceLevel;

    private String controlledGene;

    private Integer numberOfReplicates;
    
    private Double value;

    public final String getIdentifier()
    {
        return identifier;
    }

    public final void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public final String getHumanReadable()
    {
        return humanReadable;
    }

    public final void setHumanReadable(String humanReadable)
    {
        this.humanReadable = humanReadable;
    }

    public final String getBsbId()
    {
        return bsbId;
    }

    public final void setBsbId(String bsbId)
    {
        this.bsbId = bsbId;
    }

    public final String getConfidenceLevel()
    {
        return confidenceLevel;
    }

    public final void setConfidenceLevel(String confidenceLevel)
    {
        this.confidenceLevel = confidenceLevel;
    }

    public final String getControlledGene()
    {
        return controlledGene;
    }

    public final void setControlledGene(String controlledGene)
    {
        this.controlledGene = controlledGene;
    }

    public final Integer getNumberOfReplicates()
    {
        return numberOfReplicates;
    }

    public final void setNumberOfReplicates(Integer numberOfReplicates)
    {
        this.numberOfReplicates = numberOfReplicates;
    }

    public final Double getValue()
    {
        return value;
    }

    public final void setValue(Double value)
    {
        this.value = value;
    }
    
    TimeSeriesValue createFor(int rowIndex, Double cellValue, List<IColumnInjection<TimeSeriesValue>> injections)
    {
        TimeSeriesValue v = new TimeSeriesValue();
        for (IColumnInjection<TimeSeriesValue> injection : injections)
        {
            injection.inject(v, rowIndex);
        }
        v.setDescriptor(getDescriptor());
        v.setColumnIndex(getColumnIndex());
        v.setRowIndex(rowIndex);
        v.setValue(cellValue);
        v.setValueGroupId(getValueGroupId());
        return v;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
     
}
