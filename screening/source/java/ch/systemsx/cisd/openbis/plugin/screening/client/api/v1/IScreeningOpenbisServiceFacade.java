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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.IOException;
import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A client side facade of openBIS and Datastore Server API. Since version 1.2 of the API features
 * are no longer identified by a name but by a code. Previous client code still works but all name
 * will be normalized internally. Normalized means that the original code arguments turn to upper
 * case and any symbol which isn't from A-Z or 0-9 is replaced by an underscore character.
 * {@link FeatureVectorDataset} will provide feature codes and feature labels.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IScreeningOpenbisServiceFacade
{

    /**
     * Return the session token for this authenticated user.
     */
    public String getSessionToken();

    /** Closes connection with the server. After calling this method this facade cannot be used. */
    public void logout();

    /**
     * Return the list of all visible plates assigned to any experiment, along with their
     * hierarchical context (space, project, experiment).
     */
    public List<Plate> listPlates();

    /**
     * Return the list of all visible experiments, along with their hierarchical context (space,
     * project).
     */
    public List<ExperimentIdentifier> listExperiments();

    /**
     * For a given set of plates provides the list of all connected data sets containing feature
     * vectors.
     */
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates);

    /**
     * For a given set of plates provides the list of all connected data sets containing images.
     */
    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates);

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to
     * the specified <var>materialIdentifier</var>. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     * <p>
     * For how to get the feature vectors, see
     * {@link #convertToFeatureVectorDatasetWellIdentifier(List)}.
     */
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets);

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to
     * it. If <code>findDatasets == true</code>, find also the connected image and image analysis
     * data sets for the relevant plates.
     */
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            MaterialIdentifier materialIdentifier, boolean findDatasets);

    /**
     * Converts the given list of {@link PlateWellReferenceWithDatasets} into a list of
     * {@link FeatureVectorDatasetWellReference}.
     * 
     * @see #listPlateWells(ExperimentIdentifier, MaterialIdentifier, boolean)
     * @see #loadFeaturesForDatasetWellReferences(List, List)
     */
    public List<FeatureVectorDatasetWellReference> convertToFeatureVectorDatasetWellIdentifier(
            List<PlateWellReferenceWithDatasets> plateWellReferenceWithDataSets);

    /**
     * Converts a given list of dataset codes to dataset identifiers which can be used in other API
     * calls.
     */
    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes);

    /**
     * For a given set of feature vector data sets provides the list of all available features. This
     * is just the name of the feature. If for different data sets different sets of features are
     * available, provides the union of the feature names of all data sets.
     */
    @Deprecated
    public List<String> listAvailableFeatureNames(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of feature vector data sets provides the list of all available features. This
     * is just the code of the feature. If for different data sets different sets of features are
     * available, provides the union of the feature names of all data sets.
     */
    public List<String> listAvailableFeatureCodes(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of plates and a set of features (given by their code), provide all the
     * feature vectors.
     * 
     * @param plates The plates to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all
     *            available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the
     *         <var>featureDatasets</var>.
     */
    public List<FeatureVectorDataset> loadFeaturesForPlates(List<? extends PlateIdentifier> plates,
            final List<String> featureCodesOrNull);

    /**
     * For a given set of data sets and a set of features (given by their code), provide all the
     * feature vectors.
     * 
     * @param featureDatasets The data sets to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all
     *            available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the
     *         <var>featureDatasets</var>.
     */
    public List<FeatureVectorDataset> loadFeatures(
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodesOrNull);

    /**
     * Conceptually, for a given list of dataset well references (i.e. specified wells on specified
     * feature vector data sets) and a set of features (given by their code) provide the feature
     * matrix. In this matrix, each column is one feature, each row is one well in one data set.
     * <p>
     * Physically, the result is delivered as a list of feature vectors. Each entry in this list
     * corresponds to one well in one dataset.
     * 
     * @param datasetWellReferences The references for datasets / wells to get the feature vectors
     *            for.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or
     *            <code>null</code>, if all available features should be included. Note that for an
     *            empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s, each element corresponds to one of
     *         the <var>datasetWellReferences</var>. <b>Note that the order of the returned list is
     *         <i>not</i> guaranteed to be the same as the order of the list
     *         <var>datasetWellReferences</var>. Use
     *         {@link FeatureVectorWithDescription#getDatasetWellReference()} to find the
     *         corresponding dataset / well.</b>
     */
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodesOrNull);

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to
     * the specified <var>materialIdentifier</var> and load the feature vectors for the given
     * feature code if not <code>null</code>, or all available features otherwise.
     * 
     * @param experimentIdentifer The identifier of the experiment to get the feature vectors for
     * @param materialIdentifier The identifier of the material contained in the wells to get the
     *            feature vectors for.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or
     *            <code>null</code>, if all available features should be included. Note that for an
     *            empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s found in the given
     *         <var>experimentIdentifer</var> and connected with the given
     *         <var>materialIdentifier</var>.
     */
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            List<String> featureCodesOrNull);

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to it
     * and load the feature vectors for the given feature code if not <code>null</code>, or all
     * available features otherwise.
     * 
     * @param materialIdentifier The identifier of the material contained in the wells to get the
     *            feature vectors for.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or
     *            <code>null</code>, if all available features should be included. Note that for an
     *            empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s found in the given
     *         <var>experimentIdentifer</var> and connected with the given
     *         <var>materialIdentifier</var>.
     */
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, List<String> featureCodesOrNull);

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the provided output streams. Output streams will not be closed
     * automatically.<br/>
     * The images will be converted to PNG format before being shipped.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams
     *             fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IImageOutputStreamProvider outputStreamProvider) throws IOException;

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the provided output streams. Output streams will not be closed
     * automatically.<br/>
     * If <code>convertToPng==true</code>, the images will be converted to PNG format before being
     * shipped, otherwise they will be shipped in the format that they are stored on the server.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams
     *             fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IImageOutputStreamProvider outputStreamProvider, boolean convertToPNG)
            throws IOException;
    
    /**
     * Loads original images or thumbnails for a specified data set, a list of well positions (empty
     * list means all wells), a channel, and an optional thumb nail size. Images of all tiles are
     * delivered. If thumb nail size isn't specified the original image is delivered otherwise a
     * thumb nail image with same aspect ratio as the original image but which fits into specified
     * size will be delivered.
     * 
     * @return a list of byte arrays where each array contains a PNG encoded image.
     */
    public List<byte[]> loadImages(IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
            throws IOException;
    
    /**
     * Saves the specified transformer factory for the specified channel and the experiment to
     * which the specified data sets belong.
     */
    public void saveImageTransformerFactory(List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory);

    /**
     * Returns the transformer factory for the specified channel and the experiment to which
     * the specified data sets belong.
     * 
     * @return <code>null</code> if such a factory has been defined yet.
     */
    public IImageTransformerFactory getImageTransformerFactoryOrNull(
            List<IDatasetIdentifier> dataSetIdentifiers, String channel);

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    public List<ImageDatasetMetadata> listImageMetadata(
            List<? extends IImageDatasetIdentifier> imageDatasets);

    /**
     * For a given list of <var>plates</var>, return the mapping of plate wells to materials
     * contained in each well.
     * 
     * @param plates The list of plates to get the mapping for
     * @param materialTypeIdentifierOrNull If not <code>null</code>, consider only materials of the
     *            given type for the mapping.
     * @return A list of well to material mappings, one element for each plate.
     */
    public List<PlateWellMaterialMapping> listPlateMaterialMapping(
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull);

}