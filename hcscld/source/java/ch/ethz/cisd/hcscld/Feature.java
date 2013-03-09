/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

import java.util.Arrays;

import ch.ethz.cisd.hcscld.IFeatureGroup.FeatureGroupDataType;

/**
 * The description of a cell-level feature.
 * 
 * @author Bernd Rinn
 */
public class Feature
{
    /**
     * The data types of features.
     */
    public enum FeatureDataType
    {
        BOOL(FeatureGroupDataType.COMPOUND), 
        INT8(FeatureGroupDataType.INT32), 
        INT16(FeatureGroupDataType.INT32), 
        INT32(FeatureGroupDataType.INT32), 
        INT64(FeatureGroupDataType.COMPOUND), 
        FLOAT32(FeatureGroupDataType.FLOAT32), 
        FLOAT64(FeatureGroupDataType.COMPOUND), 
        STRING(FeatureGroupDataType.COMPOUND), 
        ENUM(FeatureGroupDataType.COMPOUND), 
        OTHER(FeatureGroupDataType.COMPOUND);
        
        private final FeatureGroupDataType optimalGroupType;
        
        private FeatureDataType(FeatureGroupDataType optimalGroupType)
        {
            this.optimalGroupType = optimalGroupType;
        }

        public FeatureGroupDataType getOptimalGroupType()
        {
            return optimalGroupType;
        }
    }

    private final String name;

    private final FeatureDataType type;

    private final int length;

    private final String[] optionsOrNull;

    Feature(String name, FeatureDataType type)
    {
        this.name = name;
        this.type = type;
        this.length = -1;
        this.optionsOrNull = null;
    }

    Feature(String name, int length)
    {
        this.name = name;
        this.type = FeatureDataType.STRING;
        this.length = length;
        this.optionsOrNull = null;
    }

    Feature(String name, String[] options)
    {
        this.name = name;
        this.type = FeatureDataType.ENUM;
        this.length = -1;
        this.optionsOrNull = options;
    }

    public String getName()
    {
        return name;
    }

    public FeatureDataType getType()
    {
        return type;
    }

    public int getLength()
    {
        return length;
    }

    public String[] tryGetOptions()
    {
        return optionsOrNull;
    }

    @Override
    public String toString()
    {
        if (length >= 0)
        {
            return "Feature [name=" + name + ", type=" + type + ", length=" + length + "]";
        } else if (optionsOrNull != null)
        {
            return "Feature [name=" + name + ", type=" + type + ", options="
                    + Arrays.toString(optionsOrNull) + "]";
        } else
        {
            return "Feature [name=" + name + ", type=" + type + "]";
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(optionsOrNull);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Feature other = (Feature) obj;
        if (length != other.length)
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        } else if (!name.equals(other.name))
        {
            return false;
        }
        if (!Arrays.equals(optionsOrNull, other.optionsOrNull))
        {
            return false;
        }
        if (type != other.type)
        {
            return false;
        }
        return true;
    }
}