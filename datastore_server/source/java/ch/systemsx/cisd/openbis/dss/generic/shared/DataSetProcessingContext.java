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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Map;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISessionWorkspaceProvider;

/**
 * Context for processing data sets by a {@link IProcessingPluginTask}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetProcessingContext
{
    private final Map<String, String> parameterBindings;

    private final IMailClient mailClient;

    private final String userEmailOrNull;

    private final IDataSetDirectoryProvider directoryProvider;

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    private final String sessionTokenOrNull;

    private final ISessionWorkspaceProvider sessionWorkspaceProviderOrNull;

    /**
     * Creates an instance for specified directory provider, parameter bindings, e-mail client, and
     * optional user e-mail address and sessionToken.
     */
    public DataSetProcessingContext(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider, Map<String, String> parameterBindings,
            IMailClient mailClient, String userEmailOrNull)
    {
        this(contentProvider, directoryProvider, parameterBindings, mailClient, userEmailOrNull,
                null);
    }

    /**
     * Creates an instance for specified directory provider, parameter bindings, e-mail client, and
     * optional user e-mail address and sessionToken.
     */
    public DataSetProcessingContext(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider, Map<String, String> parameterBindings,
            IMailClient mailClient, String userEmailOrNull, String sessionTokenOrNull)
    {
        this(contentProvider, directoryProvider, null, parameterBindings, mailClient,
                userEmailOrNull, sessionTokenOrNull);
    }

    /**
     * Creates an instance for specified directory provider, workspace provider, parameter bindings,
     * e-mail client, and optional user e-mail address and sessionToken.
     */
    public DataSetProcessingContext(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider,
            ISessionWorkspaceProvider sessionWorkspaceProviderOrNull,
            Map<String, String> parameterBindings,
            IMailClient mailClient, String userEmailOrNull, String sessionTokenOrNull)
    {
        this.hierarchicalContentProvider = contentProvider;
        this.directoryProvider = directoryProvider;
        this.parameterBindings = parameterBindings;
        this.mailClient = mailClient;
        this.userEmailOrNull = userEmailOrNull;
        this.sessionTokenOrNull = sessionTokenOrNull;
        this.sessionWorkspaceProviderOrNull = sessionWorkspaceProviderOrNull;
    }

    public IDataSetDirectoryProvider getDirectoryProvider()
    {
        return directoryProvider;
    }

    /**
     * Returns the session workspace provider of this context, if available and <code>null</code> if
     * this context has no session workspace provider.
     */
    public ISessionWorkspaceProvider tryGetSessionWorkspaceProvider()
    {
        return sessionWorkspaceProviderOrNull;
    }

    public final Map<String, String> getParameterBindings()
    {
        return parameterBindings;
    }

    public final IMailClient getMailClient()
    {
        return mailClient;
    }

    public final String getUserEmailOrNull()
    {
        return userEmailOrNull;
    }

    public String trySessionToken()
    {
        return sessionTokenOrNull;
    }

    public IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return hierarchicalContentProvider;
    }
}
