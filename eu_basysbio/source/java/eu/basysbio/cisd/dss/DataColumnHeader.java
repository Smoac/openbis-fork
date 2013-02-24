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

package eu.basysbio.cisd.dss;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;

public final class DataColumnHeader
{
    private static final class PropertyManager
    {
        private final Map<String, String> propertiesMap;
        private final String dataSetCode;

        PropertyManager(AbstractExternalData dataSet)
        {
            dataSetCode = dataSet.getCode();
            List<IEntityProperty> properties = dataSet.getProperties();
            propertiesMap = new HashMap<String, String>();
            for (IEntityProperty property : properties)
            {
                propertiesMap.put(property.getPropertyType().getCode(), property.tryGetAsString());
            }
        }
        
        String getProperty(TimePointPropertyType timePointPropertyType)
        {
            String value = propertiesMap.get(timePointPropertyType.toString());
            if (value == null)
            {
                throw new UserFailureException("Data Set '" + dataSetCode + "' has no property "
                        + timePointPropertyType + ".");
            }
            return value;
        }
    }
    
    static final String SEPARATOR = "::";

    static final TimePointPropertyType[] HEADER_ELEMENTS =
            new TimePointPropertyType[]
                { TimePointPropertyType.TECHNICAL_REPLICATE_CODE, TimePointPropertyType.CEL_LOC,
                        TimePointPropertyType.TIME_SERIES_DATA_SET_TYPE,
                        TimePointPropertyType.VALUE_TYPE, TimePointPropertyType.SCALE,
                        TimePointPropertyType.BI_ID, TimePointPropertyType.CG };
    
    private static final int HEADER_PARTS = 12;
    
    private static final int TIME_POINT_INDEX = 3;
    private static final int TIME_POINT_TYPE_INDEX = 4;
    private final Pattern VALUE_TYPE_PATTERN =
            Pattern.compile("([a-zA-Z0-9]+)\\[([a-zA-Z0-9%]*)\\]");
    
    private final String experimentCode;
    private final String cultivationMethod;
    private final String biologicalReplicateCode;
    private final int timePoint;
    private final String timePointType;
    private final String technicalReplicateCode;
    private final String celLoc;
    private final String timeSeriesDataSetType;
    private final String valueTypeAndUnit;
    private final String scale;
    private final String biID;
    private final String controlledGene;
    private final String growthPhase;
    private final String genotype;
    private final String normalizedHeader;
    private final String header;
    
    DataColumnHeader(DataColumnHeader header, AbstractExternalData dataSet)
    {
        PropertyManager propertyManager = new PropertyManager(dataSet);
        experimentCode = header.getExperimentCode();
        cultivationMethod = header.getCultivationMethod();
        biologicalReplicateCode = header.getBiologicalReplicateCode();
        timePoint = header.getTimePoint();
        timePointType = header.getTimePointType();
        technicalReplicateCode = propertyManager.getProperty(TimePointPropertyType.TECHNICAL_REPLICATE_CODE);
        celLoc = propertyManager.getProperty(TimePointPropertyType.CEL_LOC);
        timeSeriesDataSetType = propertyManager.getProperty(TimePointPropertyType.TIME_SERIES_DATA_SET_TYPE);
        valueTypeAndUnit = propertyManager.getProperty(TimePointPropertyType.VALUE_TYPE);
        scale = propertyManager.getProperty(TimePointPropertyType.SCALE);
        biID = propertyManager.getProperty(TimePointPropertyType.BI_ID);
        controlledGene = propertyManager.getProperty(TimePointPropertyType.CG);
        growthPhase = null;
        genotype = null;
        normalizedHeader = createNormalizedHeader();
        this.header = normalizedHeader;
    }

    DataColumnHeader(String header)
    {
        this.header = header;
        String[] parts = header.split(SEPARATOR);
        if (parts.length < HEADER_PARTS)
        {
            throw new IllegalArgumentException("At least " + HEADER_PARTS
                    + " elements of the following header separated by '" + DataColumnHeader.SEPARATOR
                    + "' expected: " + header);
        }
        experimentCode = parts[0];
        cultivationMethod = parts[1];
        biologicalReplicateCode = parts[2];
        timePoint = parseTimePoint(parts[TIME_POINT_INDEX], header);
        timePointType = parts[TIME_POINT_TYPE_INDEX];
        technicalReplicateCode = parts[5];
        celLoc = parts[6];
        timeSeriesDataSetType = parts[7];
        valueTypeAndUnit = parts[8];
        scale = parts[9];
        biID = parts[10];
        controlledGene = parts[11];
        growthPhase = parts.length > 12 ? parts[12] : null;
        genotype = parts.length > 13 ? parts[13] : null;
        normalizedHeader = createNormalizedHeader();
    }
    
    private String createNormalizedHeader()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(experimentCode).append(SEPARATOR);
        builder.append(cultivationMethod).append(SEPARATOR);
        builder.append(biologicalReplicateCode).append(SEPARATOR);
        builder.append(timePoint).append(SEPARATOR);
        builder.append(timePointType);
        for (TimePointPropertyType type : HEADER_ELEMENTS)
        {
            builder.append(SEPARATOR).append(type.getElement(this));
        }
        if (growthPhase != null)
        {
            builder.append(SEPARATOR).append(growthPhase).append(SEPARATOR).append(genotype);
        }
        return builder.toString();
    }

    private int parseTimePoint(String value, String originalHeader)
    {
        try
        {
            return Util.parseIntegerWithPlusSign(value);
        } catch (NumberFormatException ex)
        {
            return 0;
        }
    }
    
    public String getExperimentCode()
    {
        return experimentCode;
    }
    
    public String getCultivationMethod()
    {
        return cultivationMethod;
    }
    
    public String getBiologicalReplicateCode()
    {
        return biologicalReplicateCode;
    }
    
    public int getTimePoint()
    {
        return timePoint;
    }

    public String getTimePointType()
    {
        return timePointType;
    }

    public String getTechnicalReplicateCode()
    {
        return technicalReplicateCode;
    }

    public String getCelLoc()
    {
        return celLoc;
    }

    public String getTimeSeriesDataSetType()
    {
        return timeSeriesDataSetType;
    }

    public String getValueTypeAndUnit()
    {
        return valueTypeAndUnit;
    }
    
    public String getValueType()
    {
        Matcher matcher = VALUE_TYPE_PATTERN.matcher(valueTypeAndUnit);
        return matcher.matches() ? matcher.group(1) : valueTypeAndUnit;
    }

    public String getUnit()
    {
        Matcher matcher = VALUE_TYPE_PATTERN.matcher(valueTypeAndUnit);
        return matcher.matches() ? matcher.group(2) : "?";
    }
    
    public String getScale()
    {
        return scale;
    }

    public String getBiID()
    {
        return biID;
    }

    public String getControlledGene()
    {
        return controlledGene;
    }
    
    public String getGrowthPhase()
    {
        return growthPhase;
    }

    public String getGenotype()
    {
        return genotype;
    }


    @Override
    public boolean equals(Object obj)
    {
        return obj == this
                || (obj instanceof DataColumnHeader && ((DataColumnHeader) obj).normalizedHeader
                        .equals(normalizedHeader));
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return header;
    }
    
}