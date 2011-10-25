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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * A class that represents an image channel.
 * 
 * @since 1.9
 * @author Bernd Rinn
 */
public class ImageChannel implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String code;

    private final String label;

    private final String description;

    private final Integer wavelength;

    private final List<ImageTransformationInfo> transformations;

    public ImageChannel(String code, String label)
    {
        this.code = code;
        this.label = label;
        this.description = null;
        this.wavelength = null;
        this.transformations = Collections.emptyList();
    }

    public ImageChannel(String code, String label, String description, Integer wavelength,
            List<ImageTransformationInfo> transformations)
    {
        this.code = code;
        this.label = label;
        this.description = description;
        this.wavelength = wavelength;
        this.transformations = Collections.unmodifiableList(transformations);
    }

    /**
     * Returns the channel code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the channel label.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns the channel' description or <code>null</code>, if this channel has no description.
     */
    public String tryGetDescription()
    {
        return description;
    }

    /**
     * Returns the channel's wavelength or <code>null</code>, if the wavelength is not known.
     */
    public Integer tryGetWavelength()
    {
        return wavelength;
    }

    /**
     * Returns the transformations defined for this channel. May be empty, if there are no
     * transformations defined.
     */
    public List<ImageTransformationInfo> getTransformations()
    {
        return transformations;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
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
        ImageChannel other = (ImageChannel) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (code.equals(other.code) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ImageChannel [code=" + code + ", label=" + label + ", description=" + description
                + ", wavelength=" + wavelength + ", transformations=" + transformations + "]";
    }

}
