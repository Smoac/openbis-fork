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
import ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginScanner.ScannerType;

/**
 * @author Kaloyan Enimanev
 */
public class CorePluginRegistrator implements InitializingBean
{
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private ICorePluginRegistry corePluginRegistry;

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
            ICorePluginScanner pluginScanner =
                    new CorePluginScanner(pluginsFolderName, ScannerType.AS);
            corePluginRegistry.registerPlugins(pluginScanner);
        }
    }

    public void afterPropertiesSet() throws Exception
    {
        registerPlugins();
    }

    public void setCorePluginRegistry(ICorePluginRegistry corePluginRegistry)
    {
        this.corePluginRegistry = corePluginRegistry;
    }

    public void setPluginsFolderName(String pluginsFolderName)
    {
        this.pluginsFolderName = pluginsFolderName;
    }

}
