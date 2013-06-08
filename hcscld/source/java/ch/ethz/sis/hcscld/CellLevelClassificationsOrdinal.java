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

package ch.ethz.sis.hcscld;

/**
 * The cell level classifications as ordinal values for a given image.
 *
 * @author Bernd Rinn
 */
public class CellLevelClassificationsOrdinal
{
    private final ImageId id;

    private final int[] data;

    CellLevelClassificationsOrdinal(ImageId id, int[] data)
    {
        this.id = id;
        this.data = data;
    }

    /**
     * Returns the well field id of this classification results.
     */
    public ImageId getId()
    {
        return id;
    }

    /**
     * Returns the classification results represented by their ordinal.
     */
    public int[] getData()
    {
        return data;
    }

}
