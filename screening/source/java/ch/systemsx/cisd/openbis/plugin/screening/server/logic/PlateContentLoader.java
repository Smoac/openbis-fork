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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Loads content of the plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateContentLoader
{
    public static PlateContent load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory).getPlateContent(plateId);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private PlateContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);

        Sample plate = loadPlate(plateId);
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);

        TileImages images = tryLoadImages(datasets, externalDataTable);
        List<WellMetadata> wells = loadWells(plateId);
        DatasetReference imageAnalysisDataset = tryFindImageAnalysisDataset(datasets);

        return new PlateContent(plate, wells, images, imageAnalysisDataset);
    }

    private Sample loadPlate(TechId plateId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(plateId);
        SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

    private DatasetReference tryFindImageAnalysisDataset(List<ExternalDataPE> datasets)
    {
        ExternalDataPE dataset =
                tryFindDataset(datasets, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        if (dataset != null)
        {
            DataStorePE dataStore = dataset.getDataStore();
            return new DatasetReference(dataset.getCode(), dataStore.getCode(), dataStore
                    .getDownloadUrl());
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

    private TileImages tryLoadImages(List<ExternalDataPE> datasets,
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

    private TileImages loadImages(IExternalDataTable externalDataTable, ExternalDataPE dataset)
    {
        DataStorePE dataStore = dataset.getDataStore();
        String datasetCode = dataset.getCode();
        List<String> datasets = Arrays.asList(datasetCode);
        String datastoreCode = dataStore.getCode();
        List<TileImage> plateReport =
                DatasetLoader.loadImages(datasets, datastoreCode, externalDataTable);
        List<PlateImageParameters> imageParamsReports =
                DatasetLoader.loadImageParameters(datasets, datastoreCode, externalDataTable);

        return TileImages.create(new DatasetReference(datasetCode, datastoreCode, dataStore
                .getDownloadUrl()), plateReport, imageParamsReports.get(0));
    }

    private static ExternalDataPE tryFindDataset(List<ExternalDataPE> datasets, String datasetType)
    {
        for (ExternalDataPE dataset : datasets)
        {
            if (dataset.getDataSetType().getCode().equals(datasetType))
            {
                return dataset;
            }
        }
        return null;
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
        well.setWellSample(wellSample);
        Material content = tryFindMaterialProperty(wellSample.getProperties());
        well.setContent(content);
        if (content != null)
        {
            Material inhibited = tryFindInhibitedMaterial(content);
            well.setGene(inhibited);
        }
        return well;
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
