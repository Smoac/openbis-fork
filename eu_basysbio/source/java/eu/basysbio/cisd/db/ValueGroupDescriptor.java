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

    public ValueGroupDescriptor(ValueGroupDescriptor descr, String replacementBiId, int replacementTimePoint)
    {
        experimentType = descr.experimentType;
        cultivationMethod = descr.cultivationMethod;
        biologicalReplicates = descr.biologicalReplicates;
        timePoint = replacementTimePoint;
        timePointType = descr.timePointType;
        technicalReplicates = descr.technicalReplicates;
        cellLocation = descr.cellLocation;
        dataSetType = descr.dataSetType;
        biId = replacementBiId;
        controlledGene = descr.controlledGene;
    }
    
    public ValueGroupDescriptor(String[] headerFields)
    {
        experimentType = headerFields[0];
        cultivationMethod = headerFields[1];
        biologicalReplicates = headerFields[2];
        if ("NT".equals(headerFields[3]))
        {
            timePoint = Integer.MIN_VALUE;
        } else
        {
            timePoint =
                Integer.parseInt(headerFields[3].startsWith("+") ? headerFields[3].substring(1)
                        : headerFields[3]);
        }
        timePointType = headerFields[4];
        technicalReplicates = headerFields[5];
        cellLocation = headerFields[6];
        dataSetType = headerFields[7];
        biId = headerFields[10];
        controlledGene = headerFields[11];
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
        if (biId == null)
        {
            if (other.biId != null)
            {
                return false;
            }
        } else if (false == biId.equals(other.biId))
        {
            return false;
        }
        if (biologicalReplicates == null)
        {
            if (other.biologicalReplicates != null)
            {
                return false;
            }
        } else if (false == biologicalReplicates.equals(other.biologicalReplicates))
        {
            return false;
        }
        if (cellLocation == null)
        {
            if (other.cellLocation != null)
            {
                return false;
            }
        } else if (false == cellLocation.equals(other.cellLocation))
        {
            return false;
        }
        if (controlledGene == null)
        {
            if (other.controlledGene != null)
            {
                return false;
            }
        } else if (false == controlledGene.equals(other.controlledGene))
        {
            return false;
        }
        if (cultivationMethod == null)
        {
            if (other.cultivationMethod != null)
            {
                return false;
            }
        } else if (false == cultivationMethod.equals(other.cultivationMethod))
        {
            return false;
        }
        if (dataSetType == null)
        {
            if (other.dataSetType != null)
            {
                return false;
            }
        } else if (false == dataSetType.equals(other.dataSetType))
        {
            return false;
        }
        if (experimentType == null)
        {
            if (other.experimentType != null)
            {
                return false;
            }
        } else if (false == experimentType.equals(other.experimentType))
        {
            return false;
        }
        if (technicalReplicates == null)
        {
            if (other.technicalReplicates != null)
            {
                return false;
            }
        } else if (false == technicalReplicates.equals(other.technicalReplicates))
        {
            return false;
        }
        if (timePoint != other.timePoint)
        {
            return false;
        }
        if (timePointType == null)
        {
            if (other.timePointType != null)
            {
                return false;
            }
        } else if (false == timePointType.equals(other.timePointType))
        {
            return false;
        }
        return true;
    }

}
