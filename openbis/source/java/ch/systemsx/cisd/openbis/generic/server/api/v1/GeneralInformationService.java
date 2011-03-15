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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
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

    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        SessionContextDTO session = tryToAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    public boolean isSessionActive(String sessionToken)
    {
        return tryGetSession(sessionToken) != null;
    }

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
        return 5;
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

    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        checkSession(sessionToken);

        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convertToDetailedSearchCriteria(
                        SearchableEntityKind.SAMPLE, searchCriteria);
        List<DetailedSearchSubCriteria> detailedSearchSubCriterias =
                new ArrayList<DetailedSearchSubCriteria>();
        for (SearchSubCriteria subCriteria : searchCriteria.getSubCriterias())
        {
            DetailedSearchSubCriteria detailedSearchSubCriteria =
                    SearchCriteriaToDetailedSearchCriteriaTranslator
                            .convertToDetailedSearchSubCriteria(subCriteria);
            detailedSearchSubCriterias.add(detailedSearchSubCriteria);
        }
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> privateSamples =
                commonServer.searchForSamples(sessionToken, detailedSearchCriteria,
                        detailedSearchSubCriterias);
        ArrayList<Sample> samples = new ArrayList<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample : privateSamples)
        {
            samples.add(Translator.translate(privateSample));
        }

        return samples;
    }

    public List<Sample> listSamplesForExperiment(String sessionToken,
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
        ArrayList<Sample> samples = new ArrayList<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample : privateSamples)
        {
            samples.add(Translator.translate(privateSample));
        }

        return samples;
    }

    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples)
    {
        checkSession(sessionToken);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes =
                commonServer.listSampleTypes(sessionToken);
        SampleToDataSetRelatedEntitiesTranslator translator =
                new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<ExternalData> externalData = commonServer.listRelatedDataSets(sessionToken, dsre);
        ArrayList<DataSet> dataSets = new ArrayList<DataSet>(externalData.size());
        for (ExternalData externalDatum : externalData)
        {
            dataSets.add(Translator.translate(externalDatum));
        }
        return dataSets;
    }

    public List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentTypeString)
    {
        checkSession(sessionToken);

        // Convert the string to an experiment type
        List<ExperimentType> experimentTypes = commonServer.listExperimentTypes(sessionToken);
        ExperimentType experimentType = null;
        for (ExperimentType anExperimentType : experimentTypes)
        {
            if (anExperimentType.getCode().equals(experimentTypeString))
            {
                experimentType = anExperimentType;
            }
        }
        if (null == experimentType)
        {
            throw new UserFailureException("Unknown experiment type : " + experimentTypeString);
        }

        // Retrieve the matches for each project
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();

        for (Project project : projects)
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifier(project.getSpaceCode(), project.getCode());

            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> basicExperiments =
                    commonServer.listExperiments(sessionToken, experimentType, projectIdentifier);
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment basicExperiment : basicExperiments)
            {
                experiments.add(Translator.translate(basicExperiment));
            }
        }
        return experiments;
    }

    public List<DataSet> listDataSetsForSample(String sessionToken, Sample sample,
            boolean areOnlyDirectlyConnectedIncluded)
    {
        checkSession(sessionToken);
        List<ExternalData> externalData =
                commonServer.listSampleExternalData(sessionToken, new TechId(sample.getId()),
                        areOnlyDirectlyConnectedIncluded);
        ArrayList<DataSet> dataSets = new ArrayList<DataSet>(externalData.size());
        for (ExternalData externalDatum : externalData)
        {
            dataSets.add(Translator.translate(externalDatum));
        }
        return dataSets;
    }

    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        checkSession(sessionToken);
        IDataStoreDAO dataStoreDAO = getDAOFactory().getDataStoreDAO();
        List<DataStorePE> dataStores = dataStoreDAO.listDataStores();
        if (dataStores.size() != 1)
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            "Expected exactly one Data Store Server to be registered in openBIS but found %s.",
                            dataStores.size());
        }
        return dataStores.get(0).getDownloadUrl();
    }

    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode)
    {
        checkSession(sessionToken);

        IExternalDataDAO dataDAO = getDAOFactory().getExternalDataDAO();
        DataPE data = dataDAO.tryToFindDataSetByCode(dataSetCode);
        if (data == null)
        {
            return null;
        }

        return data.getDataStore().getDownloadUrl();
    }

    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType> privateDataSetTypes =
                commonServer.listDataSetTypes(sessionToken);

        ArrayList<DataSetType> dataSetTypes = new ArrayList<DataSetType>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType privateDataSetType : privateDataSetTypes)
        {
            dataSetTypes.add(Translator.translate(privateDataSetType));
        }
        return dataSetTypes;
    }
}
