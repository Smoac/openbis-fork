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

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;


/**
 * Descriptor of a column in a time series data set file.
 * 
 * @author Bernd Rinn
 */
public class TimeSeriesColumnDescriptor
{
    private final ValueGroupDescriptor valueGroupDescriptor;

    private final String valueType;

    private final String unit;

    private final String scale;

    public TimeSeriesColumnDescriptor(ValueGroupDescriptor descriptor,
            DataColumnHeader dataColumnHeader)
    {
        valueGroupDescriptor = descriptor;
        valueType = dataColumnHeader.getValueType();
        unit = dataColumnHeader.getUnit();
        scale = dataColumnHeader.getScale();
    }

    public String getExperimentType()
    {
        return valueGroupDescriptor.getExperimentType();
    }

    public String getCultivationMethod()
    {
        return valueGroupDescriptor.getCultivationMethod();
    }

    public String getBiologicalReplicates()
    {
        return valueGroupDescriptor.getBiologicalReplicates();
    }

    public int getTimePoint()
    {
        return valueGroupDescriptor.getTimePoint();
    }

    public String getTimePointType()
    {
        return valueGroupDescriptor.getTimePointType();
    }

    public String getTechnicalReplicates()
    {
        return valueGroupDescriptor.getTechnicalReplicates();
    }

    public String getCellLocation()
    {
        return valueGroupDescriptor.getCellLocation();
    }

    public String getDataSetType()
    {
        return valueGroupDescriptor.getDataSetType();
    }

    public String getValueType()
    {
        return valueType;
    }

    public String getUnit()
    {
        return unit;
    }

    public String getScale()
    {
        return scale;
    }

    public String getBiId()
    {
        return valueGroupDescriptor.getBiId();
    }

    public String getControlledGene()
    {
        return valueGroupDescriptor.getControlledGene();
    }

    public final String getGenotype()
    {
        return valueGroupDescriptor.getGenotype();
    }

    public final String getGrowthPhase()
    {
        return valueGroupDescriptor.getGrowthPhase();
    }

    public ValueGroupDescriptor getValueGroupDescriptor()
    {
        return valueGroupDescriptor;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
