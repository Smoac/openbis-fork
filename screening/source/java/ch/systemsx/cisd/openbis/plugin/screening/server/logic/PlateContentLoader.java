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
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Loads content of the plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateContentLoader
{
    /**
     * loads data about the plate for a specified sample id. Attaches information about images and
     * image analysis only if one dataset with such a data exist.
     */
    public static PlateContent load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory).getPlateContent(plateId);
    }

    /**
     * loads data about the plate for a specified dataset, which is supposed to contain images in
     * BDS-HCS format.
     */
    public static PlateImages loadForDataset(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId datasetId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .getPlateContentForDataset(datasetId);
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
        List<WellMetadata> wells = loadWells(new TechId(plate.getId()));
        DatasetImagesReference datasetImagesReference =
                loadImages(businessObjectFactory.createExternalDataTable(session), externalData);
        return new PlateImages(translate(plate), wells, datasetImagesReference);
    }

    private ExternalDataPE loadDataset(TechId datasetId)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        externalDataBO.loadDataByTechId(datasetId);
        ExternalDataPE externalData = externalDataBO.getExternalData();
        return externalData;
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);

        Sample plate = loadPlate(plateId);
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        List<WellMetadata> wells = loadWells(plateId);

        DatasetImagesReference images = null;
        int imageDatasetsNumber = countDatasets(datasets, ScreeningConstants.IMAGE_DATASET_TYPE);
        if (imageDatasetsNumber == 1)
        {
            images = tryLoadImages(datasets, externalDataTable);
        }

        int imageAnalysisDatasetsNumber =
                countDatasets(datasets, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        DatasetReference imageAnalysisDataset = null;
        if (imageAnalysisDatasetsNumber == 1)
        {
            imageAnalysisDataset = tryFindImageAnalysisDataset(datasets);
        }

        return new PlateContent(plate, wells, images, imageDatasetsNumber, imageAnalysisDataset,
                imageAnalysisDatasetsNumber);
    }

    private Sample loadPlate(TechId plateId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(plateId);
        SamplePE sample = sampleBO.getSample();
        return translate(sample);
    }

    private Sample translate(SamplePE sample)
    {
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

    private DatasetReference tryFindImageAnalysisDataset(List<ExternalDataPE> datasets)
    {
        ExternalDataPE dataset =
                tryFindDataset(datasets, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        if (dataset != null)
        {
            return ScreeningUtils.createDatasetReference(dataset);
        } else
        {
            return null;
        }
    }

    private List<WellMetadata> loadWells(TechId plateId)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);

        List<Sample> wells = sampleLister.list(createSamplesForContainerCriteria(plateId));
        List<Material> containedMaterials = getReferencedMaterials(wells);
        materialLister.enrichWithProperties(containedMaterials);
        List<Material> genes =
                getInhibitedMaterials(containedMaterials,
                        ScreeningConstants.INHIBITOR_PROPERTY_CODE);
        materialLister.enrichWithProperties(genes);
        return createWells(wells);
    }

    private DatasetImagesReference tryLoadImages(List<ExternalDataPE> datasets,
            IExternalDataTable externalDataTable)
    {
        ExternalDataPE dataset = tryFindDataset(datasets, ScreeningConstants.IMAGE_DATASET_TYPE);
        if (dataset != null)
        {
            return loadImages(externalDataTable, dataset);
        } else
        {
            return null;
        }
    }

    protected static List<ExternalDataPE> loadDatasets(TechId plateId,
            IExternalDataTable externalDataTable)
    {
        externalDataTable.loadBySampleTechId(plateId);
        List<ExternalDataPE> externalData = externalDataTable.getExternalData();
        return externalData;
    }

    private DatasetImagesReference loadImages(IExternalDataTable externalDataTable,
            ExternalDataPE dataset)
    {
        PlateImageParameters imageParameters = loadImageParams(dataset, externalDataTable);
        return DatasetImagesReference.create(ScreeningUtils.createDatasetReference(dataset),
                imageParameters);
    }

    private PlateImageParameters loadImageParams(ExternalDataPE dataset,
            IExternalDataTable externalDataTable)
    {
        DataStorePE dataStore = dataset.getDataStore();
        String datasetCode = dataset.getCode();
        List<String> datasets = Arrays.asList(datasetCode);
        List<PlateImageParameters> imageParamsReports =
                DatasetLoader.loadImageParameters(datasets, dataStore.getCode(), externalDataTable);
        assert imageParamsReports.size() == 1;
        return imageParamsReports.get(0);
    }

    private static int countDatasets(List<ExternalDataPE> datasets, String datasetType)
    {
        int counter = 0;
        for (ExternalDataPE dataset : datasets)
        {
            if (isTypeEqual(dataset, datasetType))
            {
                counter++;
            }
        }
        return counter;
    }

    private static ExternalDataPE tryFindDataset(List<ExternalDataPE> datasets, String datasetType)
    {
        for (ExternalDataPE dataset : datasets)
        {
            if (isTypeEqual(dataset, datasetType))
            {
                return dataset;
            }
        }
        return null;
    }

    private static boolean isTypeEqual(ExternalDataPE dataset, String datasetType)
    {
        return dataset.getDataSetType().getCode().equals(datasetType);
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
        Material content = tryFindMaterialProperty(wellSample.getProperties());
        well.setContent(content);
        if (content != null)
        {
            Material inhibited = tryFindInhibitedMaterial(content);
            well.setGene(inhibited);
        }
        return well;
    }

    private static WellLocation tryGetLocation(Sample wellSample)
    {
        return ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellSample.getSubCode());
    }

    private static Material tryFindInhibitedMaterial(Material content)
    {
        IEntityProperty property =
                tryFindProperty(content.getProperties(), ScreeningConstants.INHIBITOR_PROPERTY_CODE);
        if (property != null)
        {
            Material material = property.getMaterial();
            assert material != null : "Material property expected, but got: " + property;
            return material;
        } else
        {
            return null;
        }
    }

    private static List<Material> getInhibitedMaterials(List<Material> materials,
            String propertyCode)
    {
        List<Material> inhibitedMaterials = new ArrayList<Material>();
        for (Material material : materials)
        {
            Material inhibitedMaterial = tryFindInhibitedMaterial(material);
            if (inhibitedMaterial != null)
            {
                inhibitedMaterials.add(inhibitedMaterial);
            }
        }
        return inhibitedMaterials;
    }

    private static IEntityProperty tryFindProperty(List<IEntityProperty> properties,
            String propertyCode)
    {
        for (IEntityProperty prop : properties)
        {
            if (prop.getPropertyType().getCode().equals(propertyCode))
            {
                return prop;
            }
        }
        return null;
    }

    private static Material tryFindMaterialProperty(List<IEntityProperty> properties)
    {
        for (IEntityProperty prop : properties)
        {
            if (prop.getMaterial() != null)
            {
                return prop.getMaterial();
            }
        }
        return null;
    }

    private static List<Material> getReferencedMaterials(
            List<? extends IEntityPropertiesHolder> entities)
    {
        List<Material> materials = new ArrayList<Material>();
        for (IEntityPropertiesHolder entity : entities)
        {
            Material material = tryFindMaterialProperty(entity.getProperties());
            if (material != null)
            {
                materials.add(material);
            }
        }
        return materials;
    }

    private static ListOrSearchSampleCriteria createSamplesForContainerCriteria(TechId plateId)
    {
        return new ListOrSearchSampleCriteria(ListOrSearchSampleCriteria
                .createForContainer(plateId));
    }
}
