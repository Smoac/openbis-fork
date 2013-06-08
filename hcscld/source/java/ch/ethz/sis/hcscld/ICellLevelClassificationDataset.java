/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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
    public int[] getClassificationsOrdinals(final ImageId imageId, ObjectNamespace namespace);

    /**
     * Returns <code>true</code> if this dataset has classifications for the given parameters and
     * <code>false</code> otherwise.
     */
    public boolean hasClassifications(ImageId id, ObjectNamespace namespace);

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
    public Iterable<CellLevelClassificationsOrdinal> getClassificationsOrdinals(
            ObjectNamespace namespace);

    //
    // Single namespace versions
    //

    /**
     * Returns the classification of the given <var>cellId</var> in image <var>imageId</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public String getClassification(final ImageId imageId, int cellId) throws IllegalStateException;

    /**
     * Returns the classification of the given <var>cellId</var> in <var>namespace</var> of image
     * <var>imageId</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public <T extends Enum<T>> T getClassification(final ImageId id, Class<T> enumClass, int cellId)
            throws IllegalStateException;

    /**
     * Returns the ordinal of the classification of the given <var>cellId</var> in image
     * <var>imageId</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public int getClassificationOrdinal(final ImageId imageId, int cellId)
            throws IllegalStateException;

    /**
     * Returns the classifications of all cells in image <var>imageId</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public String[] getClassifications(final ImageId imageId) throws IllegalStateException;

    /**
     * Returns the classification of the given <var>cellId</var> in image <var>imageId</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public <T extends Enum<T>> T[] getClassifications(final ImageId imageId, Class<T> enumClass)
            throws IllegalStateException;

    /**
     * Returns the classification ordinals of all cells in image <var>imageId</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public int[] getClassificationsOrdinals(final ImageId imageId) throws IllegalStateException;

    /**
     * Returns <code>true</code> if this dataset has classifications for the given
     * <var>imageId</var> in the only object name space and <code>false</code> otherwise.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public boolean hasClassifications(ImageId id) throws IllegalStateException;

    /**
     * Returns the classifications for all cells in <var>namespace</var> in all wells in the only
     * object name space.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public Iterable<CellLevelClassificationsString> getClassifications()
            throws IllegalStateException;

    /**
     * Returns the classifications for all cells in <var>namespace</var> in all wells in the only
     * object name space.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public <T extends Enum<T>> Iterable<CellLevelClassificationsEnum<T>> getClassifications(
            Class<T> enumClass) throws IllegalStateException;

    /**
     * Returns the classification ordinals for all cells in <var>namespace</var> in all wells in the
     * only object name space.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public Iterable<CellLevelClassificationsOrdinal> getClassificationsOrdinals()
            throws IllegalStateException;

}
