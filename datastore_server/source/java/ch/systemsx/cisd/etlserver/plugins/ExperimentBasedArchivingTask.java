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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
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

    static final String MINIMUM_FREE_SPACE_KEY = "minimum-free-space-in-MB";

    static final String STOREROOT_DIR_KEY = "storeroot-dir";

    static final String MONITORED_SHARE_KEY = "monitored-share";

    static final String EXCLUDED_DATA_SET_TYPES_KEY = "excluded-data-set-types";

    static final String MONITORED_DIR = "monitored-dir";

    private static final EnumSet<DataSetArchivingStatus> ARCHIVE_STATES = EnumSet.of(
            DataSetArchivingStatus.ARCHIVE_PENDING, DataSetArchivingStatus.ARCHIVED);

    private final IEncapsulatedOpenBISService service;

    private final IFreeSpaceProvider freeSpaceProvider;

    private File storeRoot;

    private File monitoredDirectory;

    private long minimumFreeSpace;

    private String shareIDOrNull;

    private final IShareIdManager shareIdManager;

    private Set<String> excludedDataSetTypes;

    public ExperimentBasedArchivingTask()
    {
        this(ServiceProvider.getOpenBISService(), new SimpleFreeSpaceProvider(), ServiceProvider
                .getShareIdManager());
    }

    ExperimentBasedArchivingTask(IEncapsulatedOpenBISService service,
            IFreeSpaceProvider freeSpaceProvider, IShareIdManager shareIdManager)
    {
        this.service = service;
        this.freeSpaceProvider = freeSpaceProvider;
        this.shareIdManager = shareIdManager;
    }

    public boolean requiresDataStoreLock()
    {
        return true;
    }

    public void setUp(String pluginName, Properties properties)
    {
        String storeRootFileName =
                PropertyUtils.getMandatoryProperty(properties, STOREROOT_DIR_KEY);
        storeRoot = new File(storeRootFileName);
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root doesn't exists or isn't a directory: " + storeRoot);
        }
        final String monitoredDirPath = PropertyUtils.getProperty(properties, MONITORED_DIR);
        if (monitoredDirPath == null)
        {
            shareIDOrNull = PropertyUtils.getMandatoryProperty(properties, MONITORED_SHARE_KEY);
            monitoredDirectory = new File(storeRoot, shareIDOrNull);
        } else
        {
            shareIDOrNull = null;
            monitoredDirectory = new File(monitoredDirPath);
        }
        if (monitoredDirectory.isDirectory() == false)
        {
            if (monitorDataStoreShare())
            {
                throw new ConfigurationFailureException("Share " + shareIDOrNull
                        + " doesn't exists or isn't a directory.");
            } else
            {
                throw new ConfigurationFailureException("Directory '" + monitoredDirPath
                        + "' doesn't exists or isn't a directory.");
            }
        }
        minimumFreeSpace =
                FileUtils.ONE_MB * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);
        excludedDataSetTypes =
                new HashSet<String>(Arrays.asList(PropertyParametersUtil.parseItemisedProperty(
                        properties.getProperty(EXCLUDED_DATA_SET_TYPES_KEY, ""),
                        EXCLUDED_DATA_SET_TYPES_KEY)));
    }

    public void execute()
    {
        long freeSpace = getFreeSpace();
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
        if (monitorDataStoreShare())
        {
            for (int i = 0; i < infos.size() && freeSpace < minimumFreeSpace; i++)
            {
                ExperimentDataSetsInfo info = infos.get(i);
                freeSpace += info.calculateSize();
                archive(info, archivingMessages);
            }
        } else
        {
            for (int i = 0; i < infos.size() && freeSpace < minimumFreeSpace; i++)
            {
                if (archive(infos.get(i), archivingMessages))
                {
                    freeSpace = getFreeSpace();
                }
            }
        }
        if (archivingMessages.length() > 0)
        {
            notificationLog.info("Archiving summary:" + archivingMessages);
        }
    }

    private boolean monitorDataStoreShare()
    {
        return shareIDOrNull != null;
    }

    private long getFreeSpace()
    {
        try
        {
            return 1024L * freeSpaceProvider.freeSpaceKb(new HostAwareFile(monitoredDirectory));
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
                "Starting archiving " + dataSetCodes.size() + " data sets of experiment "
                        + info.getExperimentIdentifier() + ": " + dataSetCodes;
        operationLog.info(message);
        archivingMessages.append('\n').append(message);
        service.archiveDataSets(dataSetCodes, true);
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
                if (shareIDOrNull != null
                        && realDataSet.getShareId().equals(shareIDOrNull) == false)
                {
                    continue;
                }
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

        long calculateSize()
        {
            long sum = 0;
            for (DataSet dataSet : dataSetsToBeArchived)
            {
                Long size = dataSet.getSize();
                if (size == null)
                {
                    String dataSetCode = dataSet.getCode();
                    shareIdManager.lock(dataSetCode);
                    try
                    {
                        File shareRoot =
                                new File(storeRoot, shareIdManager.getShareId(dataSetCode));
                        String location = dataSet.getLocation();
                        size = FileUtils.sizeOfDirectory(new File(shareRoot, location));
                    } finally
                    {
                        shareIdManager.releaseLock(dataSetCode);
                    }
                }
                sum += size;
            }
            return sum;
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
