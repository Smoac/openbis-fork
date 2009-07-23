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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Implementation of {@link ISqlScriptProvider} based on files in classpath or working directory.
 * This provider tries first to load a resource. If this isn't successful the provider tries to look
 * for files relative to the working directory.
 * 
 * @author Franz-Josef Elmer
 */
public class SqlScriptProvider implements ISqlScriptProvider
{
    private static final String DUMP_FILENAME = ".DUMP";

    private static final String SQL_FILE_TYPE = ".sql";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SqlScriptProvider.class);

    private final String genericScriptFolder;

    private final String specificScriptFolder;

    /**
     * Creates an instance for the specified script folders. They are either resource folders or
     * folders relative to the working directory.
     * 
     * @param schemaScriptRootFolder Root folder of schema, migration and data scripts.
     * @param databaseEngineCode The code of the database engine. Used to find the db engine
     *            specific schema script folder.
     */
    public SqlScriptProvider(final String schemaScriptRootFolder, final String databaseEngineCode)
    {
        this.genericScriptFolder = schemaScriptRootFolder + "/generic";
        this.specificScriptFolder = schemaScriptRootFolder + "/" + databaseEngineCode;
    }

    /**
     * Returns <code>true</code> if a &lt;finish script&gt; is found and <code>false</code>
     * otherwise.
     */
    public boolean isDumpRestore(final String version)
    {
        return getDumprestoreFile(version).exists();
    }

    public void markAsDumpRestorable(final String version)
    {
        try
        {
            FileUtils.touch(getDumprestoreFile(version));
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private File getDumprestoreFile(final String version)
    {
        return new File(getDumpFolder(version), DUMP_FILENAME);
    }

    /**
     * Returns the folder where all dump files for <var>version</var> reside.
     */
    public File getDumpFolder(final String version)
    {
        return new File(specificScriptFolder, version);
    }

    /**
     * Returns the schema script for the specified version. The name of the script is expected to be
     * 
     * <pre>
     * &lt;schema script folder&gt;/&lt;version&gt;/schema-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetSchemaScript(final String version)
    {
        return tryLoadScript("schema-" + version + SQL_FILE_TYPE, version);
    }

    /**
     * Returns the script containing all functions for the specified version. The name of the script
     * is expected to be
     * 
     * <pre>
     * &lt;data script folder&gt;/&lt;version&gt;/function-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetFunctionScript(final String version)
    {
        return tryLoadScript("function-" + version + SQL_FILE_TYPE, version);
    }

    /**
     * Returns the script containing all domain definitions for the specified version. The name of
     * the script is expected to be
     * 
     * <pre>
     * &lt;schema script folder&gt;/&lt;version&gt;/domains-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetDomainsScript(final String version)
    {
        return tryLoadScript("domains-" + version + SQL_FILE_TYPE, version);
    }

    /**
     * Returns the script containing all grant declarations for the specified version. The name of
     * the script is expected to be
     * 
     * <pre>
     * &lt;schema script folder&gt;/&lt;version&gt;/grants-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetGrantsScript(final String version)
    {
        return tryLoadScript("grants-" + version + SQL_FILE_TYPE, version);
    }

    /**
     * Returns the data script for the specified version. The name of the script is expected to be
     * 
     * <pre>
     * &lt;data script folder&gt;/&lt;version&gt;/data-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetDataScript(final String version)
    {
        return tryLoadScript("data-" + version + SQL_FILE_TYPE, version);
    }

    /**
     * Returns the migration script for the specified versions. The name of the script is expected
     * to be
     * 
     * <pre>
     * &lt;schema script folder&gt;/migration/migration-&lt;fromVersion&gt;-&lt;toVersion&gt;.sql
     * </pre>
     */
    public Script tryGetMigrationScript(final String fromVersion, final String toVersion)
    {
        final String scriptName = "migration-" + fromVersion + "-" + toVersion + SQL_FILE_TYPE;
        return tryLoadScript(scriptName, toVersion, "migration");
    }

    private Script tryLoadScript(final String scriptName, final String scriptVersion)
    {
        return tryLoadScript(scriptName, scriptVersion, scriptVersion);
    }

    private Script tryLoadScript(final String scriptName, final String scriptVersion,
            final String prefix)
    {
        Script script =
                tryPrimLoadScript(specificScriptFolder + "/" + prefix, scriptName, scriptVersion);
        if (script == null)
        {
            script =
                    tryPrimLoadScript(genericScriptFolder + "/" + prefix, scriptName, scriptVersion);
        }
        return script;
    }

    private Script tryPrimLoadScript(final String scriptFolder, final String scriptName,
            final String scriptVersion)
    {
        final String scriptPath = scriptFolder + "/" + scriptName;
        final String resource = "/" + scriptPath;
        String script = FileUtilities.loadToString(getClass(), resource);
        if (script == null)
        {
            final File file = new File(scriptFolder, scriptName);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Resource '" + resource + "' could not be found. Trying '"
                        + file.getPath() + "'.");
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
        return new Script(scriptPath, script, scriptVersion);
    }

}
