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

package ch.systemsx.cisd.openbis.installer.izpack;

import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.ADMIN_PASSWORD_VARNAME;
import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.ETL_SERVER_PASSWORD_VARNAME;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Executes a script that configures the installation, copies key store (if specified) and inject
 * passwords.
 * 
 * @author Kaloyan Enimanev
 * @author Franz-Josef Elmer
 */
public class ExecuteSetupScriptsAction extends AbstractScriptExecutor implements PanelAction
{
    /**
     * executed for first time installations.
     */
    private static final String POST_INSTALLATION_SCRIPT = "post-installation.sh";

    /**
     * executed for upgrade installations to restore backed up the configuration files.
     */
    private static final String RESTORE_CONFIG_FROM_BACKUP_SCRIPT = "restore-config-from-backup.sh";

    @Override
    public synchronized void executeAction(AutomatedInstallData data, AbstractUIHandler arg1)
    {
        if (GlobalInstallationContext.isFirstTimeInstallation)
        {
            executePostInstallationScript(data);
        } else
        {
            executRestoreConfigScript(data);
        }
        String keyStoreFileName =
                data.getVariable(GlobalInstallationContext.KEY_STORE_FILE_VARNAME);
        String keyStorePassword =
                data.getVariable(GlobalInstallationContext.KEY_STORE_PASSWORD_VARNAME);
        String certificatePassword =
                data.getVariable(GlobalInstallationContext.KEY_PASSWORD_VARNAME);
        File installDir = GlobalInstallationContext.installDir;
        installKeyStore(keyStoreFileName, installDir);
        injectPasswords(keyStorePassword, certificatePassword, installDir);
    }

    void installKeyStore(String keyStoreFileName, File installDir)
    {
        if (keyStoreFileName != null && keyStoreFileName.length() > 0)
        {
            try
            {
                File keyStoreFile = new File(keyStoreFileName);
                File keystoreFileAS = Utils.getKeystoreFileForAS(installDir);
                FileUtils.copyFile(keyStoreFile, keystoreFileAS);
                File keystoreFileDSS = Utils.getKeystoreFileForDSS(installDir);
                FileUtils.copyFile(keyStoreFile, keystoreFileDSS);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    void injectPasswords(String keyStorePassword, String keyPassword, File installDir)
    {
        File dssServicePropertiesFile =
                new File(installDir, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        Utils.updateOrAppendProperty(dssServicePropertiesFile, Utils.DSS_KEYSTORE_PASSWORD_KEY,
                keyStorePassword);
        Utils.updateOrAppendProperty(dssServicePropertiesFile, Utils.DSS_KEYSTORE_KEY_PASSWORD_KEY,
                keyPassword);
        
        File jettyXMLFile = new File(installDir, Utils.AS_PATH + Utils.JETTY_XML_PATH);
        try
        {
            String jettyXML = FileUtils.readFileToString(jettyXMLFile);
            jettyXML =
                    jettyXML.replaceAll("<Set name=\"Password\">.*</Set>",
                            "<Set name=\"Password\"><![CDATA[" + keyStorePassword + "]]></Set>")
                            .replaceAll(
                                    "<Set name=\"KeyPassword\">.*</Set>",
                                    "<Set name=\"KeyPassword\"><![CDATA[" + keyPassword
                                            + "]]></Set>");
            FileUtils.writeStringToFile(jettyXMLFile, jettyXML);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    private void executRestoreConfigScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, RESTORE_CONFIG_FROM_BACKUP_SCRIPT);
        String backupConfigFolder =
                data.getVariable(GlobalInstallationContext.BACKUP_FOLDER_VARNAME)
                        + "/config-backup";
        executeAdminScript(null, script, backupConfigFolder);
    }

    private void executePostInstallationScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, POST_INSTALLATION_SCRIPT);
        Map<String, String> customEnvironment = new HashMap<String, String>();
        customEnvironment.put(ADMIN_PASSWORD_VARNAME, data.getVariable(ADMIN_PASSWORD_VARNAME));
        customEnvironment.put(ETL_SERVER_PASSWORD_VARNAME,
                data.getVariable(ETL_SERVER_PASSWORD_VARNAME));
        executeAdminScript(customEnvironment, script);
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }
    



}
