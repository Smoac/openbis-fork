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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.IWaitingCondition;
import ch.systemsx.cisd.common.utilities.WaitingHelper;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.FileBasedPause;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Task which waits until multi data set container file in the archive is replicated and archiving status can be set.
 *
 * @author Franz-Josef Elmer
 */
class MultiDataSetArchivingFinalizer implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    public static final String ORIGINAL_FILE_PATH_KEY = "original-file-path";

    public static final String REPLICATED_FILE_PATH_KEY = "replicated-file-path";

    public static final String FINALIZER_POLLING_TIME_KEY = "finalizer-polling-time";

    public static final String FINALIZER_MAX_WAITING_TIME_KEY = "finalizer-max-waiting-time";

    public static final String START_TIME_KEY = "start-time";

    public static final String STATUS_KEY = "status";

    public static final String TIME_STAMP_FORMAT = "yyyyMMdd-HHmmss";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiDataSetArchivingFinalizer.class);

    private final File pauseFile;

    private final long pauseFilePollingTime;

    private final ITimeAndWaitingProvider timeProvider;

    private final Properties cleanerProperties;

    private transient IMultiDataSetArchiveCleaner cleaner;

    MultiDataSetArchivingFinalizer(Properties cleanerProperties, File pauseFile, long pauseFilePollingTime,
            ITimeAndWaitingProvider timeProvider)
    {
        this.cleanerProperties = cleanerProperties;
        this.pauseFile = pauseFile;
        this.pauseFilePollingTime = pauseFilePollingTime;
        this.timeProvider = timeProvider;
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets, DataSetProcessingContext context)
    {
        List<String> dataSetCodes = extracCodes(datasets);
        Status status = Status.OK;
        try
        {
            Parameters parameters = getParameters(context);
            DataSetArchivingStatus archivingStatus = parameters.getStatus();
            boolean removeFromDataStore = archivingStatus.isAvailable() == false;
            File originalFile = parameters.getOriginalFile();
            if (originalFile.exists() == false)
            {
                String message = "Replication of '" + originalFile + "' failed because the original file does not exist.";
                status = createStatusAndRearchive(dataSetCodes, parameters, removeFromDataStore, originalFile, message);
            } else
            {
                operationLog.info("Waiting for replication of archive '" + originalFile
                        + "' containing the following data sets: " + CollectionUtils.abbreviate(dataSetCodes, 20));
                boolean noTimeout = waitUntilReplicated(parameters);
                if (noTimeout)
                {
                    DataSetCodesWithStatus codesWithStatus = new DataSetCodesWithStatus(dataSetCodes, archivingStatus, true);
                    IDataSetDeleter dataSetDeleter = ServiceProvider.getDataStoreService().getDataSetDeleter();
                    if (removeFromDataStore)
                    {
                        dataSetDeleter.scheduleDeletionOfDataSets(datasets,
                                TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                                TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    }
                    updateStatus(codesWithStatus);
                } else
                {
                    String message = "Replication of '" + originalFile + "' failed.";
                    status = createStatusAndRearchive(dataSetCodes, parameters, removeFromDataStore, originalFile, message);
                }
            }
        } catch (Exception ex)
        {
            operationLog.error("Finalizing failed", ex);
            status = Status.createError(ex.getMessage());
        }
        ProcessingStatus processingStatus = new ProcessingStatus();
        processingStatus.addDatasetStatuses(datasets, status);
        return processingStatus;
    }

    private Status createStatusAndRearchive(List<String> dataSetCodes, Parameters parameters, boolean removeFromDataStore, File originalFile,
            String message)
    {
        operationLog.error(message);
        Status status = Status.createError(message);
        getCleaner().delete(originalFile);
        getCleaner().delete(parameters.getReplicatedFile());
        removeFromMapping(originalFile);
        updateStatus(new DataSetCodesWithStatus(dataSetCodes, DataSetArchivingStatus.AVAILABLE, false));
        ServiceProvider.getOpenBISService().archiveDataSets(dataSetCodes, removeFromDataStore);
        return status;
    }

    private void removeFromMapping(File originalFile)
    {
        IMultiDataSetArchiverDBTransaction transaction = getTransaction();
        try
        {
            transaction.deleteContainer(originalFile.getName());
            transaction.commit();
        } catch (Exception ex)
        {
            transaction.rollback();
        }
        transaction.close();
    }

    protected void updateStatus(DataSetCodesWithStatus codesWithStatus)
    {
        ServiceProvider.getOpenBISService().updateDataSetStatuses(codesWithStatus.getDataSetCodes(),
                codesWithStatus.getStatus(), codesWithStatus.isPresentInArchive());
    }

    protected IMultiDataSetArchiveCleaner getCleaner()
    {
        if (cleaner == null)
        {
            cleaner = MultiDataSetArchivingUtils.createCleaner(cleanerProperties);
        }
        return cleaner;
    }

    IMultiDataSetArchiverDBTransaction getTransaction()
    {
        return new MultiDataSetArchiverDBTransaction();
    }

    private boolean waitUntilReplicated(Parameters parameters)
    {
        final File originalFile = parameters.getOriginalFile();
        final File replicatedFile = parameters.getReplicatedFile();
        final long originalSize = originalFile.length();
        long waitingTime = parameters.getWaitingTime();
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        WaitingHelper waitingHelper = new WaitingHelper(waitingTime, parameters.getPollingTime(), timeProvider, logger, true);
        long startTime = parameters.getStartTime();
        FileBasedPause pause = new FileBasedPause(pauseFile, pauseFilePollingTime, timeProvider, logger,
                "Waiting for replicated file " + parameters.getReplicatedFile());
        return waitingHelper.waitOn(startTime, new IWaitingCondition()
            {
                @Override
                public boolean conditionFulfilled()
                {
                    return replicatedFile.length() >= originalSize;
                }

                @Override
                public String toString()
                {
                    return FileUtilities.byteCountToDisplaySize(replicatedFile.length())
                            + " of " + FileUtilities.byteCountToDisplaySize(originalSize)
                            + " are replicated for " + originalFile;
                }
            }, pause);
    }

    private List<String> extracCodes(List<DatasetDescription> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (DatasetDescription dataSet : datasets)
        {
            codes.add(dataSet.getDataSetCode());
        }
        return codes;
    }

    private Parameters getParameters(DataSetProcessingContext context)
    {
        Map<String, String> parameterBindings = context.getParameterBindings();
        operationLog.info("Parameters: " + parameterBindings);
        Parameters parameters = new Parameters();
        parameters.setOriginalFile(new File(getProperty(parameterBindings, ORIGINAL_FILE_PATH_KEY)));
        parameters.setReplicatedFile(new File(getProperty(parameterBindings, REPLICATED_FILE_PATH_KEY)));
        parameters.setPollingTime(getNumber(parameterBindings, FINALIZER_POLLING_TIME_KEY));
        parameters.setStartTime(getTimestamp(parameterBindings, START_TIME_KEY));
        parameters.setWaitingTime(getNumber(parameterBindings, FINALIZER_MAX_WAITING_TIME_KEY));
        parameters.setStatus(DataSetArchivingStatus.valueOf(getProperty(parameterBindings, STATUS_KEY)));
        return parameters;
    }

    private long getTimestamp(Map<String, String> parameterBindings, String property)
    {
        String value = getProperty(parameterBindings, property);
        try
        {
            return new SimpleDateFormat(TIME_STAMP_FORMAT).parse(value).getTime();
        } catch (ParseException ex)
        {
            throw new IllegalArgumentException("Property '" + property + "' isn't a time stamp of format "
                    + TIME_STAMP_FORMAT + ": " + value);
        }
    }

    private long getNumber(Map<String, String> parameterBindings, String property)
    {
        String value = getProperty(parameterBindings, property);
        try
        {
            return Long.parseLong(value);
        } catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Property '" + property + "' isn't a number: " + value);
        }
    }

    private String getProperty(Map<String, String> parameterBindings, String property)
    {
        String value = parameterBindings.get(property);
        if (StringUtils.isBlank(value))
        {
            throw new IllegalArgumentException("Unknown property '" + property + "'.");
        }
        return value;
    }

    private static final class Parameters
    {

        private File originalFile;

        private File replicatedFile;

        private long pollingTime;

        private long startTime;

        private long waitingTime;

        private DataSetArchivingStatus status;

        public void setOriginalFile(File file)
        {
            originalFile = file;
        }

        public File getOriginalFile()
        {
            return originalFile;
        }

        public void setPollingTime(long pollingTime)
        {
            this.pollingTime = pollingTime;
        }

        public long getPollingTime()
        {
            return pollingTime;
        }

        public long getStartTime()
        {
            return startTime;
        }

        public void setStartTime(long startTime)
        {
            this.startTime = startTime;
        }

        public void setWaitingTime(long waitingTime)
        {
            this.waitingTime = waitingTime;
        }

        public long getWaitingTime()
        {
            return waitingTime;
        }

        public void setReplicatedFile(File file)
        {
            replicatedFile = file;
        }

        public File getReplicatedFile()
        {
            return replicatedFile;
        }

        public void setStatus(DataSetArchivingStatus status)
        {
            this.status = status;
        }

        public DataSetArchivingStatus getStatus()
        {
            return status;
        }
    }

}
