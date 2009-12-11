/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * The concrete {@link IScreeningServer} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.SCREENING_PLUGIN_SERVER)
public final class ScreeningServer extends AbstractServer<IScreeningServer> implements
        IScreeningServer
{
    // name of the property which stores material (gene) inhibited by the material stored in a well
    private static final String INHIBITOR_PROPERTY_CODE = "INHIBITOR_OF";

    // type of the dataset which stores plate images, there should be at most one
    private static final String IMAGE_DATASET_TYPE = "HCS_IMAGE";

    // id of the DSS screening reporting plugin to get the images of the plate
    private static final String PLATE_VIEWER_REPORT_KEY = "plate-image-reporter";

    // id of the DSS screening reporting plugin to get the images parameters
    private static final String PLATE_IMAGE_PARAMS_REPORT_KEY = "plate-image-params-reporter";

    @Resource(name = ResourceNames.SCREENING_BUSINESS_OBJECT_FACTORY)
    private IScreeningBusinessObjectFactory businessObjectFactory;

    public ScreeningServer()
    {
    }

    @Private
    ScreeningServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IScreeningBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final IScreeningServer createLogger(final boolean invocationSuccessful,
            final long elapsedTime)
    {
        return new ScreeningServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    //
    // IScreeningServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        final SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL());
    }

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        final SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL());
    }

    public final void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        throw new NotImplementedException();
    }

    public PlateContent getPlateContent(String sessionToken, TechId plateId)
    {
        Session session = getSession(sessionToken);
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);

        PlateImages images = tryLoadImages(plateId, session);
        List<Sample> wells = loadWells(plateId, sampleLister, materialLister);

        return createPlateContent(wells, images);
    }

    private List<Sample> loadWells(TechId plateId, ISampleLister sampleLister,
            IMaterialLister materialLister)
    {
        List<Sample> wells = sampleLister.list(createSamplesForContainerCriteria(plateId));
        List<Material> containedMaterials = getReferencedMaterials(wells);
        materialLister.enrichWithProperties(containedMaterials);
        List<Material> genes = getInhibitedMaterials(containedMaterials, INHIBITOR_PROPERTY_CODE);
        materialLister.enrichWithProperties(genes);
        return wells;
    }

    private PlateImages tryLoadImages(TechId plateId, Session session)
    {
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadBySampleTechId(plateId);
        List<ExternalDataPE> externalData = externalDataTable.getExternalData();
        ExternalDataPE dataset = tryFindDataset(externalData, IMAGE_DATASET_TYPE);
        if (dataset != null)
        {
            return loadImages(externalDataTable, dataset);
        } else
        {
            return null;
        }
    }

    private PlateImages loadImages(IExternalDataTable externalDataTable, ExternalDataPE dataset)
    {
        DataStorePE dataStore = dataset.getDataStore();
        String datasetCode = dataset.getCode();
        List<String> datasets = Arrays.asList(datasetCode);
        String datastoreCode = dataStore.getCode();
        TableModel plateReport =
                externalDataTable.createReportFromDatasets(PLATE_VIEWER_REPORT_KEY, datastoreCode,
                        datasets);
        TableModel imageParamsReport =
                externalDataTable.createReportFromDatasets(PLATE_IMAGE_PARAMS_REPORT_KEY,
                        datastoreCode, datasets);

        return PlateImage.createImages(datasetCode, dataStore.getDownloadUrl(), plateReport,
                imageParamsReport);
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

    private static PlateContent createPlateContent(List<Sample> wellSamples,
            PlateImages imagesOrNull)
    {
        List<WellMetadata> wells = createWells(wellSamples);
        return new PlateContent(wells, imagesOrNull);
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
                tryFindProperty(content.getProperties(), INHIBITOR_PROPERTY_CODE);
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
