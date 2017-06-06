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

package ch.systemsx.cisd.openbis.plugin.proteomics.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.authorization.validator.ParentSampleValidator;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.authorization.validator.RawDataSampleValidator;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.ExperimentLoader;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.ISampleLoader;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.CommonConstants;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
public class ProteomicsDataServiceInternal extends AbstractServer<IProteomicsDataServiceInternal>
        implements IProteomicsDataServiceInternal
{
    private static final ParentSampleValidator PARENT_SAMPLE_VALIDATOR = new ParentSampleValidator();

    private static final IValidator<MsInjectionSample> RAW_DATA_SAMPLE_VALIDATOR =
            new RawDataSampleValidator();

    private ICommonBusinessObjectFactory commonBoFactory;

    private IOpenBisSessionManager sessionManagerFromConstructor;

    private ExperimentLoader experimentLoader;

    private IBusinessObjectFactory boFactory;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public ProteomicsDataServiceInternal()
    {
    }

    public ProteomicsDataServiceInternal(IOpenBisSessionManager sessionManager,
            IDAOFactory daoFactory, ICommonBusinessObjectFactory businessObjectFactory,
            IBusinessObjectFactory boFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this(sessionManager, daoFactory, null, businessObjectFactory, boFactory,
                managedPropertyEvaluatorFactory);
    }

    ProteomicsDataServiceInternal(IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            ICommonBusinessObjectFactory businessObjectFactory, IBusinessObjectFactory boFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        sessionManagerFromConstructor = sessionManager;
        this.commonBoFactory = businessObjectFactory;
        this.boFactory = boFactory;
        experimentLoader = new ExperimentLoader(getDAOFactory(), managedPropertyEvaluatorFactory);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    public void replaceAutoWiredSesseionManagerByConstructorSessionManager()
    {
        sessionManager = sessionManagerFromConstructor;
    }

    @Override
    public IProteomicsDataServiceInternal createLogger(IInvocationLoggerContext context)
    {
        return new ProteomicsDataServiceInternalLogger(getSessionManager(), context);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @ReturnValueFilter(validatorClass = RawDataSampleValidator.class)
    public List<MsInjectionSample> listRawDataSamples(String sessionToken)
    {
        return loadAllRawDataSamples(getSession(sessionToken), true);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public List<MsInjectionSample> listAllRawDataSamples(String sessionToken)
    {
        return loadAllRawDataSamples(getSession(sessionToken), false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void processRawData(String sessionToken, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        Session session = getSession(sessionToken);
        PersonPE person = session.tryGetPerson();

        List<MsInjectionSample> samples = loadAllRawDataSamples(session, true);
        Set<Long> sampleIDs = asSet(rawDataSampleIDs);
        List<String> dataSetCodes = new ArrayList<String>();
        Map<String, String> parameterBindings = new HashMap<String, String>();
        for (MsInjectionSample sample : samples)
        {
            if (RAW_DATA_SAMPLE_VALIDATOR.isValid(person, sample)
                    && sampleIDs.contains(sample.getSample().getId()))
            {
                Map<String, AbstractExternalData> latestDataSets = sample.getLatestDataSets();
                AbstractExternalData latestDataSet = latestDataSets.get(dataSetType);
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

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void processDataSets(String sessionToken, String dataSetProcessingKey,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes)
    {
        Session session = getSession(sessionToken);
        processDataSets(session, dataSetProcessingKey, dataSetCodes, new HashMap<String, String>());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @ReturnValueFilter(validatorClass = ExperimentValidator.class)
    public List<Experiment> listExperiments(String sessionToken, String experimentTypeCode)
    {
        Session session = getSession(sessionToken);

        IDAOFactory daoFactory = getDAOFactory();
        IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT);
        ExperimentTypePE type =
                (ExperimentTypePE) entityTypeDAO.tryToFindEntityTypeByCode(experimentTypeCode);
        List<ExperimentPE> experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(type, null, null);
        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        session.tryGetPerson(), experiments, EntityKind.EXPERIMENT);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);
        return ExperimentTranslator.translate(experiments, "", assignments,
                managedPropertyEvaluatorFactory);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public List<AbstractExternalData> listDataSetsByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentID)
    {
        final Session session = getSession(sessionToken);

        IDataSetTable dataSetTable = commonBoFactory.createDataSetTable(session);
        dataSetTable.loadByExperimentTechId(experimentID);
        List<DataPE> dataSetPEs = dataSetTable.getDataSets();
        Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        session.tryGetPerson(), dataSetPEs, EntityKind.DATA_SET);
        return DataSetTranslator.translate(dataSetPEs, "", "",
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs),
                managedPropertyEvaluatorFactory);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void processProteinResultDataSets(String sessionToken, String dataSetProcessingKey,
            String experimentType, long[] searchExperimentIDs)
    {
        Session session = getSession(sessionToken);
        PersonPE person = session.tryGetPerson();

        List<String> dataSetCodes = new ArrayList<String>();
        IExperimentDAO experimentDAO = getDAOFactory().getExperimentDAO();
        IDataDAO dataSetDAO = getDAOFactory().getDataDAO();
        ExperimentValidator validator = new ExperimentValidator();

        validator.init(new AuthorizationDataProvider(getDAOFactory()));

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
            Experiment translatedExperiment =
                    ExperimentTranslator.translate(experiment, "", null,
                            managedPropertyEvaluatorFactory);
            if (validator.isValid(person, translatedExperiment))
            {
                List<DataPE> dataSets = dataSetDAO.listDataSets(experiment);
                dataSetCodes.addAll(Code.extractCodes(dataSets));
            }
        }

        processDataSets(session, dataSetProcessingKey, dataSetCodes, new HashMap<String, String>());
    }

    private List<MsInjectionSample> loadAllRawDataSamples(Session session, boolean parentHasToBeValid)
    {
        List<Sample> samples = loadAccessableSamples(session, parentHasToBeValid);
        List<Sample> parentSamples = new ArrayList<Sample>();
        for (Sample sample : samples)
        {
            Sample parent = sample.getGeneratedFrom();
            if (parent != null)
            {
                parentSamples.add(parent);
            }
        }
        experimentLoader.enrichWithExperiments(parentSamples);
        Map<Sample, List<AbstractExternalData>> dataSetsBySamples =
                commonBoFactory.createDatasetLister(session).listAllDataSetsFor(samples);
        List<MsInjectionSample> result = new ArrayList<MsInjectionSample>();
        for (Entry<Sample, List<AbstractExternalData>> entry : dataSetsBySamples.entrySet())
        {
            result.add(new MsInjectionSample(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    protected List<Sample> loadAccessableSamples(Session session, boolean parentHasToBeValid)
    {
        ISampleLoader sampleLoader = boFactory.createSampleLoader(session);
        List<Sample> samples =
                sampleLoader.listSamplesWithParentsByTypeAndSpace(
                        CommonConstants.MS_INJECTION_SAMPLE_TYPE_CODE,
                        CommonConstants.MS_DATA_SPACE);
        PersonPE person = session.tryGetPerson();
        List<Sample> validSamples = new ArrayList<Sample>();
        for (Sample sample : samples)
        {
            if (PARENT_SAMPLE_VALIDATOR.isValid(person, sample, parentHasToBeValid))
            {
                if (PARENT_SAMPLE_VALIDATOR.isValid(person, sample) == false)
                {
                    sample.setParents(Collections.<Sample> emptySet());
                }
                validSamples.add(sample);
            }
        }
        return validSamples;
    }

    private void processDataSets(Session session, String dataSetProcessingKey,
            List<String> dataSetCodes, Map<String, String> parameterBindings)
    {
        try
        {
            String dataStoreServerCode = findDataStoreServer(dataSetProcessingKey);
            IDataSetTable dataSetTable = commonBoFactory.createDataSetTable(session);
            dataSetTable.processDatasets(dataSetProcessingKey, dataStoreServerCode, dataSetCodes,
                    parameterBindings);
        } catch (EnvironmentFailureException ex)
        {
            throw new EnvironmentFailureException("Processing data sets " +
                    CollectionUtils.abbreviate(dataSetCodes, 20) + " with processing plugin '"
                    + dataSetProcessingKey + "' using bindings " + parameterBindings + " failed.", ex);
        }
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
