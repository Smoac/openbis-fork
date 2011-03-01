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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import org.apache.commons.lang.StringUtils;

/**
 * Definition of the feature.
 * 
 * @author Tomasz Pylak
 */
public class FeatureDefinition
{
    private String code;

    private String label;

    private String description;

    public FeatureDefinition()
    {
    }

    /** Creates a feature definition, the label will be equal to the code. */
    public FeatureDefinition(String code)
    {
        this.code = code;
        this.label = code;
    }

    public String getCode()
    {
        return code;
    }

    /** Sets the code of a feature. */
    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLabel()
    {
        return label;
    }

    /** Sets the label of a feature. */
    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getDescription()
    {
        return description;
    }

    /** Sets description of a feature. */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Validates that tile number, well and channel have been specified.
     * 
     * @throws IllegalStateException if the object is not valid.
     */
    public void ensureValid()
    {
        if (StringUtils.isBlank(code))
        {
            throw new IllegalStateException("Code is not specified");
        }
        if (StringUtils.isBlank(label))
        {
            throw new IllegalStateException("Label is not specified");
        }
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeatureDefinition other = (FeatureDefinition) obj;
        if (code == null)
        {
            return other.code == null;
        } else
        {
            return code.equals(other.code);
        }
    }

}
