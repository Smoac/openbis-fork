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

import java.util.BitSet;
import java.util.List;

import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValueMDArray;

/**
 * An interface for feature datasets on the cell level.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelFeatureDataset extends ICellLevelDataset
{
    /**
     * Returns all available feature groups. This list is read-only.
     */
    public List<IFeatureGroup> getFeatureGroups();

    /**
     * Returns the feature group with the given <var>id</var>.
     * 
     * @throws IllegalArgumentException If a feature group with that id doesn't exist.
     */
    public IFeatureGroup getFeatureGroup(String id) throws IllegalArgumentException;

    /**
     * Returns the number of segmented objects for the given image and object namespace.
     */
    public int getNumberOfSegmentedObjects(ImageId imageId, ObjectNamespace namespace);

    /**
     * Returns all feature values for the given <var>cellId</var> of field <var>id</var>.
     */
    public Object[] getValues(ImageId id, ObjectNamespace namespace, int cellId);

    /**
     * Returns all feature values for all cells of field <var>id</var>.
     */
    public Object[][] getValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns <code>true</code> if this dataset has values for the given parameter and
     * <code>false</code> otherwise.
     */
    public boolean hasValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns all feature values for all cells in all wells in the given <var>namespace</var>.
     */
    public Iterable<CellLevelFeatures> getValues(ObjectNamespace namespace);

    /**
     * Returns all feature values for the given <var>cellId</var> of field <var>id</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public Object[] getValues(ImageId id, int cellId) throws IllegalStateException;

    /**
     * Returns all feature values for all cells of field <var>id</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public Object[][] getValues(ImageId id) throws IllegalStateException;

    /**
     * Returns <code>true</code>, if this dataset has values for the given image id and
     * <code>false</code> otherwise.
     * 
     * @throws IllegalStateException If the dataset has more than one object name space.
     */
    public boolean hasValues(ImageId id);

    /**
     * Returns all feature values for all cells in all wells.
     * 
     * @throws IllegalStateException If the dataset has more than one feature group name space.
     */
    public Iterable<CellLevelFeatures> getValues() throws IllegalStateException;

    /**
     * Returns the feature values for the given <var>cellId</var> of field <var>id</var> in the
     * given <var>featureGroup</var>.
     */
    public Object[] getValues(ImageId id, IFeatureGroup featureGroup, int cellId);

    /**
     * Returns the feature values for all cells of field <var>id</var> in the given
     * <var>featureGroup</var>.
     * 
     * @return The feature values; the first index is the cell id, the second index is the feature
     *         index.
     */
    public Object[][] getValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns <code>true</code>, if this dataset has values for the given parameters and
     * <code>false</code> otherwise.
     */
    public boolean hasValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values for all cells in all wells in the given <var>featureGroup<var>.
     */
    public Iterable<CellLevelFeatures> getValues(IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>INT8</code>.
     */
    public MDByteArray getByteValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>INT16</code>.
     */
    public MDShortArray getShortValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>INT32</code>.
     */
    public MDIntArray getIntValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>INT64</code>.
     */
    public MDLongArray getLongValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>FLOAT32</code>.
     */
    public MDFloatArray getFloatValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the <var>featureGroup</var> is not of storage data type
     *             <code>FLOAT64</code>.
     */
    public MDDoubleArray getDoubleValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>BOOL</code>.
     */
    public BitSet[] getBoolValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the <var>featureGroup</var> is not of storage data type <code>ENUM</code>.
     */
    public HDF5EnumerationValueMDArray getEnumValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values of all feature groups of the <var>namespace</var> as an array of
     * primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>INT8</code>.
     */
    public MDByteArray getByteValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns the feature values of all feature groups of the <var>namespace</var> as an array of
     * primitive values.
     * 
     * @throws IllegalArgumentException If one of the feature groups is not of storage data type
     *             <code>INT16</code>.
     */
    public MDShortArray getShortValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns the feature values of all feature groups of the <var>namespace</var> as an array of
     * primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>INT32</code>.
     */
    public MDIntArray getIntValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns the feature values of all feature groups of the <var>namespace</var> as an array of
     * primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>INT64</code>.
     */
    public MDLongArray getLongValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns the feature values of all feature groups of the <var>namespace</var> as an array of
     * primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>FLOAT32</code>.
     */
    public MDFloatArray getFloatValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns the feature values of all feature groups of the <var>namespace</var> as an array of
     * primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>FLOAT64</code>.
     */
    public MDDoubleArray getDoubleValues(ImageId id, ObjectNamespace namespace);

    /**
     * Returns the feature values of all feature groups as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if the dataset has more than one feature group name space or if one of the
     *             feature groups is not of storage data type <code>INT8</code>.
     */
    public MDByteArray getByteValues(ImageId id);

    /**
     * Returns the feature values of all feature groups as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>INT16</code>.
     */
    public MDShortArray getShortValues(ImageId id);

    /**
     * Returns the feature values of all feature groups as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>INT32</code>.
     */
    public MDIntArray getIntValues(ImageId id);

    /**
     * Returns the feature values of all feature groups as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>INT64</code>.
     */
    public MDLongArray getLongValues(ImageId id);

    /**
     * Returns the feature values of all feature groups as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>FLOAT32</code>.
     */
    public MDFloatArray getFloatValues(ImageId id);

    /**
     * Returns the feature values of all feature groups as an array of primitive values.
     * 
     * @throws IllegalArgumentException If the dataset has more than one feature group name space or
     *             if one of the feature groups is not of storage data type <code>FLOAT64</code>.
     */
    public MDDoubleArray getDoubleValues(ImageId id);
}
