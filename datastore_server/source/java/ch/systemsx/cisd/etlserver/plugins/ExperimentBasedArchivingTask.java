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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * Archiving maintenance task which archives all data sets of experiments starting with the oldest
 * experiment if free disk space is below a threshold.
 * 
 * @author Franz-Josef Elmer
 */
public class ExperimentBasedArchivingTask implements IDataStoreLockingMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExperimentBasedArchivingTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            ExperimentBasedArchivingTask.class);
    
    private static final long ONE_KB_IN_BYTES = 1024L;

    static final String MINIMUM_FREE_SPACE_KEY = "minimum-free-space-in-MB";

    static final String EXCLUDED_DATA_SET_TYPES_KEY = "excluded-data-set-types";

    static final String MONITORED_DIR = "monitored-dir";

    static final String FREE_SPACE_PROVIDER_PREFIX = "free-space-provider.";

    static final String DATA_SET_SIZE_PREFIX = "data-set-size.";

    static final String DEFAULT_DATA_SET_TYPE = "DEFAULT";

    private static final EnumSet<DataSetArchivingStatus> ARCHIVE_STATES = EnumSet.of(
            DataSetArchivingStatus.ARCHIVE_PENDING, DataSetArchivingStatus.ARCHIVED);

    private final IEncapsulatedOpenBISService service;

    private IFreeSpaceProvider freeSpaceProvider;

    private File monitoredDirectory;

    private long minimumFreeSpace;

    private Set<String> excludedDataSetTypes;

    private Map<String, Long> estimatedDataSetSizes;

    public ExperimentBasedArchivingTask()
    {
        this(ServiceProvider.getOpenBISService());
    }

    ExperimentBasedArchivingTask(IEncapsulatedOpenBISService service)
    {
        this.service = service;
    }

    public boolean requiresDataStoreLock()
    {
        return true;
    }

    public void setUp(String pluginName, Properties properties)
    {
        freeSpaceProvider = setUpFreeSpaceProvider(properties);
        monitoredDirectory = setUpMonitoredDirectory(properties);
        minimumFreeSpace =
                FileUtils.ONE_MB * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);
        excludedDataSetTypes =
                new HashSet<String>(Arrays.asList(PropertyParametersUtil.parseItemisedProperty(
                        properties.getProperty(EXCLUDED_DATA_SET_TYPES_KEY, ""),
                        EXCLUDED_DATA_SET_TYPES_KEY)));
        estimatedDataSetSizes = setUpEstimatedDataSetSizes(properties);
    }

    private File setUpMonitoredDirectory(Properties properties)
    {
        final String monitoredDirPath = PropertyUtils.getProperty(properties, MONITORED_DIR);
        File monitoredDir = new File(monitoredDirPath);
        if (monitoredDir.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Directory '" + monitoredDirPath
                    + "' doesn't exists or isn't a directory.");
        }
        return monitoredDir;
    }

    private IFreeSpaceProvider setUpFreeSpaceProvider(Properties properties)
    {
        Properties providerProps =  ExtendedProperties.getSubset(properties, FREE_SPACE_PROVIDER_PREFIX, true);
        String freeSpaceProviderClassName =
                PropertyUtils.getProperty(providerProps, "class",
                        SimpleFreeSpaceProvider.class.getName());
        
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(freeSpaceProviderClassName);
        } catch (ClassNotFoundException cnfe)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Cannot find configured free space provider class '%s'",
                    freeSpaceProviderClassName);
        }
        
        if (ClassUtils.hasConstructor(clazz, properties))
        {
            return ClassUtils.create(IFreeSpaceProvider.class, clazz, properties);

        } else
        {
            return ClassUtils.create(IFreeSpaceProvider.class, clazz);
        }
        
    }

    private Map<String, Long> setUpEstimatedDataSetSizes(Properties properties)
    {
        ISimpleLogger log = new Log4jSimpleLogger(operationLog);
        Properties dataSetSizeProps =
                ExtendedProperties.getSubset(properties, DATA_SET_SIZE_PREFIX, true);
        Map<String, Long> result = new HashMap<String, Long>();
        for (Object key : dataSetSizeProps.keySet())
        {
            String dataSetType = ((String) key).toUpperCase();
            long estimatedSizeInBytes =
                    ONE_KB_IN_BYTES
                            * PropertyUtils.getPosLong(dataSetSizeProps, dataSetType, 0L, log);
            if (estimatedSizeInBytes > 0)
            {
                result.put(dataSetType, estimatedSizeInBytes);
            }

        }
        return result;
    }

    public void execute()
    {
        long freeSpace = getFreeSpace();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Free space: %d MB, minimal free space required: %d MB", freeSpace,
                    minimumFreeSpace));
        }
        if (freeSpace >= minimumFreeSpace)
        {
            return;
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Free space is below threshold: " + freeSpace + " ("
                    + FileUtils.byteCountToDisplaySize(freeSpace) + ") < " + minimumFreeSpace
                    + " (" + FileUtils.byteCountToDisplaySize(minimumFreeSpace) + ")");
        }
        List<ExperimentDataSetsInfo> infos = new ArrayList<ExperimentDataSetsInfo>();
        for (Project project : service.listProjects())
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifierFactory(project.getIdentifier()).createIdentifier();
            for (Experiment experiment : service.listExperiments(projectIdentifier))
            {
                List<ExternalData> dataSets =
                        service.listDataSetsByExperimentID(experiment.getId());
                infos.add(new ExperimentDataSetsInfo(experiment.getIdentifier(), dataSets));
            }
        }
        Collections.sort(infos, new ExperimentDataSetsInfoComparator());
        StringBuilder archivingMessages = new StringBuilder();
        try
        {
            for (int i = 0; i < infos.size() && freeSpace < minimumFreeSpace; i++)
            {
                ExperimentDataSetsInfo info = infos.get(i);
                long estimatedSpaceFreed = info.estimateSize();
                if (archive(info, archivingMessages))
                {
                    freeSpace += estimatedSpaceFreed;
                }
            }
        } finally
        {
            if (archivingMessages.length() > 0)
            {
                notificationLog.info("Archiving summary:" + archivingMessages);
            }
        }
    }

    private long getFreeSpace()
    {
        try
        {
            return ONE_KB_IN_BYTES
                    * freeSpaceProvider.freeSpaceKb(new HostAwareFile(monitoredDirectory));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private boolean archive(ExperimentDataSetsInfo info, StringBuilder archivingMessages)
    {
        List<DataSet> dataSets = info.getDataSetsToBeArchived();
        if (dataSets.isEmpty())
        {
            return false;
        }
        List<String> dataSetCodes = new ArrayList<String>();
        for (DataSet dataSet : dataSets)
        {
            dataSetCodes.add(dataSet.getCode());
        }
        final String message =
                "#" + dataSetCodes.size() + " data sets of experiment "
                        + info.getExperimentIdentifier() + ": " + dataSetCodes;
        operationLog.info("Starting archiving " + message);
        service.archiveDataSets(dataSetCodes, true);
        archivingMessages.append('\n').append("Archived " + message);
        return true;
    }

    private final class ExperimentDataSetsInfo
    {
        private Date lastModificationDate;

        private List<DataSet> dataSetsToBeArchived = new ArrayList<DataSet>();

        private final String experimentIdentifier;

        ExperimentDataSetsInfo(String experimentIdentifier, List<ExternalData> dataSets)
        {
            this.experimentIdentifier = experimentIdentifier;
            for (ExternalData dataSet : dataSets)
            {
                if (dataSet instanceof DataSet == false)
                {
                    continue;
                }
                DataSet realDataSet = (DataSet) dataSet;
                if (excludedDataSetTypes.contains(realDataSet.getDataSetType().getCode()))
                {
                    continue;
                }
                DataSetArchivingStatus status = realDataSet.getStatus();
                if (DataSetArchivingStatus.LOCKED.equals(status))
                {
                    continue;
                }
                if (ARCHIVE_STATES.contains(status))
                {
                    continue;
                }
                dataSetsToBeArchived.add(realDataSet);
                Date modificationDate = dataSet.getModificationDate();
                if (modificationDate == null)
                {
                    modificationDate = dataSet.getRegistrationDate();
                }
                if (lastModificationDate == null || lastModificationDate.before(modificationDate))
                {
                    lastModificationDate = modificationDate;
                }
            }
        }

        public long estimateSize()
        {
            long sum = 0L;
            for (DataSet dataSetToBeArchived : getDataSetsToBeArchived())
            {
                sum += estimateSize(dataSetToBeArchived);
            }
            return sum;
        }

        private long estimateSize(DataSet dataSet)
        {
            String dataSetType = dataSet.getDataSetType().getCode().toUpperCase();
            Long estimatedDataSetSize = estimatedDataSetSizes.get(dataSetType);
            if (estimatedDataSetSize == null)
            {
                estimatedDataSetSize = estimatedDataSetSizes.get(DEFAULT_DATA_SET_TYPE);
            }
            if (estimatedDataSetSize == null)
            {
                throw ConfigurationFailureException
                        .fromTemplate(
                                "Failed to estimate the average size for data set type '%s'. "
                                        + "Please, configure the maintenance task with a '%s' or a '%s' property. ",
                                dataSetType, DATA_SET_SIZE_PREFIX + dataSetType,
                                DATA_SET_SIZE_PREFIX + DEFAULT_DATA_SET_TYPE);

            }
            return estimatedDataSetSize.longValue();
        }

        public String getExperimentIdentifier()
        {
            return experimentIdentifier;
        }

        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }

        public List<DataSet> getDataSetsToBeArchived()
        {
            return dataSetsToBeArchived;
        }
    }

    private final class ExperimentDataSetsInfoComparator implements
            Comparator<ExperimentDataSetsInfo>
    {
        public int compare(ExperimentDataSetsInfo i1, ExperimentDataSetsInfo i2)
        {
            Date d1 = i1.getLastModificationDate();
            Date d2 = i2.getLastModificationDate();
            if (d1 != null && d2 != null)
            {
                return d1.compareTo(d2);
            }
            if (d1 == null && d2 != null)
            {
                return 1;
            }
            if (d1 != null && d2 == null)
            {
                return -1;
            }
            return 0;
        }
    }
}
