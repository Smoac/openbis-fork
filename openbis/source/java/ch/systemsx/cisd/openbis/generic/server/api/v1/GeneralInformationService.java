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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister.DataSetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister.IDataSetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.SampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ProjectPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.DataSetByExperimentIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
public class GeneralInformationService extends AbstractServer<IGeneralInformationService> implements
        IGeneralInformationService
{
    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    // Default constructor needed by Spring
    public GeneralInformationService()
    {
    }

    GeneralInformationService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager, ICommonServer commonServer)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.commonServer = commonServer;
    }

    public IGeneralInformationService createLogger(IInvocationLoggerContext context)
    {
        return new GeneralInformationServiceLogger(sessionManager, context);
    }

    @Transactional
    // this is not a readOnly transaction - it can create new users
    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        SessionContextDTO session = tryToAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionToken)
    {
        return tryGetSession(sessionToken) != null;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken)
    {
        checkSession(sessionToken);

        Map<String, Set<Role>> namedRoleSets = new LinkedHashMap<String, Set<Role>>();
        RoleWithHierarchy[] values = RoleWithHierarchy.values();
        for (RoleWithHierarchy roleSet : values)
        {
            Set<RoleWithHierarchy> roles = roleSet.getRoles();
            Set<Role> translatedRoles = new HashSet<Role>();
            for (RoleWithHierarchy role : roles)
            {
                translatedRoles.add(Translator.translate(role));
            }
            namedRoleSets.put(roleSet.name(), translatedRoles);
        }
        return namedRoleSets;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SimpleSpaceValidator.class)
    @Capability("LIST_SPACES")
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull)
    {
        checkSession(sessionToken);

        Map<String, List<RoleAssignmentPE>> roleAssignmentsPerSpace = getRoleAssignmentsPerSpace();
        List<RoleAssignmentPE> instanceRoleAssignments = roleAssignmentsPerSpace.get(null);
        List<SpacePE> spaces = listSpaces(databaseInstanceCodeOrNull);
        List<SpaceWithProjectsAndRoleAssignments> result =
                new ArrayList<SpaceWithProjectsAndRoleAssignments>();
        for (SpacePE space : spaces)
        {
            SpaceWithProjectsAndRoleAssignments fullSpace =
                    new SpaceWithProjectsAndRoleAssignments(space.getCode());
            addProjectsTo(fullSpace, space);
            addRoles(fullSpace, instanceRoleAssignments);
            List<RoleAssignmentPE> list = roleAssignmentsPerSpace.get(space.getCode());
            if (list != null)
            {
                addRoles(fullSpace, list);
            }
            result.add(fullSpace);
        }
        return result;
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 16;
    }

    private Map<String, List<RoleAssignmentPE>> getRoleAssignmentsPerSpace()
    {
        List<RoleAssignmentPE> roleAssignments =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        Map<String, List<RoleAssignmentPE>> roleAssignmentsPerSpace =
                new HashMap<String, List<RoleAssignmentPE>>();
        for (RoleAssignmentPE roleAssignment : roleAssignments)
        {
            SpacePE space = roleAssignment.getSpace();
            String spaceCode = space == null ? null : space.getCode();
            List<RoleAssignmentPE> list = roleAssignmentsPerSpace.get(spaceCode);
            if (list == null)
            {
                list = new ArrayList<RoleAssignmentPE>();
                roleAssignmentsPerSpace.put(spaceCode, list);
            }
            list.add(roleAssignment);
        }
        return roleAssignmentsPerSpace;
    }

    private List<SpacePE> listSpaces(String databaseInstanceCodeOrNull)
    {
        IDAOFactory daoFactory = getDAOFactory();
        DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        if (databaseInstanceCodeOrNull != null)
        {
            IDatabaseInstanceDAO databaseInstanceDAO = daoFactory.getDatabaseInstanceDAO();
            databaseInstance =
                    databaseInstanceDAO.tryFindDatabaseInstanceByCode(databaseInstanceCodeOrNull);
        }
        return daoFactory.getSpaceDAO().listSpaces(databaseInstance);
    }

    private void addProjectsTo(SpaceWithProjectsAndRoleAssignments fullSpace, SpacePE space)
    {
        List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects(space);
        for (ProjectPE project : projects)
        {
            fullSpace.add(new Project(fullSpace.getCode(), project.getCode()));
        }
    }

    private void addRoles(SpaceWithProjectsAndRoleAssignments fullSpace, List<RoleAssignmentPE> list)
    {
        for (RoleAssignmentPE roleAssignment : list)
        {
            Role role =
                    Translator.translate(roleAssignment.getRole(),
                            roleAssignment.getSpace() != null);
            Set<PersonPE> persons;
            AuthorizationGroupPE authorizationGroup = roleAssignment.getAuthorizationGroup();
            if (authorizationGroup != null)
            {
                persons = authorizationGroup.getPersons();
            } else
            {
                persons = Collections.singleton(roleAssignment.getPerson());
            }
            for (PersonPE person : persons)
            {
                fullSpace.add(person.getUserId(), role);
            }
        }
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    @Capability("SEARCH_FOR_SAMPLES")
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        return searchForSamples(sessionToken, searchCriteria,
                EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PROPERTIES));
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    @Capability("SEARCH_FOR_SAMPLES")
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOption)
    {
        checkSession(sessionToken);

        EnumSet<SampleFetchOption> sampleFetchOptions =
                (fetchOption != null) ? fetchOption : EnumSet.noneOf(SampleFetchOption.class);
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(
                        SearchableEntityKind.SAMPLE, searchCriteria);
        Collection<Long> sampleIDs =
                new SampleSearchManager(getDAOFactory().getHibernateSearchDAO(), null)
                        .searchForSampleIDs(detailedSearchCriteria);
        return createSampleLister().getSamples(sampleIDs, sampleFetchOptions);
    }

    protected ISampleLister createSampleLister()
    {
        return new SampleLister(getDAOFactory());
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    @Capability("LIST_SAMPLES")
    public List<Sample> listSamplesForExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class)
            String experimentIdentifierString)
    {
        checkSession(sessionToken);
        ExperimentIdentifier experimentId =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExperiment =
                commonServer.getExperimentInfo(sessionToken, experimentId);

        ListSampleCriteria listSampleCriteria =
                ListSampleCriteria.createForExperiment(new TechId(privateExperiment.getId()));
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> privateSamples =
                commonServer.listSamples(sessionToken, listSampleCriteria);
        return Translator.translateSamples(privateSamples);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("LIST_DATA_SETS")
    public List<DataSet> listDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = SamplePredicate.class)
            List<Sample> samples)
    {
        return listDataSets(sessionToken, samples, EnumSet.noneOf(Connections.class));
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    @Capability("LIST_EXPERIMENTS")
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class)
            List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, false, false);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    @Capability("LIST_EXPERIMENTS")
    public List<Experiment> listExperimentsHavingDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class)
            List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, false, true);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    @Capability("LIST_EXPERIMENTS")
    public List<Experiment> listExperimentsHavingSamples(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class)
            List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, true, false);
    }

    private List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentTypeString, boolean onlyHavingSamples, boolean onlyHavingDataSets)
    {
        checkSession(sessionToken);

        // Convert the string to an experiment type
        ExperimentType experimentType = null;
        if (experimentTypeString == null || EntityType.ALL_TYPES_CODE.equals(experimentTypeString))
        {
            experimentType = new ExperimentType();
            experimentType.setCode(EntityType.ALL_TYPES_CODE);
        } else
        {
            experimentType = tryFindExperimentType(sessionToken, experimentTypeString);
            if (null == experimentType)
            {
                throw new UserFailureException("Unknown experiment type : " + experimentTypeString);
            }
        }

        // Retrieve the matches for each project
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();

        for (Project project : projects)
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifier(project.getSpaceCode(), project.getCode());

            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> basicExperiments;

            if (onlyHavingSamples)
            {
                basicExperiments =
                        commonServer.listExperimentsHavingSamples(sessionToken, experimentType,
                                projectIdentifier);
            } else if (onlyHavingDataSets)
            {
                basicExperiments =
                        commonServer.listExperimentsHavingDataSets(sessionToken, experimentType,
                                projectIdentifier);
            } else
            {
                basicExperiments =
                        commonServer.listExperiments(sessionToken, experimentType,
                                projectIdentifier);
            }
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment basicExperiment : basicExperiments)
            {
                experiments.add(Translator.translate(basicExperiment));
            }
        }
        return experiments;
    }

    private ExperimentType tryFindExperimentType(String sessionToken, String experimentTypeString)
    {
        List<ExperimentType> experimentTypes = commonServer.listExperimentTypes(sessionToken);
        for (ExperimentType anExperimentType : experimentTypes)
        {
            if (anExperimentType.getCode().equals(experimentTypeString))
            {
                return anExperimentType;
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("LIST_DATA_SETS")
    public List<DataSet> listDataSetsForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SamplePredicate.class)
            Sample sample, boolean areOnlyDirectlyConnectedIncluded)
    {
        checkSession(sessionToken);
        List<ExternalData> externalData =
                commonServer.listSampleExternalData(sessionToken, new TechId(sample.getId()),
                        areOnlyDirectlyConnectedIncluded);
        return Translator.translate(externalData, EnumSet.noneOf(Connections.class));
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        return commonServer.getDefaultPutDataStoreBaseURL(sessionToken);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode)
    {
        checkSession(sessionToken);

        IDataDAO dataDAO = getDAOFactory().getDataDAO();
        DataPE data = dataDAO.tryToFindDataSetByCode(dataSetCode);
        if (data == null)
        {
            return null;
        }

        return data.getDataStore().getDownloadUrl();
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType> privateDataSetTypes =
                commonServer.listDataSetTypes(sessionToken);

        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                getVocabularyTermsMap(sessionToken);

        ArrayList<DataSetType> dataSetTypes = new ArrayList<DataSetType>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType privateDataSetType : privateDataSetTypes)
        {
            dataSetTypes.add(Translator.translate(privateDataSetType, vocabTerms));
        }
        return dataSetTypes;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> getVocabularyTermsMap(
            String sessionToken)
    {
        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                new HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>>();
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary> privateVocabularies =
                commonServer.listVocabularies(sessionToken, true, false);
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary privateVocabulary : privateVocabularies)
        {
            vocabTerms.put(privateVocabulary,
                    Translator.translatePropertyTypeTerms(privateVocabulary.getTerms()));
        }
        return vocabTerms;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Vocabulary> listVocabularies(String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary> privateVocabularies =
                commonServer.listVocabularies(sessionToken, true, false);
        List<Vocabulary> result = new ArrayList<Vocabulary>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary privateVocabulary : privateVocabularies)
        {
            result.add(Translator.translate(privateVocabulary));
        }
        return result;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("LIST_DATA_SETS")
    public List<DataSet> listDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = SamplePredicate.class)
            List<Sample> samples, EnumSet<Connections> connections)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes =
                commonServer.listSampleTypes(sessionToken);
        SampleToDataSetRelatedEntitiesTranslator translator =
                new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<ExternalData> dataSets = commonServer.listRelatedDataSets(sessionToken, dsre, true);
        return Translator.translate(dataSets, connectionsToGet);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("LIST_DATA_SETS")
    public List<DataSet> listDataSetsForExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentPredicate.class)
            List<Experiment> experiments, EnumSet<Connections> connections)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType> experimentTypes =
                commonServer.listExperimentTypes(sessionToken);
        ExperimentToDataSetRelatedEntitiesTranslator translator =
                new ExperimentToDataSetRelatedEntitiesTranslator(experimentTypes, experiments);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<ExternalData> dataSets = commonServer.listRelatedDataSets(sessionToken, dsre, true);
        return Translator.translate(dataSets, connectionsToGet);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("GET_DATA_SET_META_DATA")
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes)
    {
        Session session = getSession(sessionToken);

        IDataDAO dataDAO = getDAOFactory().getDataDAO();
        List<DataSet> result = new ArrayList<DataSet>();
        EnumSet<Connections> connections = EnumSet.of(Connections.PARENTS, Connections.CHILDREN);
        for (String dataSetCode : dataSetCodes)
        {
            DataPE dataPE = dataDAO.tryToFindDataSetByCode(dataSetCode);
            if (dataPE == null)
            {
                throw new UserFailureException("Unknown data set " + dataSetCode);
            }
            HibernateUtils.initialize(dataPE.getChildren());
            HibernateUtils.initialize(dataPE.getProperties());
            ExternalData ds = DataSetTranslator.translate(dataPE, session.getBaseIndexURL());
            result.add(Translator.translate(ds, connections));
        }
        return result;
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("GET_DATA_SET_META_DATA")
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes,
            EnumSet<DataSetFetchOption> fetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("SessionToken was null");
        }
        if (dataSetCodes == null)
        {
            throw new IllegalArgumentException("DataSetCodes were null");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("FetchOptions were null");
        }

        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        for (DataSetFetchOption option : fetchOptions)
        {
            dataSetFetchOptions.addOption(option);
        }

        if (dataSetFetchOptions.isSubsetOf(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS,
                DataSetFetchOption.CHILDREN))
        {
            checkSession(sessionToken);
            IDataSetLister lister = new DataSetLister(getDAOFactory());
            return lister.getDataSetMetaData(dataSetCodes, dataSetFetchOptions);
        } else
        {
            List<DataSet> dataSetList = getDataSetMetaData(sessionToken, dataSetCodes);
            if (dataSetList != null)
            {
                for (DataSet dataSet : dataSetList)
                {
                    if (dataSet != null)
                    {
                        dataSet.setFetchOptions(new DataSetFetchOptions(DataSetFetchOption.values()));
                    }
                }
            }
            return dataSetList;
        }
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    @Capability("SEARCH_FOR_DATA_SETS")
    public List<DataSet> searchForDataSets(String sessionToken, SearchCriteria searchCriteria)
    {
        checkSession(sessionToken);

        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(
                        SearchableEntityKind.DATA_SET, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData> privateDataSets =
                commonServer.searchForDataSets(sessionToken, detailedSearchCriteria);

        // The underlying search, as currently implemented, does not return any of the connections
        return Translator.translate(privateDataSets, EnumSet.noneOf(Connections.class));
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    @Capability("LIST_EXPERIMENTS")
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class)
            List<String> experimentIdentifiers)
    {
        checkSession(sessionToken);

        List<ExperimentIdentifier> parsedIdentifiers =
                ExperimentIdentifierFactory.parse(experimentIdentifiers);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> experiments =
                commonServer.listExperiments(sessionToken, parsedIdentifiers);

        return Translator.translateExperiments(experiments);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ProjectByIdentiferValidator.class)
    @Capability("LIST_PROJECTS")
    public List<Project> listProjects(String sessionToken)
    {
        checkSession(sessionToken);

        return Translator.translateProjects(commonServer.listProjects(sessionToken));
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @Capability("GET_MATERIALS_BY_CODES")
    public List<Material> getMaterialByCodes(String sessionToken,
            List<MaterialIdentifier> materialIdentifier)
    {
        // convert api material indetifier into dto material identifier
        Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier> materialCodes =
                CollectionUtils
                        .collect(
                                materialIdentifier,
                                new Transformer<MaterialIdentifier, ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier>()
                                    {
                                        public ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier transform(
                                                MaterialIdentifier arg0)
                                        {
                                            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier(
                                                    arg0.getMaterialCode(), arg0
                                                            .getMaterialTypeIdentifier()
                                                            .getMaterialTypeCode());
                                        }
                                    });

        ListMaterialCriteria criteria =
                ListMaterialCriteria.createFromMaterialIdentifiers(materialCodes);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materials =
                commonServer.listMaterials(sessionToken, criteria, true);
        return Translator.translateMaterials(materials);
    }

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @Capability("SEARCH_FOR_MATERIALS")
    public List<Material> searchForMaterials(String sessionToken, SearchCriteria searchCriteria)
    {
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(
                        SearchableEntityKind.MATERIAL, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materials =
                commonServer.searchForMaterials(sessionToken, detailedSearchCriteria);
        return Translator.translateMaterials(materials);
    }
}
