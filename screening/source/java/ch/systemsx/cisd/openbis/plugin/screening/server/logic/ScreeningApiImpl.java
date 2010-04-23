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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Contains implementations of the screening public API calls.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningApiImpl
{
    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    public ScreeningApiImpl(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
    }

    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates)
    {
        List<ExternalDataPE> datasets =
                loadDatasets(plates, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        return asFeatureVectorDatasets(datasets);
    }

    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        List<ExternalDataPE> datasets = loadDatasets(plates, ScreeningConstants.IMAGE_DATASET_TYPE);
        return asImageDatasets(datasets);
    }

    // NOTE: this method is slow when a number of plates is big
    private List<ExternalDataPE> loadDatasets(List<? extends PlateIdentifier> plates,
            String datasetTypeCode)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        List<ExternalDataPE> datasets = new ArrayList<ExternalDataPE>();
        for (PlateIdentifier plate : plates)
        {
            List<ExternalDataPE> plateDatasets = loadDatasets(plate, datasetTypeCode, sampleBO);
            datasets.addAll(plateDatasets);
        }
        return datasets;
    }

    private List<ExternalDataPE> loadDatasets(PlateIdentifier plate, String datasetTypeCode,
            ISampleBO sampleBO)
    {
        sampleBO.loadBySampleIdentifier(createSampleIdentifier(plate));
        SamplePE sample = sampleBO.getSample();
        List<ExternalDataPE> datasets = daoFactory.getExternalDataDAO().listExternalData(sample);
        datasets = ScreeningUtils.filterDatasetsByType(datasets, datasetTypeCode);
        return datasets;
    }

    private static List<FeatureVectorDatasetReference> asFeatureVectorDatasets(
            List<ExternalDataPE> datasets)
    {
        List<FeatureVectorDatasetReference> result = new ArrayList<FeatureVectorDatasetReference>();
        for (ExternalDataPE externalData : datasets)
        {
            result.add(asFeatureVectorDataset(externalData));
        }
        return result;
    }

    private static FeatureVectorDatasetReference asFeatureVectorDataset(ExternalDataPE externalData)
    {
        DataStorePE dataStore = externalData.getDataStore();
        return new FeatureVectorDatasetReference(externalData.getCode(),
                dataStore.getDownloadUrl(), createPlateIdentifier(externalData));
    }

    private static List<ImageDatasetReference> asImageDatasets(List<ExternalDataPE> datasets)
    {
        List<ImageDatasetReference> result = new ArrayList<ImageDatasetReference>();
        for (ExternalDataPE externalData : datasets)
        {
            result.add(asImageDataset(externalData));
        }
        return result;
    }

    private static ImageDatasetReference asImageDataset(ExternalDataPE externalData)
    {
        DataStorePE dataStore = externalData.getDataStore();
        return new ImageDatasetReference(externalData.getCode(), dataStore.getDownloadUrl(),
                createPlateIdentifier(externalData));
    }

    private static PlateIdentifier createPlateIdentifier(ExternalDataPE externalData)
    {
        SamplePE sample = externalData.tryGetSample();
        assert sample != null : "dataset not connected to a sample: " + externalData;
        final String plateCode = sample.getCode();
        GroupPE group = sample.getGroup();
        final String spaceCodeOrNull = (group != null) ? group.getCode() : null;
        return new PlateIdentifier(plateCode, spaceCodeOrNull);
    }

    private static SampleIdentifier createSampleIdentifier(PlateIdentifier plate)
    {
        SampleOwnerIdentifier owner;
        String spaceCode = plate.tryGetSpaceCode();
        if (spaceCode != null)
        {
            SpaceIdentifier space = new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, spaceCode);
            owner = new SampleOwnerIdentifier(space);
        } else
        {
            owner = new SampleOwnerIdentifier(DatabaseInstanceIdentifier.createHome());
        }
        return SampleIdentifier.createOwnedBy(owner, plate.getPlateCode());
    }

    public List<Plate> listPlates()
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);

        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(loadPlateType());
        criteria.setIncludeSpace(true);
        criteria.setSpaceCode(null);
        criteria.setExcludeWithoutExperiment(true);

        List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(criteria));
        return asPlates(samples);
    }

    private static List<Plate> asPlates(List<Sample> samples)
    {
        List<Plate> plates = new ArrayList<Plate>();
        for (Sample sample : samples)
        {
            plates.add(asPlate(sample));
        }
        return plates;
    }

    private static Plate asPlate(Sample sample)
    {
        Experiment experiment = sample.getExperiment();
        Project project = experiment.getProject();
        Space space = sample.getSpace();
        String spaceCode = (space != null) ? space.getCode() : null;
        return new Plate(sample.getCode(), experiment.getCode(), project.getCode(), spaceCode);
    }

    private SampleType loadPlateType()
    {
        ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        SampleTypePE plateTypePE =
                sampleTypeDAO.tryFindSampleTypeByCode(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
        assert plateTypePE != null : "plate type not found";
        return SampleTypeTranslator.translate(plateTypePE, null);
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        List<IDatasetIdentifier> identifiers = new ArrayList<IDatasetIdentifier>();
        for (String datasetCode : datasetCodes)
        {
            identifiers.add(getDatasetIdentifier(externalDataBO, datasetCode));
        }
        return identifiers;
    }

    private IDatasetIdentifier getDatasetIdentifier(IExternalDataBO externalDataBO,
            String datasetCode)
    {
        externalDataBO.loadByCode(datasetCode);
        ExternalDataPE externalData = externalDataBO.getExternalData();
        return new DatasetIdentifier(datasetCode, externalData.getDataStore().getDownloadUrl());
    }
}
