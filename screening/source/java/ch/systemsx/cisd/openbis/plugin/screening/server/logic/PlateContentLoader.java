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
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;

/**
 * Loads content of the plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateContentLoader
{
    public static TableModel loadImageAnalysisForPlate(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .loadImageAnalysisForPlate(plateId);
    }

    public static TableModel loadImageAnalysisForExperiment(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId experimentId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .loadImageAnalysisForExperiment(experimentId);
    }

    /**
     * loads data about the plate for a specified sample id. Attaches information about images and
     * image analysis only if one dataset with such a data exist.
     */
    public static PlateContent loadImagesAndMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory).getPlateContent(plateId);
    }

    /**
     * loads data about the plate for a specified dataset, which is supposed to contain images in
     * BDS-HCS format.
     */
    public static PlateImages loadImagesAndMetadataForDataset(Session session,
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
        List<WellMetadata> wells = loadWells(new TechId(HibernateUtils.getId(plate)));
        DatasetImagesReference datasetImagesReference = loadImages(translate(externalData));
        Geometry plateGeometry = getPlateGeometry(plate);
        return new PlateImages(translate(plate), wells, datasetImagesReference, plateGeometry
                .getNumberOfRows(), plateGeometry.getNumberOfColumns());
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

    private TableModel loadImageAnalysisForPlate(TechId plateId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        return loadImageAnalysis(externalDataTable, datasets);
    }

    private TableModel loadImageAnalysisForExperiment(TechId experimentId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();
        List<ExternalDataPE> datasets = loadDatasetsForExperiment(experimentId, externalDataTable);
        return loadImageAnalysis(externalDataTable, datasets);
    }

    private TableModel loadImageAnalysis(IExternalDataTable externalDataTable,
            List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> analysisDatasets =
                ScreeningUtils.filterImageAnalysisDatasets(datasets);
        List<String> datasetCodes = Code.extractCodes(analysisDatasets);
        String dataStoreCode = extractDataStoreCode(analysisDatasets);
        return DatasetReportsLoader.loadAnalysisResults(datasetCodes, dataStoreCode,
                externalDataTable);
    }

    private String extractDataStoreCode(List<ExternalDataPE> imageDatasets)
    {
        assert imageDatasets.size() > 0;
        String dataStoreCode = extractDataStoreCode(imageDatasets.get(0));
        ensureSameDataStore(imageDatasets, dataStoreCode);
        return dataStoreCode;
    }

    private String extractDataStoreCode(ExternalDataPE imageDataset)
    {
        return imageDataset.getDataStore().getCode();
    }

    private void ensureSameDataStore(List<ExternalDataPE> datasets, String dataStoreCode)
    {
        for (ExternalDataPE dataset : datasets)
        {
            String anotherDataStoreCode = extractDataStoreCode(dataset);
            if (anotherDataStoreCode.equals(dataStoreCode) == false)
            {
                throw UserFailureException
                        .fromTemplate(
                                "Datasets come from the different stores: '%s' and '%s'. Cannot perform the operation.",
                                dataStoreCode, anotherDataStoreCode);
            }
        }
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();

        Sample plate = loadPlate(plateId);
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        List<WellMetadata> wells = loadWells(plateId);

        List<ExternalDataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);
        DatasetImagesReference imageDataset = null;
        if (imageDatasets.size() == 1)
        {
            imageDataset = loadImages(translate(imageDatasets.get(0)));
        }

        List<ExternalDataPE> analysisDatasets =
                ScreeningUtils.filterImageAnalysisDatasets(datasets);
        DatasetReference analysisDataset = null;
        if (analysisDatasets.size() == 1)
        {
            analysisDataset =
                    ScreeningUtils.createDatasetReference(translate(analysisDatasets.get(0)));
        }

        Geometry plateGeometry = PlateDimensionParser.getPlateGeometry(plate.getProperties());
        int rows = plateGeometry.getNumberOfRows();
        int cols = plateGeometry.getNumberOfColumns();
        return new PlateContent(plate, wells, rows, cols, imageDataset, imageDatasets.size(),
                analysisDataset, analysisDatasets.size());
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
        return createWells(wells);
    }

    protected static List<ExternalDataPE> loadDatasets(TechId plateId,
            IExternalDataTable externalDataTable)
    {
        externalDataTable.loadBySampleTechId(plateId);
        return externalDataTable.getExternalData();
    }

    private List<ExternalDataPE> loadDatasetsForExperiment(TechId experimentId,
            IExternalDataTable externalDataTable)
    {
        externalDataTable.loadByExperimentTechId(experimentId);
        return externalDataTable.getExternalData();
    }

    private DatasetImagesReference loadImages(ExternalData dataset)
    {
        PlateImageParameters imageParameters = loadImageParams(dataset);
        return DatasetImagesReference.create(ScreeningUtils.createDatasetReference(dataset),
                imageParameters);
    }

    private PlateImageParameters loadImageParams(ExternalData dataset)
    {
        final IHCSDatasetLoader loader = businessObjectFactory.createHCSDatasetLoader(dataset);
        return PlateImageParametersFactory.create(loader);
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
        return new ListOrSearchSampleCriteria(ListOrSearchSampleCriteria
                .createForContainer(plateId));
    }
}
