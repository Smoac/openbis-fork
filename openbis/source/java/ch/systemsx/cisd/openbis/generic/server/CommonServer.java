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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.DataStoreServerSessionManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonServer extends AbstractServer<ICommonServer> implements ICommonServer
{
    private final IAuthenticationService authenticationService;

    private final ICommonBusinessObjectFactory businessObjectFactory;

    private final DataStoreServerSessionManager dssSessionManager;

    private final LastModificationState lastModificationState;

    public CommonServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager,
            DataStoreServerSessionManager dssSessionManager, final IDAOFactory daoFactory,
            final ICommonBusinessObjectFactory businessObjectFactory,
            LastModificationState lastModificationState)
    {
        super(sessionManager, daoFactory);
        this.authenticationService = authenticationService;
        this.dssSessionManager = dssSessionManager;
        this.businessObjectFactory = businessObjectFactory;
        this.lastModificationState = lastModificationState;
    }

    ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    // Call this when session object is not needed but you want just to
    // refresh/check the session.
    private void checkSession(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
    }

    private static UserFailureException createUserFailureException(final DataAccessException ex)
    {
        return new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
    }

    //
    // AbstractServerWithLogger
    //

    @Override
    protected final Class<ICommonServer> getProxyInterface()
    {
        return ICommonServer.class;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final ICommonServer createLogger(final boolean invocationSuccessful)
    {
        return new CommonServerLogger(getSessionManager(), invocationSuccessful);
    }

    //
    // IGenericServer
    //

    public final List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper.getDatabaseInstance(identifier, getDAOFactory());
        final List<GroupPE> groups = getDAOFactory().getGroupDAO().listGroups(databaseInstance);
        final Long homeGroupID = session.tryGetHomeGroupId();
        for (final GroupPE group : groups)
        {
            group.setHome(homeGroupID != null && homeGroupID.equals(group.getId()));
        }
        Collections.sort(groups);
        return groups;
    }

    public final void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        groupBO.define(groupCode, descriptionOrNull, groupLeaderOrNull);
        groupBO.save();
    }

    public final void registerPerson(final String sessionToken, final String userID)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userID);
        if (person != null)
        {
            throw UserFailureException.fromTemplate("Person '%s' already exists.", userID);
        }
        final String applicationToken = authenticationService.authenticateApplication();
        if (applicationToken == null)
        {
            throw new EnvironmentFailureException("Authentication service cannot be accessed.");
        }
        try
        {
            final Principal principal =
                    authenticationService.getPrincipal(applicationToken, userID);
            createPerson(principal, session.tryGetPerson());
        } catch (final IllegalArgumentException e)
        {
            throw new UserFailureException("Person '" + userID
                    + "' unknown by the authentication service.");
        }
    }

    public final List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        checkSession(sessionToken);
        return getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
    }

    public final void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        newRoleAssignment.setGroupIdentifier(groupIdentifier);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        newRoleAssignment.setDatabaseInstanceIdentifier(new DatabaseInstanceIdentifier(
                DatabaseInstanceIdentifier.HOME));
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindGroupRoleAssignment(roleCode,
                        groupIdentifier.getGroupCode(), person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given group role does not exist.");
        }
        final PersonPE personPE = session.tryGetPerson();
        if (roleAssignment.getPerson().equals(personPE)
                && roleAssignment.getRole().equals(RoleCode.ADMIN))
        {
            boolean isInstanceAdmin = false;
            for (final RoleAssignmentPE roleAssigment : personPE.getRoleAssignments())
            {
                if (roleAssigment.getDatabaseInstance() != null
                        && roleAssigment.getRole().equals(RoleCode.ADMIN))
                {
                    isInstanceAdmin = true;
                }
            }
            if (isInstanceAdmin == false)
            {
                throw new UserFailureException(
                        "For safety reason you cannot give away your own group admin power. "
                                + "Ask instance admin to do that for you.");
            }
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    public final void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IRoleAssignmentDAO roleAssignmentDAO = getDAOFactory().getRoleAssignmentDAO();
        final RoleAssignmentPE roleAssignment =
                roleAssignmentDAO.tryFindInstanceRoleAssignment(roleCode, person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given database instance role does not exist.");
        }
        if (roleAssignment.getPerson().equals(session.tryGetPerson())
                && roleAssignment.getRole().equals(RoleCode.ADMIN)
                && roleAssignment.getDatabaseInstance() != null)
        {
            throw new UserFailureException(
                    "For safety reason you cannot give away your own omnipotence. "
                            + "Ask another instance admin to do that for you.");
        }
        roleAssignmentDAO.deleteRoleAssignment(roleAssignment);
    }

    public final List<PersonPE> listPersons(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return persons;
    }

    public final List<ProjectPE> listProjects(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return projects;
    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<SampleTypePE> sampleTypes = getDAOFactory().getSampleTypeDAO().listSampleTypes();
        Collections.sort(sampleTypes);
        return sampleTypes;
    }

    public final List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        sampleTable.enrichWithValidProcedure();
        sampleTable.enrichWithProperties();
        final List<SamplePE> samples = sampleTable.getSamples();
        Collections.sort(samples);
        return samples;
    }

    public final List<ExternalDataPE> listExternalData(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadBySampleIdentifier(identifier);
        return getSortedExternalDataFrom(externalDataTable);
    }

    public List<ExternalDataPE> listExternalData(String sessionToken,
            ExperimentIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadByExperimentIdentifier(identifier);
        return getSortedExternalDataFrom(externalDataTable);
    }

    private List<ExternalDataPE> getSortedExternalDataFrom(
            final IExternalDataTable externalDataTable)
    {
        final List<ExternalDataPE> externalData = externalDataTable.getExternalData();
        Collections.sort(externalData);
        return externalData;
    }

    public final List<PropertyTypePE> listPropertyTypes(final String sessionToken)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IPropertyTypeTable propertyTypeTable =
                businessObjectFactory.createPropertyTypeTable(session);
        propertyTypeTable.load();
        propertyTypeTable.enrichWithRelations();
        final List<PropertyTypePE> propertyTypes = propertyTypeTable.getPropertyTypes();
        Collections.sort(propertyTypes);
        return propertyTypes;
    }

    public final List<SearchHit> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
    {
        checkSession(sessionToken);
        final List<SearchHit> list = new ArrayList<SearchHit>();
        try
        {
            for (final SearchableEntity searchableEntity : searchableEntities)
            {
                final List<SearchHit> entities =
                        getDAOFactory().getHibernateSearchDAO().searchEntitiesByTerm(
                                searchableEntity.getMatchingEntityClass(), queryText);
                list.addAll(entities);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
        return list;
    }

    public final List<ExperimentPE> listExperiments(final String sessionToken,
            final ExperimentTypePE experimentType, final ProjectIdentifier projectIdentifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        experimentTable.load(experimentType.getCode(), projectIdentifier);
        experimentTable.enrichWithProperties();
        final List<ExperimentPE> experiments = experimentTable.getExperiments();
        Collections.sort(experiments);
        return experiments;
    }

    public final List<ExperimentTypePE> listExperimentTypes(final String sessionToken)
    {
        return listEntityTypes(sessionToken, EntityKind.EXPERIMENT);
    }

    public List<MaterialTypePE> listMaterialTypes(String sessionToken)
    {
        return listEntityTypes(sessionToken, EntityKind.MATERIAL);
    }

    private <T extends EntityTypePE> List<T> listEntityTypes(String sessionToken,
            EntityKind entityKind)
    {
        checkSession(sessionToken);
        final List<T> types = getDAOFactory().getEntityTypeDAO(entityKind).listEntityTypes();
        Collections.sort(types);
        return types;
    }

    public final List<DataTypePE> listDataTypes(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<DataTypePE> dataTypes = getDAOFactory().getPropertyTypeDAO().listDataTypes();
        Collections.sort(dataTypes);
        return dataTypes;
    }

    public final List<VocabularyPE> listVocabularies(final String sessionToken,
            final boolean withTerms, boolean excludeInternal)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<VocabularyPE> vocabularies =
                getDAOFactory().getVocabularyDAO().listVocabularies(excludeInternal);
        if (withTerms)
        {
            for (final VocabularyPE vocabularyPE : vocabularies)
            {
                enrichWithTerms(vocabularyPE);
            }
        }
        Collections.sort(vocabularies);
        return vocabularies;
    }

    private void enrichWithTerms(final VocabularyPE vocabularyPE)
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
    }

    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSessionManager().getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, entityKind);
        etptBO.createAssignment(propertyTypeCode, entityTypeCode, isMandatory, defaultValue);
        return String.format("%s property type '%s' successfully assigned to %s type '%s'",
                isMandatory ? "Mandatory" : "Optional", propertyTypeCode, entityKind.getLabel(),
                entityTypeCode);

    }

    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        assert sessionToken != null : "Unspecified session token";
        assert propertyType != null : "Unspecified property type";

        final Session session = getSessionManager().getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.define(propertyType);
        propertyTypeBO.save();
    }

    public final void registerVocabulary(final String sessionToken, final Vocabulary vocabulary)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabulary != null : "Unspecified vocabulary";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
    }

    public void addVocabularyTerms(String sessionToken, String vocabularyCode,
            List<String> vocabularyTerms)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyCode != null : "Unspecified vocabulary code";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.load(vocabularyCode);
        vocabularyBO.addNewTerms(vocabularyTerms);
        vocabularyBO.save();
    }

    public void deleteVocabularyTerms(String sessionToken, String vocabularyCode,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyCode != null : "Unspecified vocabulary code";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.load(vocabularyCode);
        vocabularyBO.delete(termsToBeDeleted, termsToBeReplaced);
        vocabularyBO.save();
    }

    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        projectBO.define(projectIdentifier, description, leaderId);
        projectBO.save();

    }

    public List<ExternalDataPE> searchForDataSets(String sessionToken,
            DataSetSearchCriteria criteria)
    {
        checkSession(sessionToken);
        try
        {
            IHibernateSearchDAO searchDAO = getDAOFactory().getHibernateSearchDAO();
            return searchDAO.searchForDataSets(criteria);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<MaterialPE> listMaterials(String sessionToken, MaterialTypePE materialType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.load(materialType.getCode());
        final List<MaterialPE> materials = materialTable.getMaterials();
        Collections.sort(materials);
        return materials;
    }

    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason)
    {
        Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IExternalDataTable externalDataTable =
                    businessObjectFactory.createExternalDataTable(session);
            externalDataTable.loadByDataSetCodes(dataSetCodes);
            List<ExternalDataPE> dataSets = externalDataTable.getExternalData();
            Map<DataSetTypePE, List<ExternalDataPE>> groupedDataSets = new LinkedHashMap<DataSetTypePE, List<ExternalDataPE>>();
            for (ExternalDataPE dataSet : dataSets)
            {
                DataSetTypePE dataSetType = dataSet.getDataSetType();
                List<ExternalDataPE> list = groupedDataSets.get(dataSetType);
                if (list == null)
                {
                    list = new ArrayList<ExternalDataPE>();
                    groupedDataSets.put(dataSetType, list);
                }
                list.add(dataSet);
            }
            for (Map.Entry<DataSetTypePE, List<ExternalDataPE>> entry : groupedDataSets.entrySet())
            {
                DataSetTypePE dataSetType = entry.getKey();
                IDataSetTypeSlaveServerPlugin plugin = getDataSetTypeSlaveServerPlugin(dataSetType);
                plugin.deleteDataSets(session, dssSessionManager, entry.getValue(), reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext)
    {
        Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IExternalDataTable externalDataTable =
                    businessObjectFactory.createExternalDataTable(session);
            externalDataTable.loadByDataSetCodes(dataSetCodes);
            externalDataTable.uploadLoadedDataSetsToCIFEX(dssSessionManager, uploadContext);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void editExperiment(String sessionToken, ExperimentIdentifier identifier,
            List<ExperimentProperty> properties, List<AttachmentPE> attachments,
            ProjectIdentifier newProjectIdentifier, Date version)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        if (newProjectIdentifier.equals(identifier) == false)
        {
            checkExternalData(identifier, session);
            checkSampleConsistency(identifier, newProjectIdentifier, session);
        }

        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.edit(identifier, properties, attachments, newProjectIdentifier, version);
        experimentBO.save();
    }

    private void checkExternalData(ExperimentIdentifier identifier, final Session session)
    {
        final IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadByExperimentIdentifier(identifier);
        if (externalDataTable.getExternalData().size() > 0)
        {
            throw new UserFailureException(
                    "Changing the project of experiment containing data sets is not allowed.");
        }
    }

    private void checkSampleConsistency(ExperimentIdentifier identifier,
            ProjectIdentifier newProjectIdentifier, final Session session)
    {
        ListSampleCriteriaDTO criteria =
                ListSampleCriteriaDTO.createExperimentIdentifier(identifier);
        final ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        final List<SamplePE> samples = sampleTable.getSamples();
        if (samples.size() > 0)
        {
            checkExperimentGroupMatches(samples.get(0).getSampleIdentifier(), newProjectIdentifier);
        }
    }

    private void checkExperimentGroupMatches(SampleIdentifier sampleIdentifier,
            ProjectIdentifier newProjectIdentifier)
    {
        assert sampleIdentifier != null : "Sample identifier not specified";
        assert newProjectIdentifier != null : "Project identifier not specified";
        final GroupIdentifier sampleGroup = sampleIdentifier.getGroupLevel();
        if (sampleGroup == null)
        {
            throw new UserFailureException(
                    "Inconsistency detected: shared sample found in experiment.");
        }
        if (sampleGroup.getDatabaseInstanceCode().equals(
                newProjectIdentifier.getDatabaseInstanceCode()) == false
                || sampleGroup.getGroupCode().equals(newProjectIdentifier.getGroupCode()) == false)
        {
            throw new UserFailureException(
                    String
                            .format(
                                    "Project cannot be changed to '%s' because experiment containes samples from group '%s'.",
                                    newProjectIdentifier, sampleGroup));
        }
    }

    public void editMaterial(String sessionToken, MaterialIdentifier identifier,
            List<MaterialProperty> properties, Date version)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.edit(identifier, properties, version);
        materialBO.save();

    }

    public void editSample(String sessionToken, SampleIdentifier identifier,
            List<SampleProperty> properties, ExperimentIdentifier experimentIdentifierOrNull,
            Date version)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.edit(identifier, properties, experimentIdentifierOrNull, version);
        sampleBO.save();

    }

    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.load(vocabulary.getCode());
        return vocabularyBO.countTermsUsageStatistics();
    }

    public Set<VocabularyTermPE> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.load(vocabulary.getCode());
        return vocabularyBO.enrichWithTerms();
    }

    public List<DataSetTypePE> listDataSetTypes(String sessionToken)
    {
        return listEntityTypes(sessionToken, EntityKind.DATA_SET);
    }

    public LastModificationState getLastModificationState(String sessionToken)
    {
        checkSession(sessionToken);
        return lastModificationState;
    }
}
