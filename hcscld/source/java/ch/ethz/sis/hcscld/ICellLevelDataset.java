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

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;

/**
 * Base interface for all cell level datasets.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelDataset
{
    /**
     * Returns the code of the dataset.
     */
    public String getDatasetCode();

    /**
     * Returns the date (including time) of creation of this dataset.
     */
    public Date getCreationDate();

    /**
     * Returns the image quantity structure (number of wells, fields and length of sequence).
     */
    public ImageQuantityStructure getImageQuantityStructure();

    /**
     * Returns the type of the dataset.
     */
    public CellLevelDatasetType getType();

    /**
     * Returns the dataset as a feature dataset.
     * 
     * @return This data set.
     * @throws WrongDatasetTypeException if {@link #getType()} !=
     *             {@link CellLevelDatasetType#FEATURES}.
     */
    public ICellLevelFeatureDataset toFeatureDataset() throws WrongDatasetTypeException;

    /**
     * Returns the dataset as a classification dataset.
     * 
     * @return This data set.
     * @throws WrongDatasetTypeException if {@link #getType()} !=
     *             {@link CellLevelDatasetType#CLASSIFICATION}.
     */
    public ICellLevelClassificationDataset toClassificationDataset()
            throws WrongDatasetTypeException;

    /**
     * Returns the dataset as an image segmentation dataset.
     * 
     * @return This data set.
     * @throws WrongDatasetTypeException if {@link #getType()} !=
     *             {@link CellLevelDatasetType#SEGMENTATION}.
     */
    public ICellLevelSegmentationDataset toSegmentationDataset() throws WrongDatasetTypeException;

    public ICellLevelTrackingDataset toTrackingDataset() throws WrongDatasetTypeException;

    /**
     * Returns the object type with the given <var>objectTypeId</var>, or <code>null</code>, if this
     * dataset doesn't have an object type with this id.
     */
    public ObjectType tryGetObjectType(String objectTypeId);

    /**
     * Returns the object type with the given <var>objectTypeId</var>.
     * 
     * @throws IllegalArgumentException If this dataset doesn't have an object type with
     *             <var>objectTypeId</var>.
     */
    public ObjectType getObjectType(String objectTypeId) throws IllegalArgumentException;

    /**
     * Returns all object types defined for this dataset.
     * <p>
     * <i>Note that the returned collection may be empty if no object types have been defined for
     * this data set.</i>
     */
    public Collection<ObjectType> getObjectTypes();

    /**
     * Returns the object namespace with the given <var>objectNamespaceId</var>, or
     * <code>null</code>, if this dataset doesn't have an object namespace with this id.
     */
    public ObjectNamespace tryGetObjectNamespace(String objectNamespaceId);

    /**
     * Returns the object namespace with the given <var>objectNamespaceId</var>.
     * 
     * @throws IllegalArgumentException If this dataset doesn't have an object namespace with
     *             <var>objectNamespaceId</var>.
     */
    public ObjectNamespace getObjectNamespace(String objectNamespaceId)
            throws IllegalArgumentException;

    /**
     * Returns all object namespaces defined for this dataset.
     */
    public Collection<ObjectNamespace> getObjectNamespaces();

    /**
     * Returns annotation for a time series image sequence.
     * 
     * @return The annotation for a time series sequence, or <code>null</code>, if this dataset has
     *         no time series sequence annotation. If the annotation exists, it will contain the
     *         time point for each image of the sequence in the order of sequence indices.
     */
    public HDF5TimeDurationArray tryGetTimeSeriesSequenceAnnotation();

    /**
     * Returns annotation for a depth scan image sequence.
     * 
     * @return The annotation for a depth scan sequence, or <code>null</code>, if this dataset has
     *         no depth scan sequence annotation. If the annotation exists, it will contain the
     *         z-value for each image of the sequence in the order of sequence indices.
     */
    public DepthScanAnnotation tryGetDepthScanSequenceAnnotation();

    /**
     * Returns custom annotation for any image sequence.
     * 
     * @return The annotation for a custom sequence, or <code>null</code>, if this dataset has no
     *         custom annotation. If the annotation exists, it will contain a description string for
     *         each image of the sequence in the order of sequence indices.
     */
    public String[] tryGetCustomSequenceAnnotation();

    /**
     * Returns the plate barcode.
     * 
     * @return The plate barcode, or <code>null</code>, if no plate barcode has been set for this
     *         dataset.
     */
    public String tryGetPlateBarcode();

    /**
     * Returns the dataset code of the parent dataset.
     * 
     * @return The dataset code of the parent dataset, or <code>null</code>, if no parent dataset
     *         code has been set for this dataset.
     */
    public String tryGetParentDatasetCode();

    /**
     * Returns the set of keys of dataset annotations available for this dataset.
     * 
     * @return The keys of all dataset annotations available for this dataset.
     */
    public Set<String> getDatasetAnnotationKeys();

    /**
     * Returns a dataset annotation.
     * 
     * @param annotationKey The key of the annotation to retrieve.
     * @return The dataset annotation, or <code>null</code>, if no such dataset annotation exists.
     */
    public String tryGetDatasetAnnotation(String annotationKey);
}
