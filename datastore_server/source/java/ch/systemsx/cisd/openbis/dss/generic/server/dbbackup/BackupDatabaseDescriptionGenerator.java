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

package ch.systemsx.cisd.openbis.dss.generic.server.dbbackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner;

/**
 * Given a list of properties files generates a list of well-known databases to be backed up as part
 * of the openBIS upgrade process.
 * 
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseDescriptionGenerator
{
    public static final String AS_DB_KEY_PREFIX = "database.";

    public static final String PROTEOMICS_DB_KEY_PREFIX = "proteomics.database.";

    public static final String HCS_IMAGING_DB_VERSION_HOLDER =
            "ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseVersionHolder";

    private static final String AS_BASIC_DB_NAME = "openbis";

    private static final String PROTEOMICS_BASIC_DB_NAME = "proteomics";

    private StringBuilder result = new StringBuilder();

    private void process(Properties properties)
    {
        String openBisDatabase =
                BackupDatabaseParser.getAppServerDatabaseDescription(properties, AS_DB_KEY_PREFIX,
                        AS_BASIC_DB_NAME);
        addIfFound(openBisDatabase);

        String proteomicDatabase =
                BackupDatabaseParser.getAppServerDatabaseDescription(properties,
                        PROTEOMICS_DB_KEY_PREFIX, PROTEOMICS_BASIC_DB_NAME);
        addIfFound(proteomicDatabase);

        String hcsImagingDatabase =
                BackupDatabaseParser.getDssServerDatabaseDescription(properties,
                        HCS_IMAGING_DB_VERSION_HOLDER);
        addIfFound(hcsImagingDatabase);
    }

    private void addIfFound(String databaseDescription)
    {
        if (false == StringUtils.isEmpty(databaseDescription))
        {
            if (result.length() > 0)
            {
                result.append("\n");
            }
            result.append(databaseDescription);
        }
    }

    void process(String[] fileNames)
    {
        for (String fileName : fileNames)
        {
            File file = new File(fileName);
            if (file.isDirectory())
            {
                processCorePluginsFolder(file);
            } else if (file.isFile() && file.canRead())
            {
                Properties properties = readProperties(file);
                process(properties);
            } else
            {
                System.err.println("Cannot read from specified file " + fileName);
            }
        }
    }

    private Properties readProperties(File file)
    {
        Properties properties = new Properties();
        FileInputStream fin = null;
        try
        {
            fin = new FileInputStream(file);
            properties.load(fin);
        } catch (IOException ioex)
        {
            throw new IOExceptionUnchecked(ioex);
        } finally
        {
            closeQuietly(fin);
        }
        return properties;
    }

    private void processCorePluginsFolder(File folder)
    {
        List<CorePlugin> plugins =
                new CorePluginScanner(folder.getAbsolutePath(), CorePluginScanner.ScannerType.DSS)
                        .scanForPlugins();
        for (CorePlugin plugin : plugins)
        {
            File dataSourcePlugins =
                    new File(folder, plugin.getName() + "/" + plugin.getVersion()
                            + "/dss/data-sources");
            File[] pluginFolders = dataSourcePlugins.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.startsWith(".") == false;
                    }
                });
            for (File pluginDefinitionFolder : pluginFolders)
            {
                Properties pluginProperties =
                        readProperties(new File(pluginDefinitionFolder, "plugin.properties"));
            }
        }
    }

    String getResult()
    {
        return result.toString();
    }

    private void closeQuietly(FileInputStream fin)
    {
        try
        {
            if (fin != null)
            {
                fin.close();
            }
        } catch (Throwable t)
        {
            // it is safe to ignore any errors here
        }
    }

    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
        {
            System.err.println("Please specify a list of properties files to be parsed.");
            System.exit(1);
        }
        BackupDatabaseDescriptionGenerator generator = new BackupDatabaseDescriptionGenerator();

        try
        {
            generator.process(args);
        } catch (IOExceptionUnchecked e)
        {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        String generatorResult = generator.getResult();
        System.out.println(generatorResult);
    }

}
