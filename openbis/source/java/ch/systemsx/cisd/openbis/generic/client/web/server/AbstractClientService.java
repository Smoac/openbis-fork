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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CachedResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager.TokenBasedResultSetKeyGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IDataStoreBaseURLProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An <i>abstract</i> {@link IClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientService implements IClientService
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractClientService.class);

    @Resource(name = "request-context-provider")
    private IRequestContextProvider requestContextProvider;

    @Resource(name = "common-service")
    protected IDataStoreBaseURLProvider dataStoreBaseURLProvider;

    private String cifexURL;

    private String cifexRecipient;

    protected AbstractClientService()
    {
    }

    protected AbstractClientService(final IRequestContextProvider requestContextProvider)
    {
        this.requestContextProvider = requestContextProvider;
    }

    protected String getDataStoreBaseURL()
    {
        return dataStoreBaseURLProvider.getDataStoreBaseURL();
    }

    @SuppressWarnings("unchecked")
    protected final <K> IResultSetManager<K> getResultSetManager()
    {
        HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            throw new ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException(
                    "Your session has expired, please log in again.");
        }
        return (IResultSetManager<K>) httpSession
                .getAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
    }

    protected final <T extends IEntityInformationHolder> ResultSetWithEntityTypes<T> listEntitiesWithTypes(
            final IResultSetConfig<String, T> criteria, IOriginalDataProvider<T> dataProvider)
    {
        ResultSet<T> resultSet = listEntities(criteria, dataProvider);
        Set<BasicEntityType> entityTypes =
                fetchEntityTypes(dataProvider, resultSet.getResultSetKey());
        return new ResultSetWithEntityTypes<T>(resultSet, entityTypes);
    }

    protected final <T> IResultSet<String, T> getResultSet(
            IResultSetConfig<String, T> resultSetConfig, IOriginalDataProvider<T> dummyDataProvider)
    {
        final IResultSetManager<String> resultSetManager = getResultSetManager();
        final IResultSet<String, T> result =
                resultSetManager.getResultSet(resultSetConfig, dummyDataProvider);
        return result;
    }

    protected final <T extends IEntityInformationHolder> Set<BasicEntityType> fetchEntityTypes(
            IOriginalDataProvider<T> dataProvider, String resultSetKey)
    {
        DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setResultSetKey(resultSetKey);
        final IResultSet<String, T> allData = getResultSet(criteria, dataProvider);
        Set<BasicEntityType> result = new HashSet<BasicEntityType>();
        for (T row : allData.getList())
        {
            result.add(row.getEntityType());
        }
        return result;
    }

    protected final <T> ResultSet<T> listEntities(final IResultSetConfig<String, T> criteria,
            IOriginalDataProvider<T> dataProvider)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final IResultSet<String, T> result = getResultSet(criteria, dataProvider);
            return ResultSetTranslator.translate(result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void setCifexURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    public final void setCifexRecipient(String cifexRecipient)
    {
        this.cifexRecipient = cifexRecipient;
    }

    private final SessionContext createSessionContext(final Session session)
    {
        final SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionID(session.getSessionToken());
        final User user = new User();
        user.setUserName(session.getUserName());
        final PersonPE person = session.tryGetPerson();
        if (person != null)
        {
            DisplaySettings displaySettings = person.getDisplaySettings();
            sessionContext.setDisplaySettings(displaySettings);
            final GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup != null)
            {
                user.setHomeGroupCode(homeGroup.getCode());
            }
        }
        sessionContext.setUser(user);
        return sessionContext;
    }

    protected final String getSessionToken()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            throw new InvalidSessionException("Session expired. Please login again.");
        }
        return getSession(httpSession).getSessionToken();
    }

    private final Session getSession(final HttpSession httpSession)
    {
        final Session session =
                (Session) httpSession.getAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
        if (session == null)
        {
            final String remoteHost =
                    requestContextProvider.getHttpServletRequest().getRemoteHost();
            final String msg =
                    "Attempt to get non-existent session from host '" + remoteHost
                            + "': user is not logged in.";
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(msg);
            }
            throw new InvalidSessionException(msg);

        }
        return session;
    }

    protected final HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }

    private final HttpSession creatHttpSession()
    {
        return getOrCreateHttpSession(true);
    }

    private final HttpSession getOrCreateHttpSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    @SuppressWarnings("unchecked")
    protected final <T> CacheManager<String, T> getExportManager()
    {
        return (CacheManager<String, T>) getHttpSession().getAttribute(
                SessionConstants.OPENBIS_EXPORT_MANAGER);
    }

    protected final <T> String prepareExportEntities(TableExportCriteria<T> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            final CacheManager<String, TableExportCriteria<T>> exportManager = getExportManager();
            return exportManager.saveData(criteria);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private CachedResultSetManager<String> createCachedResultSetManager()
    {
        return new CachedResultSetManager<String>(new TokenBasedResultSetKeyGenerator());
    }

    /** Returns the {@link IServer} implementation for this client service. */
    protected abstract IServer getServer();

    //
    // IClientService
    //

    public final ApplicationInfo getApplicationInfo()
    {
        final ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        applicationInfo.setCIFEXURL(cifexURL);
        applicationInfo.setCifexRecipient(cifexRecipient);
        return applicationInfo;
    }

    public final SessionContext tryToGetCurrentSessionContext()
    {
        try
        {
            final HttpSession httpSession = getHttpSession();
            if (httpSession == null)
            {
                return null;
            }
            final Session session = getSession(httpSession);
            return createSessionContext(session);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final SessionContext tryToLogin(final String userID, final String password)
    {
        try
        {
            final Session session = getServer().tryToAuthenticate(userID, password);
            if (session == null)
            {
                return null;
            }
            final HttpSession httpSession = creatHttpSession();
            // Expiration time of httpSession is 10 seconds less than of session
            final int sessionExpirationTimeInMillis = session.getSessionExpirationTime();
            final int sessionExpirationTimeInSeconds = sessionExpirationTimeInMillis / 1000;
            if (sessionExpirationTimeInMillis < 0)
            {
                httpSession.setMaxInactiveInterval(-1);
            } else if (sessionExpirationTimeInMillis < 1000 || sessionExpirationTimeInSeconds < 10)
            {
                httpSession.setMaxInactiveInterval(0);
            } else
            {
                httpSession.setMaxInactiveInterval(sessionExpirationTimeInSeconds - 10);
            }
            httpSession.setAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY, session);
            httpSession.setAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY, getServer());
            httpSession.setAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER,
                    createCachedResultSetManager());
            httpSession.setAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER, CacheManager
                    .createCacheManager());
            return createSessionContext(session);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } catch (final IllegalStateException e)
        {
            operationLog.error("Session already invalidated.", e);
            return null;
        }
    }

    public void setBaseURL(String baseURL)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.setBaseIndexURL(sessionToken, baseURL);
        } catch (InvalidSessionException e)
        {
            // ignored
        }
    }

    public void updateDisplaySettings(DisplaySettings displaySettings)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.saveDisplaySettings(sessionToken, displaySettings);
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void changeUserHomeGroup(TechId groupIdOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.changeUserHomeGroup(sessionToken, groupIdOrNull);
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void logout(DisplaySettings displaySettings)
    {
        try
        {
            final HttpSession httpSession = getHttpSession(); // FIXME
            if (httpSession != null)
            {
                final Session session = getSession(httpSession);
                httpSession.removeAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
                httpSession.removeAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);
                httpSession.removeAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
                httpSession.removeAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER);
                httpSession.invalidate();
                String sessionToken = session.getSessionToken();
                IServer server = getServer();
                server.saveDisplaySettings(sessionToken, displaySettings);
                server.logout(sessionToken);
            }
        } catch (final UserFailureException e)
        {
            // Just ignore it because we are logging out anyway.
        }
    }
}
