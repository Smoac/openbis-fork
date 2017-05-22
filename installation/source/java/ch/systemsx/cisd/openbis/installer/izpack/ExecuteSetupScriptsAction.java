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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Executes a script that configures the installation, copies key store (if specified) and inject passwords.
 * 
 * @author Kaloyan Enimanev
 * @author Franz-Josef Elmer
 */
public class ExecuteSetupScriptsAction extends AbstractScriptExecutor
{
    static final String DATA_SOURCES_KEY = "data-sources";

    static final String PATHINFO_DB_DATA_SOURCE = "path-info-db";

    static final String POST_REGISTRATION_TASKS_KEY = "post-registration.post-registration-tasks";

    static final String PATHINFO_DB_FEEDING_TASK = "pathinfo-feeding";

    static final String MAINTENANCE_PLUGINS_KEY = "maintenance-plugins";

    static final String PATHINFO_DB_DELETION_TASK = "path-info-deletion";

    static final String PROCESSING_PLUGINS_KEY = "processing-plugins";

    static final String PATHINFO_DB_CHECK = "path-info-db-consistency-check";

    private final String[] KEYS = { DATA_SOURCES_KEY, POST_REGISTRATION_TASKS_KEY, MAINTENANCE_PLUGINS_KEY, PROCESSING_PLUGINS_KEY };

    private final String[] TERMS = { PATHINFO_DB_DATA_SOURCE, PATHINFO_DB_FEEDING_TASK, PATHINFO_DB_DELETION_TASK, PATHINFO_DB_CHECK };

    private final String[] CLASS_POSTFIX = { "version-holder-class", "class", "class", "class" };

    /**
     * executed for first time installations.
     */
    private static final String POST_INSTALLATION_SCRIPT = "post-installation.sh";

    /**
     * executed for upgrade installations to restore backed up the configuration files.
     */
    private static final String RESTORE_CONFIG_FROM_BACKUP_SCRIPT = "restore-config-from-backup.sh";

    @Override
    public synchronized void executeAction(AutomatedInstallData data)
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
        String pathinfoDBEnabled =
                data.getVariable(GlobalInstallationContext.PATHINFO_DB_ENABLED);
        enablePathinfoDB("false".equalsIgnoreCase(pathinfoDBEnabled) == false, installDir);
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
                if (Utils.isASInstalled(installDir))
                {
                    File keystoreFileAS = Utils.getKeystoreFileForAS(installDir);
                    FileUtils.copyFile(keyStoreFile, keystoreFileAS);
                }
                File keystoreFileDSS = Utils.getKeystoreFileForDSS(installDir);
                FileUtils.copyFile(keyStoreFile, keystoreFileDSS);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    void enablePathinfoDB(boolean enableFlag, File installDir)
    {
        File dssServicePropertiesFile =
                new File(installDir, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        if (enableFlag)
        {
            for (int i = 0; i < KEYS.length; i++)
            {
                String key = KEYS[i];
                String newTerm = TERMS[i];
                String classProperty = newTerm + "." + CLASS_POSTFIX[i];
                int indexOfDot = key.indexOf('.');
                if (indexOfDot >= 0)
                {
                    classProperty = key.substring(0, indexOfDot) + "." + classProperty;
                }
                String className =
                        Utils.tryToGetProperties(dssServicePropertiesFile).getProperty(
                                classProperty);
                if (className != null)
                {
                    Utils.addTermToPropertyList(dssServicePropertiesFile, key, newTerm);
                }
            }
        } else
        {
            for (int i = 0; i < KEYS.length; i++)
            {
                Utils.removeTermFromPropertyList(dssServicePropertiesFile, KEYS[i], TERMS[i]);
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

        if (Utils.isASInstalled(installDir))
        {
            File jettySSLIniFile = new File(installDir, Utils.AS_PATH + Utils.JETTY_SSL_INI_PATH);
            if (jettySSLIniFile.exists() == false)
            {
                // ssl not configured
                return;
            }
            try
            {
                String jettySSLIni = FileUtils.readFileToString(jettySSLIniFile);
                jettySSLIni =
                        jettySSLIni.replaceAll("jetty\\.sslContext\\.keyStorePassword=.*", "jetty.sslContext.keyStorePassword=" + keyStorePassword)
                                .replaceAll("jetty\\.sslContext\\.keyManagerPassword=.*", "jetty.sslContext.keyManagerPassword=" + keyPassword)
                                .replaceAll("jetty\\.sslContext\\.trustStorePassword=.*", "jetty.sslContext.trustStorePassword=" + keyStorePassword);

                FileUtils.writeStringToFile(jettySSLIniFile, jettySSLIni);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private void executRestoreConfigScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, RESTORE_CONFIG_FROM_BACKUP_SCRIPT);
        String backupConfigFolder = data.getVariable(GlobalInstallationContext.BACKUP_FOLDER_VARNAME) + "/config-backup";
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
