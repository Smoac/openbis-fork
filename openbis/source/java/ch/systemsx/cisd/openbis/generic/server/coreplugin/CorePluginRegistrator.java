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

package ch.systemsx.cisd.openbis.generic.server.coreplugin;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Kaloyan Enimanev
 */
public class CorePluginRegistrator implements InitializingBean
{
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private ICommonServerForInternalUse commonServer;

    private String pluginsFolderName;

    /**
     * Loads and installs the deployed core plugins. Invoked from the Spring container after the
     * object is initialized.
     */
    public void registerPlugins()
    {
        if ("${core-plugins-folder}".equals(pluginsFolderName))
        {
            operationLog.info("No 'core-plugins-folder' configuration specified. "
                    + "Skipping registration of core plugins... ");
        } else
        {
            CorePluginScanner pluginScanner =
                    new CorePluginScanner(pluginsFolderName, ScannerType.AS);
            String sessionToken = getSessionToken();
            for (CorePlugin plugin : pluginScanner.scanForPlugins())
            {
                try
                {
                    commonServer.registerPlugin(sessionToken, plugin, pluginScanner);
                } catch (Exception ex)
                {
                    operationLog.error("Failed to install core plugin: " + plugin, ex);
                }
            }
        }
    }

    public void afterPropertiesSet() throws Exception
    {
        registerPlugins();
    }

    public void setPluginsFolderName(String pluginsFolderName)
    {
        this.pluginsFolderName = pluginsFolderName;
    }

    public void setCommonServer(ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    private String getSessionToken()
    {
        SessionContextDTO sessionDTO = commonServer.tryToAuthenticateAsSystem();
        final String sessionToken = sessionDTO.getSessionToken();
        return sessionToken;
    }
}
