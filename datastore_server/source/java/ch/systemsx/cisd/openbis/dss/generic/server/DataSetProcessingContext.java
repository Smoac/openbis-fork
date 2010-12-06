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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Map;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;

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

    /**
     * Creates an instance for specified parameter bindings, e-mail client, and user e-mail address.
     */
    public DataSetProcessingContext(Map<String, String> parameterBindings, IMailClient mailClient,
            String userEmailOrNull)
    {
        this.parameterBindings = parameterBindings;
        this.mailClient = mailClient;
        this.userEmailOrNull = userEmailOrNull;
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
}
