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
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
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

    public CommonServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory);
        this.authenticationService = authenticationService;
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // AbstractServer
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
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
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
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return persons;
    }

    public final List<ProjectPE> listProjects(final String sessionToken)
    {
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return projects;
    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
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
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
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
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
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
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
        final List<ExperimentTypePE> experimentTypes =
                getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT).listEntityTypes();
        Collections.sort(experimentTypes);
        return experimentTypes;
    }

    public final List<DataTypePE> listDataTypes(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
        final List<DataTypePE> dataTypes = getDAOFactory().getPropertyTypeDAO().listDataTypes();
        Collections.sort(dataTypes);
        return dataTypes;
    }

    public final List<VocabularyPE> listVocabularies(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        // Not needed but just to refresh/check the session.
        getSessionManager().getSession(sessionToken);
        final List<VocabularyPE> vocabularies =
                getDAOFactory().getVocabularyDAO().listVocabularies();
        for (final VocabularyPE vocabularyPE : vocabularies)
        {
            HibernateUtils.initialize(vocabularyPE.getTerms());
        }
        Collections.sort(vocabularies);
        return vocabularies;
    }

    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue)
    {
        assert sessionToken != null : "Unspecified session token";

        getSessionManager().getSession(sessionToken);
        return "Warning: feature not implemented";

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
        final IVocabularyBO propertyTypeBO = businessObjectFactory.createVocabularyBO(session);
        propertyTypeBO.define(vocabulary);
        propertyTypeBO.save();
    }
}
