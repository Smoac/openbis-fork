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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;

/**
 * Maintenance task migrating all data sets of a store by a chain of {@link IMigrator} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class ChainedDataSetMigrationTask implements IMaintenanceTask
{
    public static final String MIGRATORS_PROPERTY = "migrators";

    public static final String STORE_ROOT_PROPERTY = "storeRoot";

    private static final Pattern UUID_PATTERN =
            Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ChainedDataSetMigrationTask.class);

    private File storeRoot;

    private List<IMigrator> migrators;

    public void setUp(String pluginName, Properties properties)
    {
        String storeRootPath = PropertyUtils.getMandatoryProperty(properties, STORE_ROOT_PROPERTY);
        storeRoot = new File(storeRootPath);
        if (storeRoot.isDirectory() == false)
        {
            throw new EnvironmentFailureException(storeRoot
                    + " does not exist or is not a directory.");
        }
        SectionProperties[] sectionProperties =
                PropertyParametersUtil.extractSectionProperties(properties, MIGRATORS_PROPERTY,
                        false);
        migrators = new ArrayList<IMigrator>();
        for (SectionProperties props : sectionProperties)
        {
            Properties migratorProperties = props.getProperties();
            IMigrator migrator =
                    ClassUtils.create(IMigrator.class, PropertyUtils.getMandatoryProperty(
                            migratorProperties, "class"), migratorProperties);
            migrators.add(migrator);
        }
        if (operationLog.isInfoEnabled())
        {
            StringBuilder builder = new StringBuilder();
            for (IMigrator migrator : migrators)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(migrator.getDescription());
            }
            operationLog.info("Chain of migrators have been set up: " + builder);
        }
    }

    public void execute()
    {
        File[] files = storeRoot.listFiles(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    String name = pathname.getName().toLowerCase();
                    return pathname.isDirectory() && UUID_PATTERN.matcher(name).matches();
                }
            });
        if (files == null || files.length == 0)
        {
            operationLog.warn("Store is empty, there is nothing to migrate");
            return;
        }
        if (files.length > 1)
        {
            throw new EnvironmentFailureException("At most one folder with UUID expected in "
                    + storeRoot);
        }
        File dbInstanceDir = files[0];
        for (IMigrator migrator : migrators)
        {
            boolean success = migrate(dbInstanceDir, migrator);
            if (success == false)
            {
                operationLog.error("Migration stopped at: " + migrator.getDescription());
                break;
            }
        }
    }

    private boolean migrate(File dbInstanceDir, IMigrator migrator)
    {
        List<File> migratedDataSets = new ArrayList<File>();
        List<File> failedDataSets = new ArrayList<File>();
        for (File l1 : dbInstanceDir.listFiles())
        {
            // The OS may put files into these folders (e.g., .DS_Store)
            if (false == l1.isDirectory())
            {
                continue;
            }
            for (File l2 : l1.listFiles())
            {
                if (false == l2.isDirectory())
                {
                    continue;
                }
                for (File l3 : l2.listFiles())
                {
                    if (false == l3.isDirectory())
                    {
                        continue;
                    }
                    for (File dataset : l3.listFiles())
                    {
                        boolean success = migrator.migrate(dataset);
                        if (success)
                        {
                            migratedDataSets.add(dataset);
                        } else
                        {
                            failedDataSets.add(dataset);
                        }
                    }
                }
            }
        }
        if (failedDataSets.isEmpty())
        {
            operationLog.info(migratedDataSets.size() + " data sets have been migrated with '"
                    + migrator.getDescription() + "'.");
        } else
        {
            operationLog.error("Migration with '" + migrator.getDescription()
                    + "' failed for the following " + failedDataSets.size() + " data sets: "
                    + failedDataSets);
        }
        return failedDataSets.isEmpty();
    }

}
