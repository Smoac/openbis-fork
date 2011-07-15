/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * Loads content of the plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateContentLoader
{
    /**
     * Loads data about the plate for a specified sample id. Attaches information about images and
     * image analysis datasets.
     */
    public static PlateContent loadImagesAndMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory).getPlateContent(plateId);
    }

    /**
     * Loads feature vector of specified dataset with one feature specified by name.
     */
    public static FeatureVectorDataset loadFeatureVectorDataset(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, DatasetReference dataset,
            CodeAndLabel featureName)
    {
        return new PlateContentLoader(session, businessObjectFactory).fetchFeatureVector(dataset,
                featureName);
    }

    /**
     * Loads data about the plate for a specified dataset, which is supposed to contain images.
     */
    public static PlateImages loadImagesAndMetadataForDataset(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId datasetId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .getPlateContentForDataset(datasetId);
    }

    /**
     * Loads information about datasets connected to specified sample (microscopy) or a container
     * sample (HCS). In particular loads the logical images in datasets belonging to the specified
     * sample (restricted to one well in HCS case).
     */
    public static ImageSampleContent getImageDatasetInfosForSample(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .getImageDatasetInfosForSample(sampleId, wellLocationOrNull);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final LogicalImageLoader imageLoader;

    private PlateContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.imageLoader = new LogicalImageLoader(session, businessObjectFactory);
    }

    private PlateImages getPlateContentForDataset(TechId datasetId)
    {
        DataPE dataSet = loadDataset(datasetId);
        SamplePE plate = dataSet.tryGetSample();
        if (plate == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' has no sample connected.",
                    dataSet.getCode());
        }
        List<WellMetadata> wells = loadWells(new TechId(HibernateUtils.getId(plate)));
        DatasetImagesReference datasetImagesReference =
                imageLoader.loadImageDatasetReference(dataSet);
        Geometry plateGeometry = getPlateGeometry(plate);
        PlateMetadata plateMetadata =
                new PlateMetadata(translate(plate), wells, plateGeometry.getNumberOfRows(),
                        plateGeometry.getNumberOfColumns());
        return new PlateImages(plateMetadata, datasetImagesReference);
    }

    private Geometry getPlateGeometry(SamplePE plate)
    {
        List<IEntityProperty> properties =
                EntityPropertyTranslator.translate(plate.getProperties(), null);
        return PlateDimensionParser.getPlateGeometry(properties);
    }

    private DataPE loadDataset(TechId datasetId)
    {
        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadDataByTechId(datasetId);
        DataPE dataSet = dataBO.getData();
        return dataSet;
    }

    private ExternalData translate(ExternalDataPE externalData)
    {
        return DataSetTranslator.translate(externalData, session.getBaseIndexURL());
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IDataSetTable dataSetTable = createDataSetTable();

        Sample plate = loadPlate(plateId);
        List<ExternalDataPE> datasets = loadDatasets(plateId, dataSetTable);
        List<WellMetadata> wells = loadWells(plateId);

        List<ImageDatasetEnrichedReference> imageDatasetReferences =
                imageLoader.loadImageDatasets(datasets);
        List<FeatureVectorDataset> featureVectorDatasets = filterAndFetchFeatureVectors(datasets);
        List<DatasetReference> unknownDatasetReferences = extractUnknownDatasets(datasets);

        Geometry plateGeometry = PlateDimensionParser.getPlateGeometry(plate.getProperties());
        int rows = plateGeometry.getNumberOfRows();
        int cols = plateGeometry.getNumberOfColumns();
        PlateMetadata plateMetadata = new PlateMetadata(plate, wells, rows, cols);
        return new PlateContent(plateMetadata, imageDatasetReferences, featureVectorDatasets,
                unknownDatasetReferences);
    }

    private List<DatasetReference> extractUnknownDatasets(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> unknownDatasets = ScreeningUtils.filterUnknownDatasets(datasets);
        List<DatasetReference> unknownDatasetReferences = createDatasetReferences(unknownDatasets);
        return unknownDatasetReferences;
    }

    private List<FeatureVectorDataset> filterAndFetchFeatureVectors(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> analysisDatasets =
                ScreeningUtils.filterImageAnalysisDatasetsPE(datasets);
        List<DatasetReference> featureVectorDatasetReferences =
                createDatasetReferences(analysisDatasets);
        return fetchFeatureVectors(featureVectorDatasetReferences);
    }

    private FeatureVectorDataset fetchFeatureVector(DatasetReference datasetReference,
            CodeAndLabel featureName)
    {
        IHCSFeatureVectorLoader loader =
                businessObjectFactory.createHCSFeatureVectorLoader(datasetReference
                        .getDatastoreCode());
        FeatureVectorDataset result = loadFeatureVector(datasetReference, featureName, loader);
        return result;
    }

    private List<FeatureVectorDataset> fetchFeatureVectors(
            List<DatasetReference> featureVectorDatasetReferences)
    {
        List<FeatureVectorDataset> featureVectorDatasets = new ArrayList<FeatureVectorDataset>();

        List<Long> ids = new ArrayList<Long>(featureVectorDatasetReferences.size());
        for (DatasetReference featureVectorDatasetReference : featureVectorDatasetReferences)
        {
            ids.add(featureVectorDatasetReference.getId());
        }
        Map<Long, GenericEntityPropertyRecord> analysisProcedures =
                businessObjectFactory.createDatasetLister(session).fetchProperties(ids,
                        ScreeningConstants.ANALYSIS_PROCEDURE_PROPERTY);

        if (featureVectorDatasetReferences.isEmpty() == false)
        {
            for (DatasetReference datasetReference : featureVectorDatasetReferences)
            {
                IHCSFeatureVectorLoader loader =
                        businessObjectFactory.createHCSFeatureVectorLoader(datasetReference
                                .getDatastoreCode());

                GenericEntityPropertyRecord analysisProcedure =
                        analysisProcedures.get(datasetReference.getId());
                FeatureVectorDataset featureVectorDataset =
                        loadFeatureVector(datasetReference, loader,
                                analysisProcedure == null ? null : analysisProcedure.value);
                featureVectorDatasets.add(featureVectorDataset);
            }
        }
        return featureVectorDatasets;
    }

    // loads feature vector with only 1 feature
    private FeatureVectorDataset loadFeatureVector(DatasetReference datasetReference,
            IHCSFeatureVectorLoader loader, String analysisProcedure)
    {
        List<CodeAndLabel> allFeatureNames =
                loader.fetchDatasetFeatureNames(datasetReference.getCode());
        List<CodeAndLabel> featuresToLoad = allFeatureNames;
        // TODO 2011-02-25, Piotr Buczek: we don't need feature values at all
        if (featuresToLoad.size() > 1)
        {
            featuresToLoad = featuresToLoad.subList(0, 1);
        }
        WellFeatureCollection<FeatureVectorValues> featureValues =
                loader.fetchDatasetFeatureValues(Arrays.asList(datasetReference.getCode()),
                        CodeAndLabel.asCodes(featuresToLoad));
        List<FeatureVectorValues> featureVectors = featureValues.getFeatures();

        FeatureVectorDataset featureVectorDataset =
                new FeatureVectorDataset(datasetReference, featureVectors, allFeatureNames,
                        analysisProcedure);
        return featureVectorDataset;
    }

    // loads feature vector with only one feature specified by name
    private FeatureVectorDataset loadFeatureVector(DatasetReference datasetReference,
            CodeAndLabel featureName, IHCSFeatureVectorLoader loader)
    {
        List<CodeAndLabel> allFeatureNames =
                loader.fetchDatasetFeatureNames(datasetReference.getCode());

        WellFeatureCollection<FeatureVectorValues> featureValues =
                loader.fetchDatasetFeatureValues(Arrays.asList(datasetReference.getCode()),
                        Collections.singletonList(featureName.getCode()));

        List<FeatureVectorValues> featureVectors = featureValues.getFeatures();

        Map<Long, GenericEntityPropertyRecord> analysisProcedures =
                businessObjectFactory.createDatasetLister(session).fetchProperties(
                        Arrays.asList(datasetReference.getId()),
                        ScreeningConstants.ANALYSIS_PROCEDURE_PROPERTY);
        GenericEntityPropertyRecord property = analysisProcedures.get(datasetReference.getId());

        FeatureVectorDataset featureVectorDataset =
                new FeatureVectorDataset(datasetReference, featureVectors, allFeatureNames,
                        property == null ? null : property.value);
        return featureVectorDataset;
    }

    private List<DatasetReference> createDatasetReferences(List<ExternalDataPE> datasets)
    {
        List<DatasetReference> datasetReferences = new ArrayList<DatasetReference>();
        for (ExternalDataPE dataset : datasets)
        {
            datasetReferences.add(ScreeningUtils.createDatasetReference(translate(dataset)));
        }
        return datasetReferences;
    }

    private IDataSetTable createDataSetTable()
    {
        return businessObjectFactory.createDataSetTable(session);
    }

    private Sample loadPlate(TechId plateId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(plateId);
        sampleBO.enrichWithProperties();
        SamplePE sample = sampleBO.getSample();
        return translate(sample);
    }

    private Sample translate(SamplePE sample)
    {
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

    private List<WellMetadata> loadWells(TechId plateId)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        List<Sample> wells = sampleLister.list(createSamplesForContainerCriteria(plateId));
        List<Material> containedMaterials = getReferencedMaterials(wells);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        materialLister.enrichWithProperties(containedMaterials);
        return createWells(wells);
    }

    private static List<Material> getReferencedMaterials(
            List<? extends IEntityPropertiesHolder> entities)
    {
        List<Material> materials = new ArrayList<Material>();
        for (IEntityPropertiesHolder entity : entities)
        {
            List<IEntityProperty> properties = entity.getProperties();
            for (IEntityProperty prop : properties)
            {
                Material material = prop.getMaterial();
                if (material != null)
                {
                    materials.add(material);
                }
            }
        }
        return materials;
    }

    protected static List<ExternalDataPE> loadDatasets(TechId plateId, IDataSetTable dataSetTable)
    {
        dataSetTable.loadBySampleTechId(plateId);
        return dataSetTable.getExternalData();
    }

    private static List<WellMetadata> createWells(List<Sample> wellSamples)
    {
        List<WellMetadata> wells = new ArrayList<WellMetadata>();
        for (Sample wellSample : wellSamples)
        {
            wells.add(createWell(wellSample));
        }
        return wells;
    }

    private static WellMetadata createWell(Sample wellSample)
    {
        WellMetadata well = new WellMetadata();
        WellLocation locationOrNull = tryGetLocation(wellSample);
        well.setWellSample(wellSample, locationOrNull);
        return well;
    }

    private static WellLocation tryGetLocation(Sample wellSample)
    {
        return ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellSample.getSubCode());
    }

    private static ListOrSearchSampleCriteria createSamplesForContainerCriteria(TechId plateId)
    {
        return new ListOrSearchSampleCriteria(
                ListOrSearchSampleCriteria.createForContainer(plateId));
    }

    private TechId fetchContainerId(TechId wellId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(wellId);
        SamplePE sample = sampleBO.getSample();
        return new TechId(sample.getContainer().getId());
    }

    private ImageSampleContent getImageDatasetInfosForSample(TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        TechId datasetOwnerSampleId;
        if (wellLocationOrNull != null)
        {
            datasetOwnerSampleId = fetchContainerId(sampleId);
        } else
        {
            datasetOwnerSampleId = sampleId;
        }
        List<ExternalDataPE> datasets = loadDatasets(datasetOwnerSampleId, createDataSetTable());
        List<ExternalDataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);

        List<LogicalImageInfo> logicalImages = new ArrayList<LogicalImageInfo>();
        for (ExternalDataPE imageDataset : imageDatasets)
        {
            LogicalImageInfo logicalImage =
                    imageLoader.loadLogicalImageInfo(imageDataset.getCode(), imageDataset
                            .getDataStore().getCode(), wellLocationOrNull);
            logicalImages.add(logicalImage);
        }

        List<DatasetReference> unknownDatasetReferences = extractUnknownDatasets(datasets);
        return new ImageSampleContent(logicalImages, unknownDatasetReferences);
    }
}
