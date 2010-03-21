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
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.IOnlineHelpResourceLocatorService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CachedResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ICustomColumnsProvider;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * An <i>abstract</i> {@link IClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientService implements IClientService,
        IOnlineHelpResourceLocatorService
{
    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractClientService.class);

    @Resource(name = "request-context-provider")
    private IRequestContextProvider requestContextProvider;

    @Resource(name = "common-service")
    protected IDataStoreBaseURLProvider dataStoreBaseURLProvider;

    private String cifexURL;

    private String cifexRecipient;

    private String onlineHelpGenericRootURL;

    private String onlineHelpGenericPageTemplate;

    private String onlineHelpSpecificRootURL;

    private String onlineHelpSpecificPageTemplate;

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
                resultSetManager
                        .getResultSet(getSessionToken(), resultSetConfig, dummyDataProvider);
        return result;
    }

    protected final <T extends IEntityInformationHolder> Set<BasicEntityType> fetchEntityTypes(
            IOriginalDataProvider<T> dataProvider, String resultSetKey)
    {
        DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setCacheConfig(ResultSetFetchConfig.createFetchFromCache(resultSetKey));
        final IResultSet<String, T> allData = getResultSet(criteria, dataProvider);
        Set<BasicEntityType> result = new HashSet<BasicEntityType>();
        for (T row : allData.getList().extractOriginalObjects())
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

    protected static <T> IOriginalDataProvider<T> createDummyDataProvider()
    {
        return new IOriginalDataProvider<T>()
            {
                public List<T> getOriginalData() throws UserFailureException
                {
                    throw new IllegalStateException("Data not found in the cache");
                }
            };
    }

    public final void setCifexURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set CIFEX URL for client to '" + cifexURL + "'.");
        }
    }

    public final void setCifexRecipient(String cifexRecipient)
    {
        this.cifexRecipient = cifexRecipient;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set CIFEX recipient for client to '" + cifexRecipient + "'.");
        }
    }

    public String getOnlineHelpGenericRootURL()
    {
        return onlineHelpGenericRootURL;
    }

    public void setOnlineHelpGenericRootURL(String onlineHelpGenericRootURL)
    {
        this.onlineHelpGenericRootURL = onlineHelpGenericRootURL;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set root ULR for generic online help to '"
                    + onlineHelpGenericRootURL + "'.");
        }
    }

    public String getOnlineHelpGenericPageTemplate()
    {
        return onlineHelpGenericPageTemplate;
    }

    public void setOnlineHelpGenericPageTemplate(String onlineHelpGenericPageTemplate)
    {
        this.onlineHelpGenericPageTemplate = onlineHelpGenericPageTemplate;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set template for generic online help to '"
                    + onlineHelpGenericPageTemplate + "'.");
        }
    }

    public String getOnlineHelpSpecificRootURL()
    {
        return onlineHelpSpecificRootURL;
    }

    public void setOnlineHelpSpecificRootURL(String onlineHelpSpecificRootURL)
    {
        this.onlineHelpSpecificRootURL = onlineHelpSpecificRootURL;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set root ULR for specific online help to '"
                    + onlineHelpSpecificRootURL + "'.");
        }
    }

    public String getOnlineHelpSpecificPageTemplate()
    {
        return onlineHelpSpecificPageTemplate;
    }

    public void setOnlineHelpSpecificPageTemplate(String onlineHelpSpecificPageTemplate)
    {
        this.onlineHelpSpecificPageTemplate = onlineHelpSpecificPageTemplate;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set template for specific online help to '"
                    + onlineHelpSpecificPageTemplate + "'.");
        }
    }

    private final SessionContext createSessionContext(final SessionContextDTO session)
    {
        final SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionID(session.getSessionToken());

        DisplaySettings displaySettings = session.getDisplaySettings();
        sessionContext.setDisplaySettings(displaySettings);

        final User user = new User();
        user.setUserName(session.getUserName());
        user.setHomeGroupCode(session.tryGetHomeGroupCode());
        sessionContext.setUser(user);

        return sessionContext;
    }

    protected final String getSessionToken()
    {
        final HttpSession httpSession = getHttpSession();
        String sessionToken = null;
        if (httpSession != null)
        {
            sessionToken =
                    (String) httpSession
                            .getAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
        }
        if (sessionToken == null)
        {
            throw new InvalidSessionException("Session expired. Please login again.");
        }
        return sessionToken;
    }

    protected final HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }

    private final HttpSession createHttpSession()
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
        return new CachedResultSetManager<String>(new TokenBasedResultSetKeyGenerator(),
                new ICustomColumnsProvider()
                    {
                        public List<GridCustomColumn> getGridCustomColumn(String sessionToken,
                                String gridDisplayId)
                        {
                            return getServer().listGridCustomColumns(sessionToken, gridDisplayId);
                        }
                    });
    }

    // Saves the specified rows in the cache.
    // Returns a key in the cache where the data were saved.
    protected <T> String saveInCache(final List<T> tableModelRows)
    {
        DefaultResultSetConfig<String, T> criteria = new DefaultResultSetConfig<String, T>();
        criteria.setLimit(0); // we do not need any data now, just a key
        ResultSet<T> resultSet = listEntities(criteria, new IOriginalDataProvider<T>()
            {
                public List<T> getOriginalData() throws UserFailureException
                {
                    return tableModelRows;
                }
            });
        return resultSet.getResultSetKey();
    }

    /** Returns the {@link IServer} implementation for this client service. */
    protected abstract IServer getServer();

    //
    // IClientService
    //

    public final ApplicationInfo getApplicationInfo()
    {
        final ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(getVersion());
        applicationInfo.setCIFEXURL(cifexURL);
        applicationInfo.setCifexRecipient(cifexRecipient);
        return applicationInfo;
    }

    protected String getVersion()
    {
        return BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
    }

    public final SessionContext tryToGetCurrentSessionContext()
    {
        try
        {
            final SessionContextDTO session = getServer().tryGetSession(getSessionToken());
            if (session == null)
            {
                return null;
            }
            return createSessionContext(session);
        } catch (final InvalidSessionException e)
        {
            return null;
        }
    }

    public final SessionContext tryToLogin(final String userID, final String password)
    {
        try
        {
            final SessionContextDTO session = getServer().tryToAuthenticate(userID, password);
            if (session == null)
            {
                return null;
            }
            final HttpSession httpSession = createHttpSession();
            // Expiration time of httpSession is 10 seconds less than of session
            final int sessionExpirationTimeInMillis = session.getSessionExpirationTime();
            final int sessionExpirationTimeInSeconds = sessionExpirationTimeInMillis / 1000;
            if (sessionExpirationTimeInMillis < 0)
            {
                httpSession.setMaxInactiveInterval(-1);
            } else if (sessionExpirationTimeInSeconds < 10)
            {
                httpSession.setMaxInactiveInterval(0);
            } else
            {
                httpSession.setMaxInactiveInterval(sessionExpirationTimeInSeconds - 10);
            }
            httpSession.setAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY, session
                    .getSessionToken());
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
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Set openBIS base URL for client to '" + baseURL + "'.");
            }
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

    public DisplaySettings resetDisplaySettings()
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            final DisplaySettings defaultSettings = server.getDefaultDisplaySettings(sessionToken);
            updateDisplaySettings(defaultSettings);
            return defaultSettings;
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
        return null;
    }

    public void changeUserHomeGroup(TechId groupIdOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.changeUserHomeSpace(sessionToken, groupIdOrNull);
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
            final HttpSession httpSession = getHttpSession();
            if (httpSession != null)
            {
                String sessionToken = getSessionToken();
                httpSession.removeAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
                httpSession.removeAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);
                httpSession.removeAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
                httpSession.removeAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER);
                httpSession.invalidate();
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
