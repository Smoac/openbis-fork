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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
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
        List<FeatureVectorDatasetReference> result = new ArrayList<FeatureVectorDatasetReference>();
        List<ImageDatasetReference> imageDatasets = listImageDatasets(plates);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        Set<ExperimentPE> visitedExperiments = new HashSet<ExperimentPE>();
        for (PlateIdentifier plate : plates)
        {
            ExperimentPE experiment = tryGetExperiment(sampleBO, plate);
            if (experiment != null && visitedExperiments.contains(experiment) == false)
            {
                List<ExternalDataPE> datasets =
                        daoFactory.getExternalDataDAO().listExternalData(experiment);
                List<ExternalDataPE> childrenDatasets = filterChildren(imageDatasets, datasets);
                List<ExternalDataPE> featureVectorDatasets =
                        ScreeningUtils.filterDatasetsByType(childrenDatasets,
                                ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
                result.addAll(asFeatureVectorDatasets(featureVectorDatasets));
                visitedExperiments.add(experiment);
            }
        }
        return result;
    }

    // return those datasets which have exactly one parent, which is contained in the parent set
    private static List<ExternalDataPE> filterChildren(
            List<? extends IDatasetIdentifier> parentDatasets, List<ExternalDataPE> datasets)
    {
        Set<String> parentDatasetCodes = createDatasetCodesSet(parentDatasets);
        List<ExternalDataPE> children = new ArrayList<ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            Set<DataPE> parents = dataset.getParents();
            if (parents.size() == 1)
            {
                DataPE parent = parents.iterator().next();
                if (parentDatasetCodes.contains(parent.getCode()))
                {
                    children.add(dataset);
                }
            }
        }
        return children;
    }

    private static Set<String> createDatasetCodesSet(List<? extends IDatasetIdentifier> datasets)
    {
        Set<String> result = new HashSet<String>();
        for (IDatasetIdentifier dataset : datasets)
        {
            result.add(dataset.getDatasetCode());
        }
        return result;
    }

    private ExperimentPE tryGetExperiment(ISampleBO sampleBO, PlateIdentifier plate)
    {
        sampleBO.loadBySampleIdentifier(createSampleIdentifier(plate));
        SamplePE sample = sampleBO.getSample();
        return sample.getExperiment();
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
        DataPE parentDataset = externalData.getParents().iterator().next();
        return new FeatureVectorDatasetReference(externalData.getCode(),
                dataStore.getDownloadUrl(), createPlateIdentifier(parentDataset),
                extractPlateGemoetry(externalData), externalData.getRegistrationDate(),
                asImageDataset(parentDataset));
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

    private static ImageDatasetReference asImageDataset(DataPE parentDataset)
    {
        DataStorePE dataStore = parentDataset.getDataStore();
        return new ImageDatasetReference(parentDataset.getCode(), dataStore.getDownloadUrl(),
                createPlateIdentifier(parentDataset), extractPlateGemoetry(parentDataset),
                parentDataset.getRegistrationDate());
    }

    private static PlateIdentifier createPlateIdentifier(DataPE parentDataset)
    {
        SamplePE sample = getSample(parentDataset);
        final String plateCode = sample.getCode();
        GroupPE group = sample.getGroup();
        final String spaceCodeOrNull = (group != null) ? group.getCode() : null;
        return new PlateIdentifier(plateCode, spaceCodeOrNull);
    }

    private static SamplePE getSample(DataPE dataset)
    {
        SamplePE sample = dataset.tryGetSample();
        assert sample != null : "dataset not connected to a sample: " + dataset;
        return sample;
    }

    private static Geometry extractPlateGemoetry(DataPE dataSet)
    {
        SamplePE sample = getSample(dataSet);
        Set<SamplePropertyPE> properties = sample.getProperties();
        for (SamplePropertyPE property : properties)
        {
            PropertyTypePE propertyType = property.getEntityTypePropertyType().getPropertyType();
            if (propertyType.getCode().equals(ScreeningConstants.PLATE_GEOMETRY))
            {
                String code = property.getVocabularyTerm().getCode();
                int lastIndexOfUnderscore = code.lastIndexOf('_');
                int lastIndexOfX = code.lastIndexOf('X');
                if (lastIndexOfUnderscore < 0 || lastIndexOfX < 0)
                {
                    throw new UserFailureException("Invalid property "
                            + ScreeningConstants.PLATE_GEOMETRY + ": " + code);
                }
                try
                {
                    int width =
                            Integer.parseInt(code
                                    .substring(lastIndexOfUnderscore + 1, lastIndexOfX));
                    int height = Integer.parseInt(code.substring(lastIndexOfX + 1));
                    return new Geometry(width, height);
                } catch (NumberFormatException ex)
                {
                    throw new UserFailureException("Invalid property "
                            + ScreeningConstants.PLATE_GEOMETRY + ": " + code);
                }

            }
        }
        throw new UserFailureException("Sample " + sample.getIdentifier() + " has no property "
                + ScreeningConstants.PLATE_GEOMETRY);
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
        if (externalData == null)
        {
            throw UserFailureException.fromTemplate("Dataset %s does not exist", datasetCode);
        }
        return new DatasetIdentifier(datasetCode, externalData.getDataStore().getDownloadUrl());
    }
}
