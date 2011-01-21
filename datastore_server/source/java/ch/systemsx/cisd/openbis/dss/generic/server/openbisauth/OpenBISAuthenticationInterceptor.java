/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.openbisauth;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.server.SessionTokenManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.dss.generic.shared.ManagedAuthentication;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Transparently handles openBIS authentication for methods annotated with
 * {@link ManagedAuthentication}.
 * <p>
 * DSS is authenticated upon the first openBIS method invocation and the session token is kept in an
 * {@link OpenBISSessionHolder} from where it can be accessed by the advised instances (e.g.
 * {@link EncapsulatedOpenBISService}).
 * </p>
 * <p>
 * Sessions can go stale for reasons like openBIS server restart or network problems. Stale session
 * tokens result in {@link InvalidSessionException} being thrown by the advised methods. If this
 * happens a re-authentication request is issued automatically and the failed method invocation is
 * retried once.
 * </p>
 * <p>
 * This class is thread safe (otherwise one thread can change the session and cause the other thread
 * to use the invalid one).
 * </p>
 * 
 * @author Kaloyan Enimanev
 */
public class OpenBISAuthenticationInterceptor implements MethodInterceptor
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            OpenBISAuthenticationAdvisor.class);

    private final SessionTokenManager sessionTokenManager;

    private int port;

    private boolean useSSL;

    private String username;

    private String password;

    private String downloadUrl;

    private OpenBISSessionHolder sessionHolder;

    private final DatastoreServiceDescriptions pluginTaskDescriptions;

    private final boolean archiverConfigured;

    private final IETLLIMSService service;

    public OpenBISAuthenticationInterceptor(SessionTokenManager sessionTokenManager,
            IETLLIMSService service, PluginTaskProviders pluginTaskParameters,
            OpenBISSessionHolder sessionHolder)
    {
        assert sessionTokenManager != null : "Unspecified session token manager.";
        assert service != null : "Given IETLLIMSService implementation can not be null.";
        assert pluginTaskParameters != null : "Unspecified plugin tasks";
        assert sessionHolder != null : "Unspecified session holder object.";

        this.sessionTokenManager = sessionTokenManager;
        this.service = service;
        this.pluginTaskDescriptions = pluginTaskParameters.getPluginTaskDescriptions();
        this.archiverConfigured =
                pluginTaskParameters.getArchiverTaskFactory().isArchiverConfigured();
        this.sessionHolder = sessionHolder;

    }

    /**
     * authenticate when the session has expired.
     */
    public synchronized Object invoke(MethodInvocation invocation) throws Throwable
    {
        checkSessionToken();
        try
        {
            return invocation.proceed();
        } catch (InvalidSessionException ise)
        {
            authenticate();
            return invocation.proceed();
        }
    }

    private final void authenticate()
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Authenticating to openBIS server as user '" + username + "'.");
        }
        SessionContextDTO sessionContextDTO = service.tryToAuthenticate(username, password);
        String sessionToken =
                sessionContextDTO == null ? null : sessionContextDTO.getSessionToken();
        if (sessionToken == null)
        {
            final String msg =
                    "Authentication failure to openBIS server. Most probable cause: user or password are invalid.";
            throw new ConfigurationFailureException(msg);
        }
        sessionHolder.setToken(sessionToken);
        DataStoreServerInfo dataStoreServerInfo = new DataStoreServerInfo();
        dataStoreServerInfo.setPort(port);
        dataStoreServerInfo.setUseSSL(useSSL);
        dataStoreServerInfo.setDataStoreCode(sessionHolder.getDataStoreCode());
        if (StringUtils.isBlank(downloadUrl))
        {
            final String msg =
                    "'" + DssPropertyParametersUtil.DOWNLOAD_URL_KEY + "' has to be set.";
            throw new ConfigurationFailureException(msg);
        }
        dataStoreServerInfo.setDownloadUrl(downloadUrl);
        dataStoreServerInfo.setSessionToken(sessionTokenManager.drawSessionToken());
        dataStoreServerInfo.setServicesDescriptions(pluginTaskDescriptions);
        dataStoreServerInfo.setArchiverConfigured(archiverConfigured);
        service.registerDataStoreServer(sessionToken, dataStoreServerInfo);
    }

    private final void checkSessionToken()
    {
        if (sessionHolder.getToken() == null)
        {
            authenticate();
        }
    }

    public final void setPort(int port)
    {
        this.port = port;
    }

    public final void setUseSSL(String useSSL)
    {
        // NOTE: 'use-ssl' property is optional. If it is not specified in DS properties file
        // String value passed by Spring is "${use-ssl}". Default value, which is 'true',
        // should be used in such case.
        boolean booleanValue = true;
        if (useSSL.equalsIgnoreCase("false"))
        {
            booleanValue = false;
        }
        this.useSSL = booleanValue;
    }

    public final void setUsername(String username)
    {
        this.username = username;
    }

    public final void setPassword(String password)
    {
        this.password = password;
    }

    public final void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }
}
