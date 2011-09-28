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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationScriptRunner;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Kaloyan Enimanev
 */
public class CorePluginRegistry implements ICorePluginRegistry
{

    private final ICommonServerForInternalUse commonServer;

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    public CorePluginRegistry(ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    public void registerPlugins(ICorePluginScanner scanner)
    {
        for (CorePlugin plugin : scanner.scanForPlugins())
        {
            try
            {
                installPluginIfNeeded(scanner, plugin);
            } catch (Exception ex)
            {
                operationLog.error("Failed to install core plugin: " + plugin, ex);
            }
        }
    }

    private void installPluginIfNeeded(ICorePluginScanner scanner, CorePlugin plugin)
    {
        String sessionToken = getSessionToken();
        if (isNewPluginDetected(sessionToken, plugin))
        {
            commonServer.registerPlugin(sessionToken, plugin);
            runInitializeMasterDataScript(sessionToken, scanner, plugin);
            operationLog.info(plugin + " installed succesfully.");
        } else
        {
            operationLog.info("Deployed core plugin detected :" + plugin);
        }
    }

    private void runInitializeMasterDataScript(String sessionToken, ICorePluginScanner scanner,
            CorePlugin plugin)
    {
        File initializeMasterDataScript =
                scanner.tryGetFile(plugin, AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
        if (initializeMasterDataScript != null && initializeMasterDataScript.isFile())
        {
            operationLog.info("Executing master data initialization script "
                    + initializeMasterDataScript.getAbsolutePath());
            Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
            EncapsulatedCommonServer encapsulated =
                    EncapsulatedCommonServer.create(commonServer, sessionToken);
            MasterDataRegistrationScriptRunner scriptRunner =
                    new MasterDataRegistrationScriptRunner(encapsulated, logger);

            // TODO KE: this has to throw exception, but currently does not
            scriptRunner.executeScript(initializeMasterDataScript);
        } else
        {
            operationLog.info(String.format("No '%s' script found for '%s'. Skipping..",
                    AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT, plugin));
        }
    }

    private boolean isNewPluginDetected(String sessionToken, CorePlugin plugin)
    {
        List<CorePlugin> installedVersions =
                commonServer.listCorePluginsByName(sessionToken, plugin.getName());
        if (installedVersions.isEmpty())
        {
            return true;
        }
        CorePlugin latestVersionInstalled = Collections.max(installedVersions);
        return latestVersionInstalled.getVersion() < plugin.getVersion();
    }

    private String getSessionToken()
    {
        SessionContextDTO sessionDTO = commonServer.tryToAuthenticateAsSystem();
        final String sessionToken = sessionDTO.getSessionToken();
        return sessionToken;
    }

}
