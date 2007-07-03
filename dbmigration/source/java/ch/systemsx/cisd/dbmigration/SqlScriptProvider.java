/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Implementation of {@link ISqlScriptProvider} based on files in classpath or working directory. This provider tries
 * first to load a resource. If this isn't successful the provider tries to look for files relative to the working
 * directory.
 * 
 * @author Franz-Josef Elmer
 */
public class SqlScriptProvider implements ISqlScriptProvider
{
    private static final String SQL_FILE_TYPE = ".sql";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SqlScriptProvider.class);

    private final String schemaScriptFolder;

    private final String dataScriptFolder;

    private final String massUploadDataFolder;

    private final String internalScriptFolder;

    /**
     * Creates an instance for the specified folders and database type. The database type specifies the resource folder
     * relative to the package of this class where the scripts with method {@link #getScript(String)} are loaded.
     */
    public static ISqlScriptProvider create(String schemaScriptFolder, String dataScriptFolder,
            String massUploadDataFolder, String databaseType)
    {
        String internalFolder = SqlScriptProvider.class.getPackage().getName().replace('.', '/') + "/" + databaseType;
        return new SqlScriptProvider(schemaScriptFolder, dataScriptFolder, massUploadDataFolder, internalFolder);
    }

    /**
     * Creates an instance for the specified script folders. They are either resource folders or folders relative to the
     * working directory.
     * 
     * @param schemaScriptFolder Folder of schema and migration scripts.
     * @param dataScriptFolder Folder of data scripts.
     * @param internalScriptFolder Folder for internal scripts.
     */
    SqlScriptProvider(String schemaScriptFolder, String dataScriptFolder, String massUploadDataFolder,
            String internalScriptFolder)
    {
        this.schemaScriptFolder = schemaScriptFolder;
        this.dataScriptFolder = dataScriptFolder;
        this.massUploadDataFolder = massUploadDataFolder;
        this.internalScriptFolder = internalScriptFolder;
    }

    /**
     * Returns the data script for the specified version. The name of the script is expected to be
     * 
     * <pre>
     * &lt;data script folder&gt;/&lt;version&gt;/data-&lt;version&gt;.sql
     * </pre>
     */
    public Script getDataScript(String version)
    {
        return loadScript(dataScriptFolder + "/" + version, "data-" + version + SQL_FILE_TYPE);
    }

    /**
     * Returns the migration script for the specified versions. The name of the script is expected to be
     * 
     * <pre>
     * &lt;schema script folder&gt;/migration/migration-&lt;fromVersion&gt;-&lt;toVersion&gt;.sql
     * </pre>
     */
    public Script getMigrationScript(String fromVersion, String toVersion)
    {
        String scriptName = "migration-" + fromVersion + "-" + toVersion + SQL_FILE_TYPE;
        return loadScript(schemaScriptFolder + "/migration", scriptName);
    }

    /**
     * Returns the schema script for the specified version. The name of the script is expected to be
     * 
     * <pre>
     * &lt;schema script folder&gt;/&lt;version&gt;/schema-&lt;version&gt;.sql
     * </pre>
     */
    public Script getSchemaScript(String version)
    {
        return loadScript(schemaScriptFolder + "/" + version, "schema-" + version + SQL_FILE_TYPE);
    }

    /**
     * Returns the specified script relative to the internal script folder.
     */
    public Script getScript(String scriptName)
    {
        return loadScript(internalScriptFolder, scriptName);
    }

    private Script loadScript(String folder, String scriptName)
    {
        String fullScriptName = folder + "/" + scriptName;
        String resource = "/" + fullScriptName;
        String script = FileUtilities.loadToString(getClass(), resource);
        if (script == null)
        {
            File file = new File(folder, scriptName);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Resource '" + resource + "' could not be found. Trying '" + file.getPath() + "'.");
            }
            if (file.exists() == false)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("File '" + file.getPath() + "' does not exist.");
                }
                return null;
            }
            script = FileUtilities.loadToString(file);
        }
        return new Script(fullScriptName, script);
    }

    /**
     * Returns the files determined for mass uploading.
     */
    public File[] getMassUploadFiles(String version)
    {
        final File dataFolder = new File(massUploadDataFolder + "/" + version);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Searching for mass upload files in directory '" + dataFolder.getAbsolutePath() + "'.");
        }
        String[] csvFiles = dataFolder.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".csv");
                }
            });
        if (csvFiles == null)
        {
            operationLog.warn("Path '" + dataFolder.getAbsolutePath() + "' is not a directory.");
            return new File[0];
        }
        Arrays.sort(csvFiles);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Found " + csvFiles.length + " files for mass uploading.");
        }
        final File[] csvPaths = new File[csvFiles.length];
        for (int i = 0; i < csvFiles.length; ++i)
        {
            csvPaths[i] = new File(dataFolder, csvFiles[i]);
        }
        return csvPaths;
    }

}
