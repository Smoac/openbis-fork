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
 * Width and height of an image.
 *
 * @since 1.4
 * @author Franz-Josef Elmer
 */
public class ImageSize implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final int width;
    private final int height;

    /**
     * Creates an instance for specified width and height.
     */
    public ImageSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public final int getWidth()
    {
        return width;
    }

    public final int getHeight()
    {
        return height;
    }

    @Override
    public String toString()
    {
        return width + "x" + height;
    }
    
}
