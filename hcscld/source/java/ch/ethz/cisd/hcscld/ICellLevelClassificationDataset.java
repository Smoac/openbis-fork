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
 * Dataset of cell-level classification results.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelClassificationDataset extends ICellLevelDataset
{
    /**
     * Returns the classification of the given <var>cellId</var> in <var>namespace</var> of image
     * <var>imageId</var>.
     */
    public String getClassification(final ImageId imageId, ObjectNamespace namespace, int cellId);

    /**
     * Returns the classification of the given <var>cellId</var> in <var>namespace</var> of image
     * <var>imageId</var>.
     */
    public <T extends Enum<T>> T getClassification(final ImageId id, Class<T> enumClass,
            ObjectNamespace namespace, int cellId);

    /**
     * Returns the ordinal of the classification of the given <var>cellId</var> in
     * <var>namespace</var> of image <var>imageId</var>.
     */
    public int getClassificationOrdinal(final ImageId imageId, ObjectNamespace namespace, int cellId);

    /**
     * Returns the classifications of all cells in <var>namespace</var> of image <var>imageId</var>.
     */
    public String[] getClassifications(final ImageId imageId, ObjectNamespace namespace);

    /**
     * Returns the classification of the given <var>cellId</var> in <var>namespace</var> of image
     * <var>imageId</var>.
     */
    public <T extends Enum<T>> T[] getClassifications(final ImageId imageId, Class<T> enumClass,
            ObjectNamespace namespace);

    /**
     * Returns the classification ordinals of all cells in <var>namespace</var> of image
     * <var>imageId</var>.
     */
    public int[] getClassificationsOrdinal(final ImageId imageId, ObjectNamespace namespace);

    /**
     * Returns the classifications for all cells in <var>namespace</var> in all wells.
     */
    public Iterable<CellLevelClassificationsString> getClassifications(ObjectNamespace namespace);

    /**
     * Returns the classifications for all cells in <var>namespace</var> in all wells.
     */
    public <T extends Enum<T>> Iterable<CellLevelClassificationsEnum<T>> getClassifications(
            Class<T> enumClass, ObjectNamespace namespace);

    /**
     * Returns the classification ordinals for all cells in <var>namespace</var> in all wells.
     */
    public Iterable<CellLevelClassificationsOrdinal> getClassificationsOrdinal(
            ObjectNamespace namespace);

}
