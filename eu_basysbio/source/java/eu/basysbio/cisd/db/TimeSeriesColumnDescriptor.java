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

package eu.basysbio.cisd.db;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;

import eu.basysbio.cisd.dss.DataColumnHeader;
import eu.basysbio.cisd.dss.ValueGroupDescriptor;

/**
 * Descriptor of a column in a time series data set file.
 * 
 * @author Bernd Rinn
 */
public class TimeSeriesColumnDescriptor
{
    private static final Pattern DATA_SET_TYPE_PATTERN =
            Pattern.compile("([a-zA-Z0-9]+)\\[([a-zA-Z0-9%]*)\\]");

    private final ValueGroupDescriptor valueGroupDescriptor;

    private final String valueType;

    private final String unit;

    private final String scale;
    
    public TimeSeriesColumnDescriptor(ValueGroupDescriptor descriptor, DataColumnHeader dataColumnHeader)
    {
        valueGroupDescriptor = descriptor;
        valueType = dataColumnHeader.getValueType();
        unit = dataColumnHeader.getUnit();
        scale = dataColumnHeader.getScale();
    }

    public TimeSeriesColumnDescriptor(TimeSeriesColumnDescriptor descr, String replacementBiId,
            int replacementTimePoint)
    {
        valueGroupDescriptor =
                new ValueGroupDescriptor(descr.getValueGroupDescriptor(), replacementBiId,
                        replacementTimePoint);
        valueType = descr.valueType;
        unit = descr.unit;
        scale = descr.scale;
    }

    public TimeSeriesColumnDescriptor(File file, String columnHeader)
    {
        final String[] headerFields = StringUtils.split(columnHeader, "::");
        if (headerFields.length != 12)
        {
            throw new ParsingException(file, "Header is supposed to have 12 elements, has "
                    + headerFields.length + ": " + columnHeader);
        }
        valueGroupDescriptor = new ValueGroupDescriptor(headerFields);
        final Matcher dataSetTypeColumnMatcher = DATA_SET_TYPE_PATTERN.matcher(headerFields[8]);
        if (dataSetTypeColumnMatcher.matches() == false)
        {
            System.err
                    .printf(
                            "Warning: Data Set Type field does not match expected format: '%s' [File: %s]\n",
                            headerFields[8], file);
            valueType = headerFields[8];
            unit = "???";
        } else
        {
            valueType = dataSetTypeColumnMatcher.group(1);
            unit = dataSetTypeColumnMatcher.group(2);
        }
        scale = headerFields[9];
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
