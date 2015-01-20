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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
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
    public void executeAction(AutomatedInstallData data)
    {
        try
        {
            String descriptions = extractDescriptions().trim();
            String databases = extractDatabases(data, descriptions);
            data.setVariable(DATABASES_TO_BACKUP_VARNAME, databases);
        } catch (Exception ex)
        {
            throw new RuntimeException("Databse description extraction failed.", ex);
        }
    }

    private String extractDatabases(AutomatedInstallData data, String descriptions)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        if (descriptions.length() > 0)
        {
            for (String description : descriptions.split("\n"))
            {
                DatabaseDescription databaseDescription = new DatabaseDescription(description);
                if (databaseExists(data, databaseDescription))
                {
                    builder.append(databaseDescription.getDatabase());
                }
            }
        }
        return builder.toString();
    }
    
    private boolean databaseExists(AutomatedInstallData data, DatabaseDescription databaseDescription)
    {
        File scriptFile = getAdminScriptFile(data, "database-existence-check.sh");
        if (scriptFile.exists() == false)
        {
            return true;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, String> env = new HashMap<String, String>();
        env.put("PGPASSWORD", databaseDescription.getPassword());
        String database = databaseDescription.getDatabase();
        String owner = databaseDescription.getUsername();
        String host = databaseDescription.getHost();
        String port = databaseDescription.getPort();
        executeAdminScript(env, outputStream, outputStream, scriptFile.getAbsolutePath(), database, owner, host, port);
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
    
    private static final class DatabaseDescription
    {
        private final String description;
        private final String database;
        private final String username;
        private final String password;
        private final String host;

        DatabaseDescription(String description)
        {
            this.description = description;
            String[] parts = description.split(";");
            if (parts.length < 3)
            {
                throw new IllegalArgumentException("Only " + parts.length 
                        + " parts separated by ';' in database description: " + description);
            }
            database = getValue(parts[0]);
            username = getValue(parts[1]);
            password = getValue(parts[2]);
            host = parts.length == 4 ? getValue(parts[3]) : "localhost";
        }
        
        private String getValue(String part)
        {
            String[] splitted = part.split("=");
            return splitted.length == 1 ? "" : splitted[1].trim();
        }

        public String getDatabase()
        {
            return database;
        }

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }
        
        public String getHost()
        {
            int indexOfColon = host.indexOf(':');
            return indexOfColon < 0 ? host : host.substring(0, indexOfColon);
        }
        
        public String getPort()
        {
            int indexOfColon = host.indexOf(':');
            return indexOfColon < 0 ? "5432" : host.substring(indexOfColon + 1);
        }

        @Override
        public String toString()
        {
            return description;
        }
        
    }

}
