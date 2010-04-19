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
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.Dataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IPlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.Plate;
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

    public List<Dataset> listFeatureVectorDatasets(List<? extends IPlateIdentifier> plates)
    {
        return loadDatasets(plates, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
    }

    public List<Dataset> listImageDatasets(List<? extends IPlateIdentifier> plates)
    {
        return loadDatasets(plates, ScreeningConstants.IMAGE_DATASET_TYPE);
    }

    // NOTE: this method is slow when a number of plates is big
    private List<Dataset> loadDatasets(List<? extends IPlateIdentifier> plates,
            String datasetTypeCode)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        List<Dataset> datasets = new ArrayList<Dataset>();
        for (IPlateIdentifier plate : plates)
        {
            List<Dataset> plateDatasets = loadDatasets(plate, datasetTypeCode, sampleBO);
            datasets.addAll(plateDatasets);
        }
        return datasets;
    }

    private List<Dataset> loadDatasets(IPlateIdentifier plate, String datasetTypeCode,
            ISampleBO sampleBO)
    {
        sampleBO.loadBySampleIdentifier(createSampleIdentifier(plate));
        SamplePE sample = sampleBO.getSample();
        List<ExternalDataPE> datasets = daoFactory.getExternalDataDAO().listExternalData(sample);
        datasets = ScreeningUtils.filterDatasetsByType(datasets, datasetTypeCode);
        return asDatasets(datasets, plate);
    }

    private static List<Dataset> asDatasets(List<ExternalDataPE> datasets, IPlateIdentifier plate)
    {
        List<Dataset> result = new ArrayList<Dataset>();
        for (ExternalDataPE externalData : datasets)
        {
            result.add(asDataset(externalData, plate));
        }
        return result;
    }

    private static Dataset asDataset(ExternalDataPE externalData, IPlateIdentifier plate)
    {
        return new Dataset(externalData.getCode(), externalData.getDataStore().getCode(), plate);
    }

    private static SampleIdentifier createSampleIdentifier(IPlateIdentifier plate)
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
}
