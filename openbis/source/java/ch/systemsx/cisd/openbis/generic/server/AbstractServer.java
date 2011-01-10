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
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.DataSetServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.IRemoteHostValidator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleSession;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomColumnTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * An <i>abstract</i> {@link IServer} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServer<T> extends AbstractServiceWithLogger<T> implements IServer
{
    @Resource(name = ResourceNames.SAMPLE_PLUGIN_REGISTRY)
    private SampleServerPluginRegistry sampleServerPluginRegistry;

    @Resource(name = ResourceNames.DATA_SET_PLUGIN_REGISTRY)
    private DataSetServerPluginRegistry dataSetServerPluginRegistry;

    // For testing purpose.
    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    // For testing purpose.
    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    protected ISessionManager<Session> sessionManager;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.REMOTE_HOST_VALIDATOR)
    private IRemoteHostValidator remoteHostValidator;

    protected AbstractServer()
    {
        operationLog.info(String.format("Creating new '%s' implementation: '%s'.",
                IServer.class.getSimpleName(), getClass().getName()));
    }

    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory)
    {
        this();
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
    }

    // For testing purpose.
    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        this(sessionManager, daoFactory);
        this.sampleTypeSlaveServerPlugin = sampleTypeSlaveServerPlugin;
        this.dataSetTypeSlaveServerPlugin = dataSetTypeSlaveServerPlugin;
    }

    public final void setSampleTypeSlaveServerPlugin(
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin)
    {
        this.sampleTypeSlaveServerPlugin = sampleTypeSlaveServerPlugin;
    }

    public final void setDataSetTypeSlaveServerPlugin(
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        this.dataSetTypeSlaveServerPlugin = dataSetTypeSlaveServerPlugin;
    }

    protected final ISampleTypeSlaveServerPlugin getSampleTypeSlaveServerPlugin(
            final SampleTypePE sampleType)
    {
        if (sampleTypeSlaveServerPlugin != null)
        {
            return sampleTypeSlaveServerPlugin;
        }
        return sampleServerPluginRegistry.getPlugin(EntityKind.SAMPLE, sampleType).getSlaveServer();
    }

    protected final IDataSetTypeSlaveServerPlugin getDataSetTypeSlaveServerPlugin(
            final DataSetTypePE dataSetType)
    {
        if (dataSetTypeSlaveServerPlugin != null)
        {
            return dataSetTypeSlaveServerPlugin;
        }
        return dataSetServerPluginRegistry.getPlugin(EntityKind.DATA_SET, dataSetType)
                .getSlaveServer();
    }

    private final RoleAssignmentPE createInstanceAdminRoleAssigment(final PersonPE registrator,
            final PersonPE person)
    {
        final RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
        roleAssignmentPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        roleAssignmentPE.setRegistrator(registrator);
        roleAssignmentPE.setRole(RoleCode.ADMIN);
        person.addRoleAssignment(roleAssignmentPE);
        return roleAssignmentPE;
    }

    protected final PersonPE createPerson(final Principal principal, final PersonPE registrator,
            DisplaySettings defaultDisplaySettings)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        person.setRegistrator(registrator);
        person.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        person.setDisplaySettings(defaultDisplaySettings);
        try
        {
            daoFactory.getPersonDAO().createPerson(person);
        } catch (final DataAccessException e)
        {
            throw new UserFailureException(e.getMessage(), e);
        }
        return person;
    }

    protected final PersonPE getSystemUser()
    {
        return getSystemUser(daoFactory.getPersonDAO().listPersons());
    }

    private final static PersonPE getSystemUser(final List<PersonPE> persons)
    {
        for (final PersonPE personPE : persons)
        {
            if (personPE.isSystemUser())
            {
                return personPE;
            }
        }
        throw new IllegalStateException(String.format(
                "No system user could be found in given list '%s'.", persons));
    }

    protected final ISessionManager<Session> getSessionManager()
    {
        return sessionManager;
    }

    protected final IDAOFactory getDAOFactory()
    {
        return daoFactory;
    }

    // Call this when session object is not needed but you want just to
    // refresh/check the session.
    protected void checkSession(final String sessionToken)
    {
        getSession(sessionToken);
    }

    protected Session getSession(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";

        return getSessionManager().getSession(sessionToken);
    }

    //
    // IServer
    //

    public final IAuthSession getAuthSession(final String sessionToken) throws UserFailureException
    {
        return new SimpleSession(sessionManager.getSession(sessionToken));
    }

    public int getVersion()
    {
        return 1;
    }

    public final void logout(final String sessionToken) throws UserFailureException
    {
        try
        {
            sessionManager.closeSession(sessionToken);
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    public final SessionContextDTO tryToAuthenticate(final String user, final String password)
    {
        final String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        final Session session = sessionManager.getSession(sessionToken);
        final List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        assert persons.size() > 0 : "At least system user should be in the database";
        // If only one user (system user), then this is the first logged user.
        final boolean isFirstLoggedUser = (persons.size() == 1);
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(session.getUserName());
        final Set<RoleAssignmentPE> roles;
        if (person == null)
        {
            final PersonPE systemUser = getSystemUser(persons);
            final DisplaySettings defaultDisplaySettings = getDefaultDisplaySettings(sessionToken);
            person = createPerson(session.getPrincipal(), systemUser, defaultDisplaySettings);
            roles = Collections.emptySet();
        } else
        {
            roles = person.getAllPersonRoles();
            HibernateUtils.initialize(roles);
        }
        if (session.tryGetPerson() == null)
        {
            session.setPerson(person);
        }
        if (isFirstLoggedUser)
        {
            // If system user does not have any role assignment yet make him database instance
            // administrator.
            final PersonPE systemUser = getSystemUser(persons);
            if (systemUser.getRoleAssignments().isEmpty())
            {
                final RoleAssignmentPE roleAssignment =
                        createInstanceAdminRoleAssigment(systemUser, person);
                daoFactory.getRoleAssignmentDAO().createRoleAssignment(roleAssignment);
            }
        } else if (roles.isEmpty())
        {
            authenticationLog.info(String.format(
                    "User '%s' has no role assignments and thus is not permitted to login.",
                    person.getUserId()));
            return null;
        }
        return asDTO(session);
    }

    private static SessionContextDTO asDTO(Session session)
    {
        SessionContextDTO result = new SessionContextDTO();
        PersonPE person = session.tryGetPerson();
        assert person != null : "cannot obtain the person which is logged in";
        result.setDisplaySettings(person.getDisplaySettings());
        SpacePE homeGroup = person.getHomeSpace();
        result.setHomeGroupCode(homeGroup == null ? null : homeGroup.getCode());
        result.setSessionExpirationTime(session.getSessionExpirationTime());
        result.setSessionToken(session.getSessionToken());
        result.setUserName(session.getUserName());
        result.setUserEmail(session.getUserEmail());
        return result;
    }

    public SessionContextDTO tryGetSession(String sessionToken)
    {
        try
        {
            final Session session = sessionManager.getSession(sessionToken);
            return asDTO(session);
        } catch (InvalidSessionException ex)
        {
            return null;
        }
    }

    public boolean isArchivingConfigured(String sessionToken)
    {
        final List<DataStorePE> stores = daoFactory.getDataStoreDAO().listDataStores();
        for (DataStorePE store : stores)
        {
            if (store.isArchiverConfigured())
            {
                return true;
            }
        }
        return false;
    }

    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings)
    {
        try
        {
            final Session session = getSessionManager().getSession(sessionToken);
            PersonPE person = session.tryGetPerson();
            if (person != null)
            {
                person.setDisplaySettings(displaySettings);
                getDAOFactory().getPersonDAO().updatePerson(person);
            }
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    public DisplaySettings getDefaultDisplaySettings(String sessionToken)
    {
        PersonPE systemUser =
                getDAOFactory().getPersonDAO().tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
        if (systemUser == null)
        {
            throw new UserFailureException(
                    "Couldn't find system user with default settings in the DB.");
        }
        return systemUser.getDisplaySettings();
    }

    public void changeUserHomeSpace(String sessionToken, TechId groupIdOrNull)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        PersonPE person = session.tryGetPerson();
        if (person != null)
        {
            SpacePE homeGroup =
                    groupIdOrNull == null ? null : getDAOFactory().getSpaceDAO().getByTechId(
                            groupIdOrNull);
            person.setHomeSpace(homeGroup);
            // don't need to updatePerson(person) with DAO because it is attached to a session
        }
    }

    public void setBaseIndexURL(String sessionToken, String baseIndexURL)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        session.setBaseIndexURL(baseIndexURL);
    }

    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridId)
    {
        Session session = getSession(sessionToken);

        List<GridCustomColumnPE> columnPEs =
                getDAOFactory().getGridCustomColumnDAO().listColumns(gridId);

        List<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
        List<GridCustomColumn> columns = GridCustomColumnTranslator.translate(columnPEs);
        // we have to remove private columns of different users to avoid calculating them
        ExpressionValidator validator = new ExpressionValidator();
        PersonPE currentPerson = session.tryGetPerson();
        for (GridCustomColumn column : columns)
        {
            if (validator.isValid(currentPerson, column))
            {
                result.add(column);
            }
        }
        return result;
    }

    public void setSessionUser(String sessionToken, String userID)
    {
        Session session = getSession(sessionToken);
        String remoteHost = session.getRemoteHost();
        if (remoteHostValidator.isValidRemoteHost(remoteHost) == false)
        {
            throw new UserFailureException("It is not allowed to change the user from remote host "
                    + remoteHost);
        }
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(userID);
        if (person == null)
        {
            throw new UserFailureException("Unknown user: " + userID);
        }
        HibernateUtils.initialize(person.getAllPersonRoles());
        session.setPerson(person);
    }

    protected void registerSamples(final Session session,
            final NewSamplesWithTypes newSamplesWithType, PersonPE registratorOrNull)
    {
        final SampleType sampleType = newSamplesWithType.getSampleType();
        final List<NewSample> newSamples = newSamplesWithType.getNewSamples();
        assert sampleType != null : "Unspecified sample type.";
        assert newSamples != null : "Unspecified new samples.";

        // Does nothing if samples list is empty.
        if (newSamples.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newSamples, "sample");
        final String sampleTypeCode = sampleType.getCode();
        final SampleTypePE sampleTypePE =
                getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleTypePE == null)
        {
            throw UserFailureException.fromTemplate("Sample type with code '%s' does not exist.",
                    sampleTypeCode);
        }
        getSampleTypeSlaveServerPlugin(sampleTypePE).registerSamples(session, newSamples,
                registratorOrNull);
    }
}
