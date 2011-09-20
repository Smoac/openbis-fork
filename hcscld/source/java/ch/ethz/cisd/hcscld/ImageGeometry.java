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

/**
 * An image geometry object.
 *
 * @author Bernd Rinn
 */
public class ImageGeometry
{
    int width, height;

    // Used by JHDF5 when constructing the geometry from a compound. 
    ImageGeometry()
    {
    }
    
    public ImageGeometry(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the number of pixels of the image in x dimension.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Returns the number of pixels of the image in y dimension.
     */
    public int getHeight()
    {
        return height;
    }

}
