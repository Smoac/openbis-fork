/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class SetDatabasesToBackupAction extends AbstractScriptExecutor implements PanelAction
{
    private static final String HELPER_CLASS =
            "ch.systemsx.cisd.openbis.dss.generic.server.dbbackup.BackupDatabaseDescriptionGenerator";

    static final String DATABASES_TO_BACKUP_VARNAME = "DATABASES_TO_BACKUP";

    @Override
    public void initialize(PanelActionConfiguration configuration)
    {

    }

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        try
        {
            String descriptions = extractDescriptions().trim();
            String databases = extractDatabases(data, descriptions);
            data.setVariable(DATABASES_TO_BACKUP_VARNAME, databases);
            return;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            handler.emitError("Exception", ex.toString());
        }
    }

    private String extractDatabases(AutomatedInstallData data, String descriptions)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        if (descriptions.length() > 0)
        {
            for (String description : descriptions.split("\n"))
            {
                String[] splitted = description.split(";")[0].split("=");
                if (splitted.length < 2)
                {
                    throw new IllegalArgumentException("Invalid database description: "
                            + description);
                }
                String database = splitted[1].trim();
                if (databaseExists(data, database))
                {
                    builder.append(database);
                }
            }
        }
        return builder.toString();
    }

    private boolean databaseExists(AutomatedInstallData data, String database)
    {
        File scriptFile = getAdminScriptFile(data, "database-existence-check.sh");
        if (scriptFile.exists() == false)
        {
            return true;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        executeAdminScript(null, outputStream, outputStream, scriptFile.getAbsolutePath(), database);
        return outputStream.toString().trim().equals("FALSE") == false;

    }

    private String extractDescriptions() throws Exception
    {
        Object[] arguments = createArguments();
        File dssLibFolder = new File(GlobalInstallationContext.installDir, Utils.DSS_PATH + "lib");
        Class<?> clazz = new JarClassLoader(dssLibFolder).loadClass(HELPER_CLASS);
        Method method = clazz.getMethod("getDescriptions", String[].class);
        return (String) method.invoke(null, arguments);
    }

    private Object[] createArguments()
    {
        List<String> paths = new ArrayList<String>();
        if (Utils.isASInstalled(GlobalInstallationContext.installDir))
        {
            paths.add(new File(GlobalInstallationContext.installDir, Utils.AS_PATH
                    + Utils.SERVICE_PROPERTIES_PATH).getAbsolutePath());
        }
        paths.add(new File(GlobalInstallationContext.installDir, Utils.DSS_PATH
                + Utils.SERVICE_PROPERTIES_PATH).getAbsolutePath());
        System.out.println("Scan following properties file for data source definitions: " + paths);
        return new Object[]
            { paths.toArray(new String[0]) };
    }
}
