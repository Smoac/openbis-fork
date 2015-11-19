/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * Constants and static method about BLAST support.
 *
 * @author Franz-Josef Elmer
 */
public class BlastUtils
{
    private static final String BLAST_ROOT = "eln-lims/bin/blast";

    private static final String LINUX = "linux/bin/";

    private static final String MAC = "mac/bin/";

    public static final String BLAST_TOOLS_DIRECTORY_PROPERTY = "blast-tools-directory";

    public static final String BLAST_DATABASES_FOLDER_PROPERTY = "blast-databases-folder";

    public static final String DEFAULT_BLAST_DATABASES_FOLDER = "blast-databases";

    public static File getBlastDatabaseFolder(Properties properties, File storeRoot)
    {
        return getFile(properties, BLAST_DATABASES_FOLDER_PROPERTY, DEFAULT_BLAST_DATABASES_FOLDER, storeRoot);
    }

    public static String getBLASTToolDirectory(Properties properties)
    {
        String blastToolsDirectory = properties.getProperty(BLAST_TOOLS_DIRECTORY_PROPERTY, "");

        if (false == blastToolsDirectory.isEmpty())
        {

            if (blastToolsDirectory.endsWith(File.separator))
            {
                return blastToolsDirectory;
            }
            return blastToolsDirectory + File.separator;
        }

        return getBestGuessBLASTToolDirectory(properties);
    }

    private static String getBestGuessBLASTToolDirectory(Properties properties)
    {
        String corePluginsFolder = CorePluginsUtils.getCorePluginsFolder(properties, ScannerType.DSS);
        Path path = Paths.get(corePluginsFolder, BLAST_ROOT).toAbsolutePath();

        if (false == path.toFile().exists())
        {
            return "";
        }

        if (SystemUtils.IS_OS_LINUX)
        {
            return path.resolve(LINUX).toString() + File.separator;
        }

        return path.resolve(MAC).toString() + File.separator;
    }

    public static File getFile(Properties properties, String pathProperty, String defaultPath, File storeRoot)
    {
        String path = properties.getProperty(pathProperty);
        return path == null ? new File(storeRoot, defaultPath) : new File(path);
    }

    public static void logMissingTools(Logger operationLog)
    {
        operationLog.error("BLAST isn't installed or property '" + BLAST_TOOLS_DIRECTORY_PROPERTY
                + "' hasn't been correctly specified.");
    }

}
