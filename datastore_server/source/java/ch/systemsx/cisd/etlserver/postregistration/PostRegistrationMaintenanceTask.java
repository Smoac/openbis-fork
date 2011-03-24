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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * Maintenance task performing {@link IPostRegistrationTask}s.
 *
 * @author Franz-Josef Elmer
 */
public class PostRegistrationMaintenanceTask implements IDataStoreLockingMaintenanceTask
{
    private static final String POST_REGISTRATION_TASKS_PROPERTY = "post-registration-tasks";
    
    private static final String CLEANUP_TASKS_FOLDER_PROPERTY = "cleanup-tasks-folder";

    private static final String DEFAULT_CLEANUP_TASKS_FOLDER = "clean-up-tasks";
    
    private static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";
    
    private static final String DEFAULT_LAST_SEEN_DATA_SET_FILE = "last-seen-data-set.txt";
    
    private static final String FILE_TYPE = ".ser";

    private static final FilenameFilter FILTER = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(FILE_TYPE);
            }
        };

    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, PostRegistrationMaintenanceTask.class);
    
    private IEncapsulatedOpenBISService service;
    
    private boolean needsLockOnDataStore;
    
    private Set<Entry<String, IPostRegistrationTask>> tasks;
    
    private File cleanupTasksFolder;

    private File lastSeenDataSetFile;

    private File newLastSeenDataSetFile;

    public boolean requiresDataStoreLock()
    {
        return needsLockOnDataStore;
    }

    public void setUp(String pluginName, Properties properties)
    {
        service = ServiceProvider.getOpenBISService();
        // Linked hash map because the task should be executed in the order of definition.
        Map<String, IPostRegistrationTask> map = new LinkedHashMap<String, IPostRegistrationTask>();
        SectionProperties[] sectionProperties =
                PropertyParametersUtil.extractSectionProperties(properties,
                        POST_REGISTRATION_TASKS_PROPERTY, false);
        for (SectionProperties sectionProperty : sectionProperties)
        {
            Properties taskPorperties = sectionProperty.getProperties();
            String className = PropertyUtils.getMandatoryProperty(taskPorperties, "class");
            IPostRegistrationTask task =
                    ClassUtils.create(IPostRegistrationTask.class, className, taskPorperties,
                            service);
            if (task.requiresDataStoreLock())
            {
                needsLockOnDataStore = true;
            }
            map.put(sectionProperty.getKey(), task);
        }
        tasks = map.entrySet();
        cleanupTasksFolder =
                new File(properties.getProperty(CLEANUP_TASKS_FOLDER_PROPERTY,
                        DEFAULT_CLEANUP_TASKS_FOLDER));
        if (cleanupTasksFolder.isFile())
        {
            throw new EnvironmentFailureException("Cleanup tasks folder is a file: "
                    + cleanupTasksFolder.getAbsolutePath());
        }
        cleanupTasksFolder.mkdirs();
        String fileName =
                properties.getProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY,
                        DEFAULT_LAST_SEEN_DATA_SET_FILE);
        lastSeenDataSetFile = new File(fileName);
        newLastSeenDataSetFile = new File(fileName + ".new");
    }

    public void execute()
    {
        cleanup();
        List<ExternalData> dataSets = getSortedUnseenDataSets();
        for (int i = 0; i < dataSets.size(); i++)
        {
            ExternalData dataSet = dataSets.get(i);
            String code = dataSet.getCode();
            try
            {
                for (Entry<String, IPostRegistrationTask> entry : tasks)
                {
                    IPostRegistrationTask task = entry.getValue();
                    ICleanupTask cleanupTask = null;
                    File savedCleanupTask = null;
                    String taskName = entry.getKey();
                    try
                    {
                        cleanupTask = task.createCleanupTask(code);
                        savedCleanupTask = save(code, taskName, cleanupTask);
                        task.execute(code);
                    } catch (Throwable t)
                    {
                        cleanUpAndLog(t, cleanupTask, code, taskName);
                        throw t;
                    } finally
                    {
                        if (savedCleanupTask != null)
                        {
                            savedCleanupTask.delete();
                        }
                    }
                }
                saveLastSeenDataSetId(dataSet.getId());
            } catch (Throwable ex)
            {
                logPostponingMessage(dataSets, i);
            }
        }
    }

    private List<ExternalData> getSortedUnseenDataSets()
    {
        long lastSeenDataSetId = 0;
        if (lastSeenDataSetFile.exists())
        {
            lastSeenDataSetId =
                    Long.parseLong(FileUtilities.loadToString(lastSeenDataSetFile).trim());
        }
        TrackingDataSetCriteria criteria = new TrackingDataSetCriteria(lastSeenDataSetId);
        List<ExternalData> dataSets = service.listNewerDataSets(criteria);
        Collections.sort(dataSets, new Comparator<ExternalData>()
            {
                public int compare(ExternalData o1, ExternalData o2)
                {
                    return (int) (o1.getId() - o2.getId());
                }
            });
        return dataSets;
    }

    private void saveLastSeenDataSetId(long lastSeenDataSetId)
    {
        FileUtilities.writeToFile(newLastSeenDataSetFile, Long.toString(lastSeenDataSetId));
        newLastSeenDataSetFile.renameTo(lastSeenDataSetFile);
    }

    private void cleanUpAndLog(Throwable throwable, ICleanupTask cleanupTaskOrNull,
            String dataSetCode, String taskName)
    {
        operationLog.error("Post registration task '" + taskName + "' for data set " + dataSetCode
                + " failed.", throwable);
        if (cleanupTaskOrNull != null)
        {
            try
            {
                cleanupTaskOrNull.cleanup();
            } catch (Throwable t)
            {
                operationLog.error("Clean up of failed post registration task '" + taskName
                        + "' for data set " + dataSetCode + " failed, too.", t);
            }
        }
    }

    private void logPostponingMessage(List<ExternalData> dataSets, int i)
    {
        int numberOfDataSets = dataSets.size();
        if (i < numberOfDataSets - 1)
        {
            StringBuilder builder = new StringBuilder();
            for (int j = i + 1; j < numberOfDataSets; j++)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(dataSets.get(j).getCode());
            }
            operationLog.error("Because post registration task failed for data set "
                    + dataSets.get(i).getCode() + " post registration tasks are postponed for "
                    + "the following data sets: " + builder);
        }
    }

    private void cleanup()
    {
        File[] files = cleanupTasksFolder.listFiles(FILTER);
        for (File file : files)
        {
            cleanupTask(file);
        }
    }

    private void cleanupTask(File file)
    {
        try
        {
            byte[] bytes = FileUtils.readFileToByteArray(file);
            ((ICleanupTask) SerializationUtils.deserialize(bytes)).cleanup();
        } catch (Exception ex)
        {
            operationLog.error("Couldn't performed clean up task " + file, ex);
        }
        file.delete();
    }

    private File save(String code, String taskName, ICleanupTask cleanupTask)
    {
        try
        {
            File file = new File(cleanupTasksFolder, code + "_" + taskName + FILE_TYPE);
            FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(cleanupTask));
            return file;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
