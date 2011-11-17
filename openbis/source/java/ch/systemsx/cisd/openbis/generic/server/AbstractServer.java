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
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.PropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.DataSetServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.IRemoteHostValidator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityVisitComparatorByTimeStamp;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
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
    protected ISessionManager<Session> sessionManager;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.REMOTE_HOST_VALIDATOR)
    private IRemoteHostValidator remoteHostValidator;

    @Resource(name = ResourceNames.MAIL_CLIENT_PARAMETERS)
    protected MailClientParameters mailClientParameters;

    private IPropertiesBatchManager propertiesBatchManager;

    private String userForAnonymousLogin;

    protected AbstractServer()
    {
        operationLog.info(String.format("Creating new '%s' implementation: '%s'.",
                IServer.class.getSimpleName(), getClass().getName()));
    }

    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager)
    {
        this();
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
        this.propertiesBatchManager = propertiesBatchManager;
    }

    // For testing purpose.
    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        this(sessionManager, daoFactory, propertiesBatchManager);
        this.sampleTypeSlaveServerPlugin = sampleTypeSlaveServerPlugin;
        this.dataSetTypeSlaveServerPlugin = dataSetTypeSlaveServerPlugin;
    }

    protected IPropertiesBatchManager getPropertiesBatchManager()
    {
        if (propertiesBatchManager == null)
        {
            propertiesBatchManager = new PropertiesBatchManager();
        }
        return propertiesBatchManager;
    }

    public final void setUserForAnonymousLogin(String userID)
    {
        userForAnonymousLogin = userID != null && userID.startsWith("$") == false ? userID : null;
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
            List<String> dataSetCodes, String reason, boolean force)
    {
        // TODO 2011-06-21, Piotr Buczek: loading less for deletion would probably be faster
        dataSetTable.loadByDataSetCodes(dataSetCodes, false, false);
        List<DataPE> dataSets = dataSetTable.getDataSets();
        Map<DataSetTypePE, List<DataPE>> groupedDataSets =
                new LinkedHashMap<DataSetTypePE, List<DataPE>>();
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
            plugin.permanentlyDeleteDataSets(session, entry.getValue(), reason, force);
        }
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

    @Transactional(readOnly = true)
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

    public SessionContextDTO tryToAuthenticateAnonymously()
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

    public final SessionContextDTO tryToAuthenticate(final String user, final String password)
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
        final List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        assert persons.size() > 0 : "At least system user should be in the database";
        // If only one user (system user), then this is the first logged user.
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

        if (roles.isEmpty())
        {
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

        return asDTO(session);
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
        result.setAnonymous(session.isAnonymous());
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

    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings,
            int maxEntityVisits)
    {
        try
        {
            final Session session = getSessionManager().getSession(sessionToken);
            PersonPE person = session.tryGetPerson();
            if (person != null)
            {
                List<EntityVisit> visits = joinVisits(displaySettings, person);
                sortAndRemoveMultipleVisits(visits);
                for (int i = visits.size() - 1; i >= maxEntityVisits; i--)
                {
                    visits.remove(i);
                }
                person.setDisplaySettings(displaySettings);
                getDAOFactory().getPersonDAO().updatePerson(person);
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

    @SuppressWarnings("deprecation")
    private List<EntityVisit> joinVisits(DisplaySettings displaySettings, PersonPE person)
    {
        List<EntityVisit> personVisits = person.getDisplaySettings().getVisits();
        if (displaySettings == null)
        {
            return personVisits;
        }
        List<EntityVisit> visits = displaySettings.getVisits();
        visits.addAll(personVisits);
        return visits;
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

    private void sendEmail(IMailClient mailClient, String content, String subject, String recipient)
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
}
