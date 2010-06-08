/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.bdsmigration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor;

/**
 * Maintenance task which migrates all the BDS datasets to the imaging database.
 * 
 * @author Tomasz Pylak
 */
public class BDSMigrationMaintananceTask implements IMaintenanceTask
{
    static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BDSMigrationMaintananceTask.class);

    static final String ANNOTATIONS_DIR = "annotations";

    static final String METADATA_DIR = "metadata";

    static final String DATA_DIR = "data";

    static final String VERSION_DIR = "version";

    static final String ORIGINAL_DIR = "original";

    private static final String STORE_ROOT_PROPERTY = "storeRoot";

    static final String DIR_SEP = "/";

    private File storeRoot;

    private String[] channelNames;

    private Properties properties;

    public void setUp(String pluginName, Properties properties)
    {
        String storeRootPath = properties.getProperty(STORE_ROOT_PROPERTY);
        if (storeRootPath == null)
        {
            throw new EnvironmentFailureException(STORE_ROOT_PROPERTY + " property not specified.");
        }
        this.storeRoot = new File(storeRootPath);
        if (storeRoot.isDirectory() == false)
        {
            throw new EnvironmentFailureException(storeRoot
                    + " does not exist or is not a directory.");
        }
        this.channelNames = PlateStorageProcessor.extractChannelNames(properties);
        this.properties = properties;
    }

    public void execute()
    {
        IBDSMigrator imagingDbUploader =
                BDSImagingDbUploader.createImagingDbUploaderMigrator(properties, channelNames);
        IBDSMigrator[] migrators =
                new IBDSMigrator[]
                    { imagingDbUploader, new OriginalDataRelocatorMigrator(),
                            new BDSDataRemoverMigrator() };
        for (IBDSMigrator migrator : migrators)
        {
            boolean ok = migrateStore(migrator);
            if (ok == false)
            {
                operationLog.error("Migration stopped at: " + migrator.getDescription());
                return;
            }
        }
    }

    private boolean migrateStore(IBDSMigrator migrator)
    {
        File[] files = storeRoot.listFiles();
        for (File file : files)
        {
            String name = file.getName();
            if (name.equals("error") == false && name.equals("unidentified") == false)
            {
                boolean ok = migrateDatabaseInstance(file, migrator);
                if (ok == false)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean migrateDatabaseInstance(File dbInstanceDir, IBDSMigrator migrator)
    {
        int successCounter = 0, failureCounter = 0;
        for (File l1 : dbInstanceDir.listFiles())
        {
            for (File l2 : l1.listFiles())
            {
                for (File l3 : l2.listFiles())
                {
                    for (File dataset : l3.listFiles())
                    {
                        boolean ok = migrateDataset(dataset, migrator);
                        if (ok)
                        {
                            successCounter++;
                        } else
                        {
                            failureCounter++;
                        }
                    }
                }
            }
        }
        logMigrationStats(migrator, successCounter, failureCounter);
        return failureCounter == 0;
    }

    private void logMigrationStats(IBDSMigrator migrator, int successCounter, int failureCounter)
    {
        String desc = migrator.getDescription();
        operationLog.info("Successful migration step '" + desc + "' of " + successCounter
                + " datasets.");
        operationLog.info("Unuccessful migration step '" + desc + "' of " + failureCounter
                + " datasets.");
    }

    private boolean migrateDataset(File dataset, IBDSMigrator migrator)
    {
        if (isBDS(dataset))
        {
            boolean ok = migrator.migrate(dataset);
            logMigrationFinished(ok, dataset, migrator.getDescription());
            return ok;
        } else
        {
            return true;
        }
    }

    private static void logMigrationFinished(boolean ok, File dataset, String stepDescription)
    {
        String msg = "Migration step '" + stepDescription + "' of the dataset '" + dataset + "' ";
        if (ok)
        {
            operationLog.info(msg + "succeeded.");
        } else
        {
            operationLog.info(msg + "failed.");
        }
    }

    static int asNum(String standardPathToken, String prefix) throws NumberFormatException
    {
        String number = standardPathToken.substring(prefix.length());
        return Integer.parseInt(number);
    }

    @SuppressWarnings("unchecked")
    static List<String> readLines(File mappingFile) throws IOException, FileNotFoundException
    {
        return IOUtils.readLines(new FileInputStream(mappingFile));
    }

    private static boolean isBDS(File dataset)
    {
        File[] files = dataset.listFiles();
        return containsDir(files, VERSION_DIR) && containsDir(files, DATA_DIR)
                && containsDir(files, METADATA_DIR) && containsDir(files, ANNOTATIONS_DIR);
    }

    private static boolean containsDir(File[] files, String dirName)
    {
        for (File file : files)
        {
            if (file.getName().equalsIgnoreCase(dirName))
            {
                return true;
            }
        }
        return false;
    }

    static File tryGetOriginalDir(File dataset)
    {
        File orgDir = new File(dataset, DATA_DIR + DIR_SEP + ORIGINAL_DIR);
        if (orgDir.isDirectory() == false)
        {
            logError(dataset, "Original directory does not exist: " + orgDir);
            return null;
        }
        return orgDir;
    }

    static void logError(File dataset, String reason)
    {
        operationLog.error("Cannot migrate dataset '" + dataset.getName() + "'. " + reason);
    }

}
