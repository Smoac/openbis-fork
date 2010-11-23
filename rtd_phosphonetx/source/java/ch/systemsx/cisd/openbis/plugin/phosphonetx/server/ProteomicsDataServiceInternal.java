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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExperimentValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.ExperimentLoader;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.ISampleLoader;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator.ParentSampleValidator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator.RawDataSampleValidator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
public class ProteomicsDataServiceInternal extends AbstractServer<IProteomicsDataServiceInternal> implements
        IProteomicsDataServiceInternal
{
    @Private
    static final String SPACE_CODE = "MS_DATA";

    @Private
    static final String RAW_DATA_SAMPLE_TYPE = "MS_INJECTION";
    
    private static final IValidator<Sample> PARENT_SAMPLE_VALIDATOR = new ParentSampleValidator();

    private static final IValidator<MsInjectionSample> RAW_DATA_SAMPLE_VALIDATOR =
            new RawDataSampleValidator();
    
    private static final IValidator<Experiment> EXPERIMENT_VALIDATOR = new ExperimentValidator();

    private ICommonBusinessObjectFactory commonBoFactory;

    private ISessionManager<Session> sessionManagerFromConstructor;

    private ExperimentLoader experimentLoader;

    private IBusinessObjectFactory boFactory;

    public ProteomicsDataServiceInternal()
    {
    }

    public ProteomicsDataServiceInternal(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory businessObjectFactory, IBusinessObjectFactory boFactory)
    {
        super(sessionManager, daoFactory);
        sessionManagerFromConstructor = sessionManager;
        this.commonBoFactory = businessObjectFactory;
        this.boFactory = boFactory;
        experimentLoader = new ExperimentLoader(getDAOFactory());
    }

    public void replaceAutoWiredSesseionManagerByConstructorSessionManager()
    {
        sessionManager = sessionManagerFromConstructor;
    }

    public IProteomicsDataServiceInternal createLogger(IInvocationLoggerContext context)
    {
        return new ProteomicsDataServiceInternalLogger(getSessionManager(), context);
    }

    public List<MsInjectionSample> listRawDataSamples(String sessionToken)
    {
        return loadAllRawDataSamples(getSession(sessionToken));
    }

    public void processRawData(String sessionToken, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        Session session = getSession(sessionToken);
        PersonPE person = session.tryGetPerson();

        List<MsInjectionSample> samples = loadAllRawDataSamples(session);
        Set<Long> sampleIDs = asSet(rawDataSampleIDs);
        List<String> dataSetCodes = new ArrayList<String>();
        Map<String, String> parameterBindings = new HashMap<String, String>();
        for (MsInjectionSample sample : samples)
        {
            if (RAW_DATA_SAMPLE_VALIDATOR.isValid(person, sample)
                    && sampleIDs.contains(sample.getSample().getId()))
            {
                Map<String, ExternalData> latestDataSets = sample.getLatestDataSets();
                ExternalData latestDataSet = latestDataSets.get(dataSetType);
                if (latestDataSet != null)
                {
                    String code = latestDataSet.getCode();
                    dataSetCodes.add(code);
                    parameterBindings.put(code, sample.getSample().getCode());
                }
            }
        }

        processDataSets(session, dataSetProcessingKey, dataSetCodes, parameterBindings);
    }

    public void processDataSets(String sessionToken, String dataSetProcessingKey,
            List<String> dataSetCodes)
    {
        Session session = getSession(sessionToken);
        processDataSets(session, dataSetProcessingKey, dataSetCodes, new HashMap<String, String>());
    }

    public List<Experiment> listSearchExperiments(String sessionToken, String experimentTypeCode)
    {
        checkSession(sessionToken);
        
        IDAOFactory daoFactory = getDAOFactory();
        IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT);
        ExperimentTypePE type =
                (ExperimentTypePE) entityTypeDAO.tryToFindEntityTypeByCode(experimentTypeCode);
        List<ExperimentPE> experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(type, null);
        return ExperimentTranslator.translate(experiments, "",
                ExperimentTranslator.LoadableFields.PROPERTIES);
    }

    public void processProteinResultDataSets(String sessionToken, String dataSetProcessingKey,
            String experimentType, long[] searchExperimentIDs)
    {
        Session session = getSession(sessionToken);
        PersonPE person = session.tryGetPerson();
        
        List<String> dataSetCodes = new ArrayList<String>();
        IExperimentDAO experimentDAO = getDAOFactory().getExperimentDAO();
        IExternalDataDAO dataSetDAO = getDAOFactory().getExternalDataDAO();
        for (long experimentID : searchExperimentIDs)
        {
            ExperimentPE experiment = experimentDAO.tryGetByTechId(new TechId(experimentID));
            String actualExperimentTypeCode = experiment.getExperimentType().getCode();
            if (actualExperimentTypeCode.equals(experimentType) == false)
            {
                throw new UserFailureException("Experiment with technical id " + experimentID
                        + " [" + experiment.getIdentifier() + "] is not of type " + experimentType
                        + " but of type " + actualExperimentTypeCode + ".");
            }
            Experiment translatedExperiment = ExperimentTranslator.translate(experiment, "");
            if (EXPERIMENT_VALIDATOR.isValid(person, translatedExperiment))
            {
                List<ExternalDataPE> dataSets = dataSetDAO.listExternalData(experiment);
                for (ExternalDataPE dataSet : dataSets)
                {
                    dataSetCodes.add(dataSet.getCode());
                }
            }
        }

        processDataSets(session, dataSetProcessingKey, dataSetCodes, new HashMap<String, String>());
    }

    private List<MsInjectionSample> loadAllRawDataSamples(Session session)
    {
        List<Sample> samples = loadAccessableSamples(session);
        List<Sample> parentSamples = new ArrayList<Sample>();
        for (Sample sample : samples)
        {
            parentSamples.add(sample.getGeneratedFrom());
        }
        experimentLoader.enrichWithExperiments(parentSamples);
        Map<Sample, List<ExternalData>> dataSetsBySamples =
                commonBoFactory.createDatasetLister(session).listAllDataSetsFor(samples);
        List<MsInjectionSample> result = new ArrayList<MsInjectionSample>();
        for (Entry<Sample, List<ExternalData>> entry : dataSetsBySamples.entrySet())
        {
            result.add(new MsInjectionSample(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    protected List<Sample> loadAccessableSamples(Session session)
    {
        ISampleLoader sampleLoader = boFactory.createSampleLoader(session);
        List<Sample> samples =
                sampleLoader.listSamplesWithParentsByTypeAndSpace(RAW_DATA_SAMPLE_TYPE, SPACE_CODE);
        PersonPE person = session.tryGetPerson();
        List<Sample> validSamples = new ArrayList<Sample>();
        for (Sample sample : samples)
        {
            if (PARENT_SAMPLE_VALIDATOR.isValid(person, sample))
            {
                validSamples.add(sample);
            }
        }
        return validSamples;
    }

    private void processDataSets(Session session, String dataSetProcessingKey,
            List<String> dataSetCodes, Map<String, String> parameterBindings)
    {
        String dataStoreServerCode = findDataStoreServer(dataSetProcessingKey);
        IExternalDataTable externalDataTable =
                commonBoFactory.createExternalDataTable(session);
        externalDataTable.processDatasets(dataSetProcessingKey, dataStoreServerCode, dataSetCodes,
                parameterBindings);
    }

    private String findDataStoreServer(String dataSetProcessingKey)
    {
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            Set<DataStoreServicePE> services = dataStore.getServices();
            for (DataStoreServicePE dataStoreService : services)
            {
                if (DataStoreServiceKind.PROCESSING.equals(dataStoreService.getKind())
                        && dataSetProcessingKey.equals(dataStoreService.getKey()))
                {
                    return dataStore.getCode();
                }
            }
        }
        throw new EnvironmentFailureException("No data store processing service with key '"
                + dataSetProcessingKey + "' found.");
    }
    
    private Set<Long> asSet(long[] ids)
    {
        Set<Long> result = new HashSet<Long>();
        for (long id : ids)
        {
            result.add(id);
        }
        return result;
    }
}

