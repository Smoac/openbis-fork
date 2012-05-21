/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Class responsible for recovering after an environmental error.
 * 
 * @author jakubs
 */
public class DataSetStorageRecoveryManager implements IDataSetStorageRecoveryManager
{
    private static final String PRECOMMIT_SERIALIZED = ".PRECOMMIT_SERIALIZED";

    private static final String PROCESSING_MARKER = ".PROCESSING_MARKER";

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageRecoveryManager.class);

    private File dropboxRecoveryStateDir;

    private File recoveryMarkerFilesDir;
    
    private int maxRetryCount = 50;
    
    private int retryPeriodInSeconds = 60;
    
    public <T extends DataSetInformation> void checkpointPrecommittedState(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();

        File serializedFile = getSerializedFile(runner);

        DataSetStoragePrecommitRecoveryState<T> recoveryState =
                new DataSetStoragePrecommitRecoveryState<T>(runner.getDataSetStorageAlgorithms(),
                        runner.getDssRegistrationLogger(), runner.getRollbackStack(), incoming,
                        runner.getPersistentMap());

        runner.getRollbackStack().setLockedState(true);

        FileUtilities.writeToFile(serializedFile, recoveryState);

        File processingMarkerFile = getProcessingMarkerFile(runner);
        
        DataSetStorageRecoveryInfo info = new DataSetStorageRecoveryInfo(serializedFile, new Date(), 0);
        
        info.writeToFile(processingMarkerFile);

        operationLog.info("Store precommit recovery checkpoint with markerfile "
                + processingMarkerFile);
    }

    public <T extends DataSetInformation> void removeCheckpoint(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        cleanup(runner);
    }

    private <T extends DataSetInformation> File getProcessingMarkerFile(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        return getProcessingMarkerFile(runner.getIncomingDataSetFile().getRealIncomingFile());
    }

    /**
     * @return processing marker file for a given incoming file.
     */
    public File getProcessingMarkerFile(File incoming)
    {
        return new File(recoveryMarkerFilesDir, incoming.getName() + PROCESSING_MARKER);
    }

    private <T extends DataSetInformation> File getSerializedFile(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();
        String incomingFileName = incoming.getRealIncomingFile().getName();

        return new File(dropboxRecoveryStateDir, incomingFileName + PRECOMMIT_SERIALIZED);
    }

    public DataSetStorageRecoveryInfo getRecoveryFileFromMarker(File markerFile)
    {
        return DataSetStorageRecoveryInfo.loadFromFile(markerFile);
    }

    @SuppressWarnings("unchecked")
    public <T extends DataSetInformation> DataSetStoragePrecommitRecoveryState<T> extractPrecommittedCheckpoint(
            File markerFile)
    {
        DataSetStorageRecoveryInfo info = getRecoveryFileFromMarker(markerFile);
        return FileUtilities.loadToObject(info.getRecoveryStateFile(), DataSetStoragePrecommitRecoveryState.class);
    }

    public <T extends DataSetInformation> void registrationCompleted(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        cleanup(runner);
    }

    public <T extends DataSetInformation> void cleanup(DataSetStorageAlgorithmRunner<T> runner)
    {
        File markerFile = getProcessingMarkerFile(runner);
        File recoveryState = getSerializedFile(runner);

        operationLog.info("Cleanup recovery with marker file " + markerFile);

        runner.getRollbackStack().setLockedState(false);
        // Cleanup the state we have accumulated
        FileUtilities.delete(markerFile);
        FileUtilities.delete(recoveryState);
    }

    public boolean canRecoverFromError(Throwable ex)
    {
        if (ex instanceof UserFailureException)
        {
            return false;
        }
        return true;
    }

    public void setDropboxRecoveryStateDir(File dropboxRecoveryStateDir)
    {
        this.dropboxRecoveryStateDir = dropboxRecoveryStateDir;
    }

    public void setRecoveryMarkerFilesDir(File recoveryMarkerFileDir)
    {
        this.recoveryMarkerFilesDir = recoveryMarkerFileDir;
    }

    public void setMaximumRertyCount(int maxRetryCount)
    {
        this.maxRetryCount = maxRetryCount;
    }

    public int getMaximumRertyCount()
    {
        return this.maxRetryCount;
    }
    
    public int getRetryPeriodInSeconds()
    {
        return retryPeriodInSeconds;
    }
    
    public void setRetryPeriodInSeconds(int retryPeriodInSeconds)
    {
        this.retryPeriodInSeconds = retryPeriodInSeconds;
        
    }
    
}
