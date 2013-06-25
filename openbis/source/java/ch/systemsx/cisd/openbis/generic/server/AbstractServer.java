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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.IPrincipalProvider;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.DataSetServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.IRemoteHostValidator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityVisitComparatorByTimeStamp;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomColumnTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonRolesTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * An <i>abstract</i> {@link IServer} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServer<T> extends AbstractServiceWithLogger<T> implements IServer
{
    protected static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 360,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private final static String ETL_SERVER_USERNAME_PREFIX = "etlserver";

    protected static final class AuthenticatedPersonBasedPrincipalProvider implements
            IPrincipalProvider
    {
        private final PersonPE person;

        AuthenticatedPersonBasedPrincipalProvider(PersonPE person)
        {
            this.person = person;
        }

        @Override
        public Principal tryToGetPrincipal(String userID)
        {
            Principal result =
                    new Principal(person.getUserId(), person.getFirstName(), person.getLastName(),
                            person.getEmail(), true);
            result.setAnonymous(true);
            return result;
        }
    }

    @Resource(name = ResourceNames.SAMPLE_PLUGIN_REGISTRY)
    private SampleServerPluginRegistry sampleServerPluginRegistry;

    @Resource(name = ResourceNames.DATA_SET_PLUGIN_REGISTRY)
    private DataSetServerPluginRegistry dataSetServerPluginRegistry;

    // For testing purpose.
    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    // For testing purpose.
    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    protected IOpenBisSessionManager sessionManager;

    @Resource(name = ComponentNames.DISPLAY_SETTINGS_PROVIDER)
    protected DisplaySettingsProvider displaySettingsProvider;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.DSS_FACTORY)
    private IDataStoreServiceFactory dssFactory;

    @Resource(name = ComponentNames.REMOTE_HOST_VALIDATOR)
    private IRemoteHostValidator remoteHostValidator;

    @Resource(name = ResourceNames.MAIL_CLIENT_PARAMETERS)
    protected MailClientParameters mailClientParameters;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ComponentNames.PROPERTIES_BATCH_MANAGER)
    private IPropertiesBatchManager propertiesBatchManager;

    private String userForAnonymousLogin;

    protected String CISDHelpdeskEmail;

    protected AbstractServer()
    {
        operationLog.info(String.format("Creating new '%s' implementation: '%s'.",
                IServer.class.getSimpleName(), getClass().getName()));
    }

    protected AbstractServer(final IOpenBisSessionManager sessionManager,
            final IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager)
    {
        this();
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
        this.propertiesBatchManager = propertiesBatchManager;
    }

    // For testing purpose.
    protected AbstractServer(final IOpenBisSessionManager sessionManager,
            final IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        this(sessionManager, daoFactory, propertiesBatchManager);
        this.sampleTypeSlaveServerPlugin = sampleTypeSlaveServerPlugin;
        this.dataSetTypeSlaveServerPlugin = dataSetTypeSlaveServerPlugin;
    }

    // For unit tests - in production Spring will inject this object.
    public void setDisplaySettingsProvider(DisplaySettingsProvider displaySettingsProvider)
    {
        this.displaySettingsProvider = displaySettingsProvider;
    }

    // For unit tests - in production Spring will inject this object.
    public void setDssFactory(IDataStoreServiceFactory dssFactory)
    {
        this.dssFactory = dssFactory;
    }

    protected IPropertiesBatchManager getPropertiesBatchManager()
    {
        return propertiesBatchManager;
    }

    public final void setUserForAnonymousLogin(String userID)
    {
        userForAnonymousLogin = isResolved(userID) ? userID : null;
    }

    public final void setCISDHelpdeskEmail(String cisdHelpdeskEmail)
    {
        this.CISDHelpdeskEmail = cisdHelpdeskEmail;
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

    @Deprecated
    /** @deprecated this is legacy code permanently deleting data sets one by one omitting trash */
    protected void permanentlyDeleteDataSets(Session session, IDataSetTable dataSetTable,
            List<String> dataSetCodes, String reason, boolean forceDisallowedTypes)
    {
        // TODO 2011-06-21, Piotr Buczek: loading less for deletion would probably be faster

        // first load by codes to get the ids
        dataSetTable.loadByDataSetCodes(dataSetCodes, false, false);

        List<DataPE> dataSets = dataSetTable.getDataSets();

        if (atLeastOneOfDataSetsIsContainer(dataSets))
        {
            // get recursively all datasets that are contained and contained
            List<TechId> ids =
                    daoFactory.getDataDAO().listContainedDataSetsRecursively(
                            TechId.createList(dataSetTable.getDataSets()));

            dataSetTable.loadByIds(ids);

            dataSets = dataSetTable.getDataSets();
        }
        Map<DataSetTypePE, List<DataPE>> groupedDataSets =
                new LinkedHashMap<DataSetTypePE, List<DataPE>>();

        // Check all data sets before deleting group by group. If we delete one group of data sets
        // and find an incorrect data set in another group then we cannot roll back already made
        // deletions in data store server. Therefore we'd better make a check now to minimize a
        // chance of failure.
        DataSetTable.assertDatasetsAreDeletable(dataSets);
        DataSetTable.assertDatasetsWithDisallowedTypes(dataSets, forceDisallowedTypes);

        for (DataPE dataSet : dataSets)
        {
            DataSetTypePE dataSetType = dataSet.getDataSetType();
            List<DataPE> list = groupedDataSets.get(dataSetType);
            if (list == null)
            {
                list = new ArrayList<DataPE>();
                groupedDataSets.put(dataSetType, list);
            }
            list.add(dataSet);
        }
        for (Map.Entry<DataSetTypePE, List<DataPE>> entry : groupedDataSets.entrySet())
        {
            DataSetTypePE dataSetType = entry.getKey();
            IDataSetTypeSlaveServerPlugin plugin = getDataSetTypeSlaveServerPlugin(dataSetType);
            plugin.permanentlyDeleteDataSets(session, entry.getValue(), reason,
                    forceDisallowedTypes);
        }
    }

    private boolean atLeastOneOfDataSetsIsContainer(Collection<DataPE> dataSets)
    {
        for (DataPE dataSet : dataSets)
        {
            if (dataSet.isContainer())
            {
                return true;
            }
        }
        return false;
    }

    private final RoleAssignmentPE createRoleAssigment(final PersonPE registrator,
            final PersonPE person, final RoleCode roleCode)
    {
        final RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
        roleAssignmentPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        roleAssignmentPE.setRegistrator(registrator);
        roleAssignmentPE.setRole(roleCode);
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
        person.setActive(true);
        try
        {
            daoFactory.getPersonDAO().createPerson(person);
        } catch (final DataAccessException e)
        {
            throw new UserFailureException(e.getMessage(), e);
        }
        return person;
    }

    private final void updatePersonIfNecessary(final PersonPE person, final Principal principal)
    {
        boolean changed = false;
        if (updateNeeded(person.getEmail(), principal.getEmail()))
        {
            person.setEmail(principal.getEmail());
            changed = true;
        }
        if (updateNeeded(person.getFirstName(), principal.getFirstName()))
        {
            person.setFirstName(principal.getFirstName());
            changed = true;
        }
        if (updateNeeded(person.getLastName(), principal.getLastName()))
        {
            person.setLastName(principal.getLastName());
            changed = true;
        }
        if (changed)
        {
            try
            {
                daoFactory.getPersonDAO().updatePerson(person);
            } catch (final DataAccessException e)
            {
                throw new UserFailureException(e.getMessage(), e);
            }
        }
    }

    private boolean updateNeeded(String currentValue, String newValue)
    {
        if (newValue == null)
        {
            return false;
        }
        return currentValue == null || currentValue.equals(newValue) == false;
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
    @Override
    public void checkSession(final String sessionToken) throws InvalidSessionException
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

    @Override
    public final IAuthSession getAuthSession(final String sessionToken) throws UserFailureException
    {
        return new SimpleSession(sessionManager.getSession(sessionToken));
    }

    @Override
    public int getVersion()
    {
        return 1;
    }

    @Override
    @Transactional(readOnly = true)
    public final void logout(final String sessionToken) throws UserFailureException
    {
        try
        {
            sessionManager.closeSession(sessionToken);
            SessionFactory.cleanUpSessionOnDataStoreServers(sessionToken,
                    daoFactory.getDataStoreDAO(), dssFactory);
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    @Override
    @Transactional(readOnly = true)
    public final void expireSession(final String sessionToken) throws UserFailureException
    {
        try
        {
            sessionManager.expireSession(sessionToken);
            SessionFactory.cleanUpSessionOnDataStoreServers(sessionToken,
                    daoFactory.getDataStoreDAO(), dssFactory);
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deactivatePersons(String sessionToken, List<String> personsCodes)
    {
        checkSession(sessionToken);
        for (String personCode : personsCodes)
        {
            PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(personCode);
            if (person != null)
            {
                IRoleAssignmentDAO roleAssignmenDAO = getDAOFactory().getRoleAssignmentDAO();
                person.setActive(false);
                person.clearAuthorizationGroups();
                // Direct iteration over role assignments could lead to a
                // ConcurrentModificationException because roleAssignmentDAO.deleteRoleAssignment()
                // will remove the assignment from person.
                List<RoleAssignmentPE> roleAssignments =
                        new ArrayList<RoleAssignmentPE>(person.getRoleAssignments());
                for (RoleAssignmentPE roleAssignment : roleAssignments)
                {
                    roleAssignmenDAO.deleteRoleAssignment(roleAssignment);
                }
                getDAOFactory().getPersonDAO().updatePerson(person);
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public int countActivePersons(String sessionToken)
    {
        checkSession(sessionToken);
        return getDAOFactory().getPersonDAO().countActivePersons();
    }

    @Override
    public SessionContextDTO tryAuthenticateAnonymously()
    {
        if (userForAnonymousLogin == null)
        {
            return null;
        }
        final PersonPE person =
                daoFactory.getPersonDAO().tryFindPersonByUserId(userForAnonymousLogin);
        if (person == null)
        {
            return null;
        }
        return tryToAuthenticate(sessionManager.tryToOpenSession(userForAnonymousLogin,
                new AuthenticatedPersonBasedPrincipalProvider(person)));
    }

    @Override
    public final SessionContextDTO tryAuthenticate(final String user, final String password)
    {
        return tryToAuthenticate(sessionManager.tryToOpenSession(user, password));
    }

    private SessionContextDTO tryToAuthenticate(final String sessionToken)
    {
        if (sessionToken == null)
        {
            return null;
        }
        final Session session = sessionManager.getSession(sessionToken);
        List<PersonPE> persons = null;
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(session.getUserName());
        final Set<RoleAssignmentPE> roles;
        if (person == null)
        {
            persons = daoFactory.getPersonDAO().listPersons();
            final PersonPE systemUser = getSystemUser(persons);
            final DisplaySettings defaultDisplaySettings = getDefaultDisplaySettings(sessionToken);
            person = createPerson(session.getPrincipal(), systemUser, defaultDisplaySettings);
            roles = Collections.emptySet();
        } else
        {
            updatePersonIfNecessary(person, session.getPrincipal());
            roles = person.getAllPersonRoles();
            HibernateUtils.initialize(roles);
        }
        if (session.tryGetPerson() == null)
        {
            session.setPerson(person);
            displaySettingsProvider.addDisplaySettingsForPerson(person);
        }

        if (roles.isEmpty())
        {
            if (persons == null)
            {
                persons = daoFactory.getPersonDAO().listPersons();
            }
            if (isFirstLoggedUser(person, persons))
            {
                grantRoleAtFirstLogin(persons, person, RoleCode.ADMIN);
            } else if (isFirstLoggedETLServer(person, persons))
            {
                grantRoleAtFirstLogin(persons, person, RoleCode.ETL_SERVER);
            } else
            {
                authenticationLog.info(String.format(
                        "User '%s' has no role assignments and thus is not permitted to login.",
                        person.getUserId()));
                return null;
            }
        }

        if (false == person.isActive())
        {
            authenticationLog.info(String.format(
                    "User '%s' has been deactivated and thus is not permitted to login.",
                    person.getUserId()));
            return null;
        }

        removeNotExistingVisits(session);

        return asDTO(session);
    }

    @SuppressWarnings("deprecation")
    private void removeNotExistingVisits(Session session)
    {
        if (session == null || session.tryGetPerson() == null)
        {
            return;
        }

        DisplaySettings settings = session.tryGetPerson().getDisplaySettings();
        Iterator<EntityVisit> iterator = settings.getVisits().iterator();
        boolean changed = false;

        while (iterator.hasNext())
        {
            EntityVisit visit = iterator.next();
            EntityKind kind = EntityKind.valueOf(visit.getEntityKind());
            Object entity = null;

            switch (kind)
            {
                case DATA_SET:
                    entity = daoFactory.getDataDAO().tryToFindDataSetByCode(visit.getIdentifier());
                    break;
                case EXPERIMENT:
                    entity = daoFactory.getExperimentDAO().tryGetByPermID(visit.getPermID());
                    break;
                case MATERIAL:
                    entity =
                            daoFactory.getMaterialDAO().tryFindMaterial(
                                    MaterialIdentifier.tryParseIdentifier(visit.getIdentifier()));
                    break;
                case SAMPLE:
                    entity = daoFactory.getSampleDAO().tryToFindByPermID(visit.getPermID());
                    break;
            }

            if (entity == null)
            {
                iterator.remove();
                changed = true;
            }
        }

        if (changed)
        {
            daoFactory.getPersonDAO().updatePerson(session.tryGetPerson());
        }
    }

    private void grantRoleAtFirstLogin(List<PersonPE> persons, PersonPE person, RoleCode roleCode)
    {
        final PersonPE systemUser = getSystemUser(persons);
        if (systemUser.getRoleAssignments().isEmpty())
        {
            final RoleAssignmentPE roleAssignment =
                    createRoleAssigment(systemUser, person, roleCode);
            daoFactory.getRoleAssignmentDAO().createRoleAssignment(roleAssignment);
        }
    }

    private boolean isFirstLoggedUser(PersonPE newPerson, List<PersonPE> persons)
    {
        if (isETLServerUserId(newPerson))
        {
            return false;
        }

        for (PersonPE person : persons)
        {
            if (person.isSystemUser() || isETLServerUserId(person))
            {
                // system & etl users should not receive INSTANCE_ADMIN rights
                // upon first login
            } else
            {
                return false;
            }
        }
        return true;
    }

    private boolean isFirstLoggedETLServer(PersonPE person, List<PersonPE> persons)
    {
        if (false == isETLServerUserId(person))
        {
            return false;
        }

        for (PersonPE existingPerson : persons)
        {
            if (isETLServerUserId(existingPerson))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isETLServerUserId(PersonPE person)
    {
        return person.getUserId().startsWith(ETL_SERVER_USERNAME_PREFIX);
    }

    private SessionContextDTO asDTO(Session session)
    {
        SessionContextDTO result = new SessionContextDTO();
        PersonPE person = session.tryGetPerson();
        assert person != null : "cannot obtain the person which is logged in";
        result.setDisplaySettings(displaySettingsProvider.getRegularDisplaySettings(person));
        SpacePE homeGroup = person.getHomeSpace();
        result.setHomeGroupCode(homeGroup == null ? null : homeGroup.getCode());
        result.setSessionExpirationTime(session.getSessionExpirationTime());
        result.setSessionToken(session.getSessionToken());
        result.setUserName(session.getUserName());
        result.setUserEmail(session.getUserEmail());
        result.setAnonymous(session.isAnonymous());
        result.setUserPersonObject(PersonTranslator.translate(person));
        result.setUserPersonRoles(PersonRolesTranslator.translate(person.getAllPersonRoles()));

        return result;
    }

    @Override
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

    @Override
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

    @SuppressWarnings("deprecation")
    @Override
    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings,
            int maxEntityVisits)
    {
        try
        {
            final Session session = getSession(sessionToken);
            synchronized (session) // synchronized with OpenBisSessionManager.updateAllSessions()
            {
                PersonPE person = session.tryGetPerson();
                if (person != null)
                {
                    synchronized (displaySettingsProvider)
                    {
                        if (maxEntityVisits >= 0)
                        {
                            List<EntityVisit> visits = displaySettings.getVisits();
                            sortAndRemoveMultipleVisits(visits);
                            for (int i = visits.size() - 1; i >= maxEntityVisits; i--)
                            {
                                visits.remove(i);
                            }
                        }
                        displaySettingsProvider.replaceRegularDisplaySettings(person, displaySettings);
                        getDAOFactory().getPersonDAO().updatePerson(person);
                    }
                }
            }
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    @Override
    public void updateDisplaySettings(String sessionToken,
            IDisplaySettingsUpdate displaySettingsUpdate)
    {
        if (displaySettingsUpdate == null)
        {
            throw new IllegalArgumentException("Display settings update cannot be null");
        }

        try
        {
            final Session session = getSession(sessionToken);
            synchronized (session) // synchronized with OpenBisSessionManager.updateAllSessions()
            {
                PersonPE person = session.tryGetPerson();
                if (person != null)
                {
                    synchronized (displaySettingsProvider)
                    {
                        DisplaySettings currentDisplaySettings =
                                displaySettingsProvider.getCurrentDisplaySettings(person);
                        DisplaySettings newDisplaySettings =
                                displaySettingsUpdate.update(currentDisplaySettings);
                        displaySettingsProvider.replaceCurrentDisplaySettings(person,
                                newDisplaySettings);
                        getDAOFactory().getPersonDAO().updatePerson(person);
                    }
                }
            }
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    private void sortAndRemoveMultipleVisits(List<EntityVisit> visits)
    {
        Collections.sort(visits, new EntityVisitComparatorByTimeStamp());
        Set<String> permIds = new HashSet<String>();
        for (Iterator<EntityVisit> iterator = visits.iterator(); iterator.hasNext();)
        {
            EntityVisit entityVisit = iterator.next();
            String permID = entityVisit.getPermID();
            if (permIds.contains(permID))
            {
                iterator.remove();
            }
            permIds.add(permID);
        }
    }

    @Override
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

    @Override
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
            getDAOFactory().getPersonDAO().updatePerson(person);
        }
    }

    @Override
    public void setBaseIndexURL(String sessionToken, String baseIndexURL)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        session.setBaseIndexURL(baseIndexURL);
    }

    @Override
    public String getBaseIndexURL(String sessionToken)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        return session.getBaseIndexURL();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExpressionValidator.class)
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

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void setSessionUser(String sessionToken, String userID)
    {
        Session session = getSession(sessionToken);
        String remoteHost = session.getRemoteHost();
        if (remoteHostValidator.isValidRemoteHost(remoteHost) == false)
        {
            throw new UserFailureException("It is not allowed to change the user from remote host "
                    + remoteHost);
        }
        injectPerson(session, userID);
    }

    protected void injectPerson(Session session, String personID)
    {
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(personID);
        if (person == null)
        {
            throw new UserFailureException("Unknown user: " + personID);
        }
        HibernateUtils.initialize(person.getAllPersonRoles());
        session.setPerson(person);
        displaySettingsProvider.addDisplaySettingsForPerson(person);
    }

    protected void registerSamples(final Session session,
            final NewSamplesWithTypes newSamplesWithType, PersonPE registratorOrNull)
    {
        final SampleType sampleType = newSamplesWithType.getEntityType();
        final List<NewSample> newSamples = newSamplesWithType.getNewEntities();
        assert sampleType != null : "Unspecified sample type.";
        assert newSamples != null : "Unspecified new samples.";

        // Does nothing if samples list is empty.
        if (newSamples.size() == 0)
        {
            return;
        }
        fillHomeSpace(newSamples, session.tryGetHomeGroupCode());
        ServerUtils.prevalidate(newSamples, "sample");
        final String sampleTypeCode = sampleType.getCode();
        final SampleTypePE sampleTypePE =
                getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleTypePE == null)
        {
            throw UserFailureException.fromTemplate("Sample type with code '%s' does not exist.",
                    sampleTypeCode);
        }
        getPropertiesBatchManager().manageProperties(sampleTypePE, newSamples, registratorOrNull);
        getSampleTypeSlaveServerPlugin(sampleTypePE).registerSamples(session, newSamples,
                registratorOrNull);
    }

    protected static void fillHomeSpace(List<NewSample> samples, String homeSpaceOrNull)
    {
        if (homeSpaceOrNull == null)
        {
            return;
        }
        String spaceIdentifier = new SpaceIdentifier(homeSpaceOrNull).toString();
        for (NewSample sample : samples)
        {
            if (sample.getDefaultSpaceIdentifier() == null)
            {
                sample.setDefaultSpaceIdentifier(spaceIdentifier);
            }
        }
    }

    protected void executeASync(final String userEmail, final IASyncAction action)
    {
        final IMailClient mailClient = new MailClient(mailClientParameters);
        Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    StringWriter writer = new StringWriter();
                    boolean success = true;
                    Date startDate = new Date();
                    try
                    {
                        success = action.doAction(writer);
                    } catch (RuntimeException e)
                    {
                        operationLog.error("Asynchronous action '" + action.getName()
                                + "' failed. ", e);
                        success = false;
                    } finally
                    {
                        sendEmail(mailClient, writer.toString(),
                                getSubject(action.getName(), startDate, success), userEmail);
                    }
                }
            };
        executor.submit(task);
    }

    protected void sendEmail(IMailClient mailClient, String content, String subject,
            String... recipient)
    {
        mailClient.sendMessage(subject, content, null, null, recipient);
    }

    private static String getSubject(String actionName, Date startDate, boolean success)
    {
        return addDate(actionName + " " + (success ? "successfully performed" : "failed"),
                startDate);
    }

    private static String addDate(String subject, Date startDate)
    {
        return subject + " (initiated at " + startDate + ")";
    }

    static boolean isResolved(String name)
    {
        return StringUtils.isNotBlank(name) && name.startsWith("${") == false;
    }

}
