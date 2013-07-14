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

import ch.ethz.sis.hcscld.IFeatureGroup.FeatureGroupDataType;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValueMDArray;

/**
 * An interface for a writable dataset of cell-level features.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelFeatureWritableDataset extends ICellLevelWritableDataset,
        ICellLevelFeatureDataset
{
    /**
     * Creates an (empty) feature definition object with given <var>namespace</var> to define the
     * features of a feature group.
     * 
     * @param namespace The namespace of this feature definition.
     */
    public IFeaturesDefinition createFeaturesDefinition(ObjectNamespace namespace);

    /**
     * Writes the values of the feature group to the dataset.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, Object[][] featureValues);

    /**
     * Writes the values of the features to the dataset.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the default feature group.
     */
    public void writeFeatures(ImageId id, Object[][] featureValues);

    //
    // Write methods for Uniform group storage data types.
    //

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT32}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, MDFloatArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT32}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, float[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#BOOL}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The array index is the object id, the
     *            bitset index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, BitSet[] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT64}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, MDDoubleArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT64}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, double[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT8}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, MDByteArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT8}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, byte[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT16}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, MDShortArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT16}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, short[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT32}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, MDIntArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT32}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, int[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT64}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, MDLongArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT64}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, long[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#ENUM}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup,
            HDF5EnumerationValueMDArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#ENUM}.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     * @throws IllegalArgumentException if <var>T</var> is not compatible with
     *        <var>featureGroup</var>.
     */
    public <T extends Enum<T>> void writeFeatures(ImageId id, IFeatureGroup featureGroup,
            T[][] featureValues);


    //
    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    //

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT32}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, MDFloatArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT32}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, float[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#BOOL}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The array index is the object id, the
     *            bitset index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, BitSet[] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT64}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, MDDoubleArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#FLOAT64}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, double[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT8}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, MDByteArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT8}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, byte[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT16}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, MDShortArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT16}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, short[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT32}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, MDIntArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT32}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, int[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT64}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, MDLongArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#INT64}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, long[][] featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#ENUM}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. It has to be of rank 2. The first index is
     *            the object id, the second index is the feature index as defined by the
     *            <var>featureGroup</var>.
     */
    public void writeFeatures(ImageId id, HDF5EnumerationValueMDArray featureValues);

    /**
     * Writes the values of the feature group to the dataset. This method is only allowed if
     * {@link IFeatureGroup#getDataType()} equals {@link FeatureGroupDataType#ENUM}.
     * <p>
     * <i>This method is only valid to use if the image data set has exactly one feature group which
     * has been created before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     * @throws IllegalArgumentException if <var>T</var> is not compatible with
     *        <var>featureGroup</var>.
     */
    public <T extends Enum<T>> void writeFeatures(ImageId id, T[][] featureValues);
}
