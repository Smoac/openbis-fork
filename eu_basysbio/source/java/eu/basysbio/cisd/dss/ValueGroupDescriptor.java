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
 * A value group groups all value types of one data set file together.
 *
 * @author Bernd Rinn
 */
public class ValueGroupDescriptor
{
    private final String experimentType;

    private final String cultivationMethod;

    private final String biologicalReplicates;

    private final int timePoint;

    private final String timePointType;

    private final String technicalReplicates;

    private final String cellLocation;

    private final String dataSetType;

    private final String biId;

    private final String controlledGene;
    
    private final String growthPhase;
    
    private final String genotype;
    
    public ValueGroupDescriptor(DataColumnHeader dataColumnHeader)
    {
        experimentType = dataColumnHeader.getExperimentCode();
        cultivationMethod = dataColumnHeader.getCultivationMethod();
        biologicalReplicates = dataColumnHeader.getBiologicalReplicateCode();
        timePoint = dataColumnHeader.getTimePoint();
        timePointType = dataColumnHeader.getTimePointType();
        technicalReplicates = dataColumnHeader.getTechnicalReplicateCode();
        cellLocation = dataColumnHeader.getCelLoc();
        dataSetType = dataColumnHeader.getTimeSeriesDataSetType();
        biId = dataColumnHeader.getBiID();
        controlledGene = dataColumnHeader.getControlledGene();
        growthPhase = dataColumnHeader.getGrowthPhase();
        genotype = dataColumnHeader.getGenotype();
    }

    public String getExperimentType()
    {
        return experimentType;
    }

    public String getCultivationMethod()
    {
        return cultivationMethod;
    }

    public String getBiologicalReplicates()
    {
        return biologicalReplicates;
    }

    public int getTimePoint()
    {
        return timePoint;
    }

    public String getTimePointType()
    {
        return timePointType;
    }

    public String getTechnicalReplicates()
    {
        return technicalReplicates;
    }

    public String getCellLocation()
    {
        return cellLocation;
    }

    public String getDataSetType()
    {
        return dataSetType;
    }

    public String getBiId()
    {
        return biId;
    }

    public String getControlledGene()
    {
        return controlledGene;
    }

    public final String getGrowthPhase()
    {
        return growthPhase;
    }

    public final String getGenotype()
    {
        return genotype;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((biId == null) ? 0 : biId.hashCode());
        result =
                prime * result
                        + ((biologicalReplicates == null) ? 0 : biologicalReplicates.hashCode());
        result = prime * result + ((cellLocation == null) ? 0 : cellLocation.hashCode());
        result = prime * result + ((controlledGene == null) ? 0 : controlledGene.hashCode());
        result = prime * result + ((cultivationMethod == null) ? 0 : cultivationMethod.hashCode());
        result = prime * result + ((dataSetType == null) ? 0 : dataSetType.hashCode());
        result = prime * result + ((experimentType == null) ? 0 : experimentType.hashCode());
        result = prime * result + ((genotype == null) ? 0 : genotype.hashCode());
        result = prime * result + ((growthPhase == null) ? 0 : growthPhase.hashCode());
        result =
                prime * result
                        + ((technicalReplicates == null) ? 0 : technicalReplicates.hashCode());
        result = prime * result + timePoint;
        result = prime * result + ((timePointType == null) ? 0 : timePointType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ValueGroupDescriptor other = (ValueGroupDescriptor) obj;
        return equals(biId, other.biId) && equals(biologicalReplicates, other.biologicalReplicates)
                && equals(cellLocation, other.cellLocation)
                && equals(controlledGene, other.controlledGene)
                && equals(cultivationMethod, other.cultivationMethod)
                && equals(dataSetType, other.dataSetType)
                && equals(experimentType, other.experimentType) && equals(genotype, other.genotype)
                && equals(growthPhase, other.growthPhase)
                && equals(technicalReplicates, other.technicalReplicates)
                && timePoint == other.timePoint && equals(timePointType, other.timePointType);
    }

    private boolean equals(Object obj1, Object obj2)
    {
        return obj1 == null ? obj1 == obj2 : obj1.equals(obj2);
    }
    

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
