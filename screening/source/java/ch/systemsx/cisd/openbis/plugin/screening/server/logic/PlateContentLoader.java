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
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;
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
     * Loads data about the plate for a specified dataset, which is supposed to contain images.
     */
    public static PlateImages loadImagesAndMetadataForDataset(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId datasetId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .getPlateContentForDataset(datasetId);
    }

    /**
     * Loads information about all channels stacks of a given well in a given dataset.
     */
    public static List<WellImageChannelStack> loadImageChannelStacks(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        return new PlateContentLoader(session, businessObjectFactory).loadImageChannelStacks(
                datasetCode, datastoreCode, wellLocationOrNull);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private PlateContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
    }

    private PlateImages getPlateContentForDataset(TechId datasetId)
    {
        ExternalDataPE externalData = loadDataset(datasetId);
        SamplePE plate = externalData.tryGetSample();
        if (plate == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' has no sample connected.",
                    externalData.getCode());
        }
        List<WellMetadata> wells = loadWells(new TechId(HibernateUtils.getId(plate)));
        DatasetImagesReference datasetImagesReference = loadImages(translate(externalData));
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

    private ExternalDataPE loadDataset(TechId datasetId)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        externalDataBO.loadDataByTechId(datasetId);
        ExternalDataPE externalData = externalDataBO.getExternalData();
        return externalData;
    }

    private ExternalData translate(ExternalDataPE externalData)
    {
        return ExternalDataTranslator.translate(externalData, session.getBaseIndexURL());
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();

        Sample plate = loadPlate(plateId);
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        List<WellMetadata> wells = loadWells(plateId);

        List<ExternalDataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);
        List<DatasetImagesReference> imageDatasetReferences =
                translateAndFetchImageParams(imageDatasets);

        List<FeatureVectorDataset> featureVectorDatasets = filterAndFetchFeatureVectors(datasets);

        List<ExternalDataPE> unknownDatasets = ScreeningUtils.filterUnknownDatasets(datasets);
        List<DatasetReference> unknownDatasetReferences = createDatasetReferences(unknownDatasets);

        Geometry plateGeometry = PlateDimensionParser.getPlateGeometry(plate.getProperties());
        int rows = plateGeometry.getNumberOfRows();
        int cols = plateGeometry.getNumberOfColumns();
        PlateMetadata plateMetadata = new PlateMetadata(plate, wells, rows, cols);
        return new PlateContent(plateMetadata, imageDatasetReferences, featureVectorDatasets,
                unknownDatasetReferences);
    }

    private List<FeatureVectorDataset> filterAndFetchFeatureVectors(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> analysisDatasets =
                ScreeningUtils.filterImageAnalysisDatasets(datasets);
        List<DatasetReference> featureVectorDatasetReferences =
                createDatasetReferences(analysisDatasets);
        return fetchFeatureVectors(featureVectorDatasetReferences);
    }

    private List<FeatureVectorDataset> fetchFeatureVectors(
            List<DatasetReference> featureVectorDatasetReferences)
    {
        List<FeatureVectorDataset> featureVectorDatasets = new ArrayList<FeatureVectorDataset>();
        for (DatasetReference datasetReference : featureVectorDatasetReferences)
        {
            IHCSFeatureVectorLoader loader =
                    businessObjectFactory.createHCSFeatureVectorLoader(datasetReference
                            .getDatastoreCode());
            WellFeatureCollection<FeatureVectorValues> featureValues =
                    loader.fetchDatasetFeatureValues(datasetReference.getCode());
            FeatureVectorDataset featureVectorDataset =
                    new FeatureVectorDataset(datasetReference, featureValues.getFeatures(),
                            featureValues.getFeatureLabels());
            featureVectorDatasets.add(featureVectorDataset);
        }
        return featureVectorDatasets;
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

    private List<DatasetImagesReference> translateAndFetchImageParams(
            List<ExternalDataPE> imageDatasets)
    {
        List<DatasetImagesReference> datasetImagesReferences =
                new ArrayList<DatasetImagesReference>();
        for (ExternalDataPE imageDataset : imageDatasets)
        {
            datasetImagesReferences.add(loadImages(translate(imageDataset)));
        }
        return datasetImagesReferences;
    }

    private IExternalDataTable createExternalDataTable()
    {
        return businessObjectFactory.createExternalDataTable(session);
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

    protected static List<ExternalDataPE> loadDatasets(TechId plateId,
            IExternalDataTable externalDataTable)
    {
        externalDataTable.loadBySampleTechId(plateId);
        return externalDataTable.getExternalData();
    }

    private DatasetImagesReference loadImages(ExternalData dataset)
    {
        PlateImageParameters imageParameters =
                ScreeningUtils.loadImageParameters(dataset, businessObjectFactory);
        return DatasetImagesReference.create(ScreeningUtils.createDatasetReference(dataset),
                imageParameters);
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

    private List<WellImageChannelStack> loadImageChannelStacks(String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        IHCSDatasetLoader datasetLoader =
                businessObjectFactory.createHCSDatasetLoader(datasetCode, datastoreCode);
        List<WellImageChannelStack> stacks =
                datasetLoader.listImageChannelStacks(wellLocationOrNull);
        return stacks;
    }
}
