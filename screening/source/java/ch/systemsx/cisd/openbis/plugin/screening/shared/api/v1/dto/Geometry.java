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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

/**
 * Immutable class defining the width and height of something. Two instances are equal if
 * and only if the widths and heights are equal.
 *
 * @author Franz-Josef Elmer
 */
public final class Geometry implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final int width;
    private final int height;
    
    /**
     * Creates a new instance with specified width and height.
     * 
     * @throws IllegalArgumentException in case of negative arguments
     */
    public Geometry(int width, int height)
    {
        if (width < 0)
        {
            throw new IllegalArgumentException("Negative width: " + width);
        }
        if (height < 0)
        {
            throw new IllegalArgumentException("Negative height: " + height);
        }
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width.
     * 
     * @return a non-negative number.
     */
    public final int getWidth()
    {
        return width;
    }

    /**
     * Returns the height.
     * 
     * @return a non-negative number.
     */
    public final int getHeight()
    {
        return height;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Geometry == false)
        {
            return false;
        }
        Geometry geometry = (Geometry) obj;
        return geometry.width == width && geometry.height == height;
    }

    @Override
    public int hashCode()
    {
        return 37 * width + height;
    }

    /**
     * Renders this in the form <code>&lt;width&gt;x&lt;height&gt;</code>.
     */
    @Override
    public String toString()
    {
        return width + "x" + height;
    }

    
}
