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
 * Writable dataset of cell-level classification results.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelClassificationWritableDataset extends ICellLevelClassificationDataset,
        ICellLevelWritableDataset
{
    /**
     * Writes out the classification values for the given well and field.
     * 
     * @param imageId The well and field id.
     * @param namespace The object namespace to write the classification for. 
     * @param classifications The classification values.
     */
    public void writeClassification(ImageId imageId, ObjectNamespace namespace, Enum<?>[] classifications);

    /**
     * Writes out the classification values for the given well and field.
     * 
     * @param imageId The well and field id.
     * @param namespace The object namespace to write the classification for. 
     * @param classifications The classification values.
     */
    public void writeClassification(ImageId imageId, ObjectNamespace namespace,
            String[] classifications);

    /**
     * Writes out the classification values for the given well and field.
     * 
     * @param imageId The well and field id.
     * @param namespace The object namespace to write the classification for. 
     * @param classificationOrdinals The ordinal values of the classification as defined by
     *            {@link ICellLevelDataWriter#addClassificationDataset(String, ImageQuantityStructure, java.util.List)}
     *            .
     */
    public void writeClassification(ImageId imageId, ObjectNamespace namespace, int[] classificationOrdinals);

}
