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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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
    @Private
    static final String POST_REGISTRATION_TASKS_PROPERTY = "post-registration-tasks";

    @Private
    static final String IGNORE_DATA_SETS = "ignore-data-sets-before-date";

    @Private
    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    private static final String DEFAULT_LAST_SEEN_DATA_SET_FILE = "last-seen-data-set.txt";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostRegistrationMaintenanceTask.class);

    private IEncapsulatedOpenBISService service;

    private boolean needsLockOnDataStore;

    private Set<Entry<String, IPostRegistrationTask>> tasks;

    private File lastSeenDataSetFile;

    private File newLastSeenDataSetFile;

    private Date ignoreBeforeDate;

    private TaskExecutor executor;

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
            Properties taskProperties = sectionProperty.getProperties();
            String className = PropertyUtils.getMandatoryProperty(taskProperties, "class");
            IPostRegistrationTask task =
                    ClassUtils.create(IPostRegistrationTask.class, className, taskProperties,
                            service);
            if (task.requiresDataStoreLock())
            {
                needsLockOnDataStore = true;
            }
            map.put(sectionProperty.getKey(), task);
        }
        tasks = map.entrySet();
        executor = new TaskExecutor(properties, operationLog);
        String fileName =
                properties.getProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY,
                        DEFAULT_LAST_SEEN_DATA_SET_FILE);
        lastSeenDataSetFile = new File(fileName);
        newLastSeenDataSetFile = new File(fileName + ".new");
        String property = properties.getProperty(IGNORE_DATA_SETS);
        if (property == null)
        {
            ignoreBeforeDate = new Date(0);
        } else
        {
            try
            {
                ignoreBeforeDate = DateUtils.parseDate(property, new String[]
                    { "yyyy-MM-dd" });
            } catch (ParseException ex)
            {
                throw new ConfigurationFailureException("Invalid value of property '"
                        + IGNORE_DATA_SETS + "': " + property);
            }
        }
    }

    public void execute()
    {
        executor.cleanup();
        List<ExternalData> dataSets = getSortedUnseenDataSets();
        for (int i = 0; i < dataSets.size(); i++)
        {
            ExternalData dataSet = dataSets.get(i);
            String code = dataSet.getCode();
            operationLog.info("Post registration of " + (i + 1) + ". of " + dataSets.size()
                    + " data sets: " + code);
            try
            {
                for (Entry<String, IPostRegistrationTask> entry : tasks)
                {
                    IPostRegistrationTask task = entry.getValue();
                    String taskName = entry.getKey();
                    executor.execute(task, taskName, code, dataSet.isContainer());
                }
                saveLastSeenDataSetId(dataSet.getId());
            } catch (Throwable ex)
            {
                operationLog.error("Post registration failed.", ex);
                logPostponingMessage(dataSets, i);
                break;
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
        List<ExternalData> filteredList = new ArrayList<ExternalData>();
        for (ExternalData dataSet : dataSets)
        {
            if (dataSet.getRegistrationDate().getTime() > ignoreBeforeDate.getTime())
            {
                filteredList.add(dataSet);
            }
        }
        Collections.sort(filteredList, new Comparator<ExternalData>()
            {
                public int compare(ExternalData o1, ExternalData o2)
                {
                    return (int) (o1.getId() - o2.getId());
                }
            });
        return filteredList;
    }

    private void saveLastSeenDataSetId(long lastSeenDataSetId)
    {
        FileUtilities.writeToFile(newLastSeenDataSetFile, Long.toString(lastSeenDataSetId));
        newLastSeenDataSetFile.renameTo(lastSeenDataSetFile);
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

}
