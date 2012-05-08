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

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Class responsible for recovering after an environmental error.
 * 
 * @author jakubs
 */
public class DataSetStorageRecoveryManager implements IDataSetStorageRecoveryManager
{
    private static final String PROCESSING_MARKER = ".PROCESSING_MARKER";

    private File dropboxRecoveryStateDir;

    public <T extends DataSetInformation> void checkpointPrecommittedState(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();

        File serializedFile = getSerializedFile(runner);

        DataSetStoragePrecommitRecoveryState<T> recoveryState =
                new DataSetStoragePrecommitRecoveryState<T>(runner.getDataSetStorageAlgorithms(),
                        runner.getDssRegistrationLogger(), runner.getRollbackStack(), incoming);

        FileUtilities.writeToFile(serializedFile, recoveryState);

        File processingMarkerFile = getProcessingMarkerFile(runner);
        FileUtilities.writeToFile(processingMarkerFile, serializedFile.getAbsolutePath());

    }

    private <T extends DataSetInformation> File getProcessingMarkerFile(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        return new File(runner.getIncomingDataSetFile().getRealIncomingFile().getParentFile(),
                runner.getIncomingDataSetFile().getRealIncomingFile().getName() + PROCESSING_MARKER);
    }

    private <T extends DataSetInformation> File getSerializedFile(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        DataSetFile incoming = runner.getIncomingDataSetFile();
        String incomingFileName = incoming.getRealIncomingFile().getName();

        return new File(dropboxRecoveryStateDir, incomingFileName + ".PRECOMMIT_SERIALIZED");
    }

    @SuppressWarnings("unchecked")
    public <T extends DataSetInformation> DataSetStoragePrecommitRecoveryState<T> extractPrecommittedCheckpoint(
            File markerFile)
    {
        String recoveryFilePath = FileUtilities.loadToString(markerFile);
        return FileUtilities.loadToObject(new File(recoveryFilePath),
                DataSetStoragePrecommitRecoveryState.class);
    }

    public <T extends DataSetInformation> void registrationCompleted(
            DataSetStorageAlgorithmRunner<T> runner)
    {
        // Cleanup the state we have accumulated
        File markerFile = getProcessingMarkerFile(runner);
        FileUtilities.delete(markerFile);
        File recoveryState = getSerializedFile(runner);
        FileUtilities.delete(recoveryState);
    }

    public boolean canRecoverFromError(Throwable ex)
    {
        return true;
    }

    public boolean isRecoveryFile(File file)
    {
        return file.getName().endsWith(PROCESSING_MARKER);
    }

    public void setDropboxRecoveryStateDir(File dropboxRecoveryStateDir)
    {
        this.dropboxRecoveryStateDir = dropboxRecoveryStateDir;
    }
}
