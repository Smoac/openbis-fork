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

package ch.systemsx.cisd.openbis.generic.server.dbbackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * Given a list of properties files generates a list of well-known databases to be backed up as part
 * of the openBIS upgrade process.
 * 
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseDescriptionGenerator
{
    public static final String AS_DB_KEY_PREFIX = "database.";

    public static final String PROTEOMICS_DB_KEY_PREFIX = "phosphonetx.database.";

    public static final String HCS_IMAGING_DB_VERSION_HOLDER =
            "ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseVersionHolder";

    private static final String AS_BASIC_DB_NAME = "openbis";

    private static final String PROTEOMICS_BASIC_DB_NAME = "phosphonetx";

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
            File propertiesFile = new File(fileName);
            if (propertiesFile.isFile() && propertiesFile.canRead())
            {
                FileInputStream fin = null;
                try
                {
                    fin = new FileInputStream(propertiesFile);
                    Properties properties = new Properties();
                    properties.load(fin);
                    process(properties);
                } catch (IOException ioex)
                {
                    System.err.println("I/O error while reading from file " + fileName);
                    System.exit(2);
                } finally
                {
                    closeQuietly(fin);
                }
            } else
            {
                System.err.println("Cannot read from specified file " + fileName);
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

        generator.process(args);

        String generatorResult = generator.getResult();
        System.out.println(generatorResult);
    }

}
