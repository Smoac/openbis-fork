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

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Interface for recovery managers.
 * 
 * @author jakubs
 */
public interface IDataSetStorageRecoveryManager
{
    <T extends DataSetInformation> void checkpointPrecommittedState(
            DataSetStorageAlgorithmRunner<T> runner);

    void registrationCompleted();

    boolean canRecoverFromError(Throwable ex);

    <T extends DataSetInformation> DataSetStoragePrecommitRecoveryState<T> extractPrecommittedCheckpoint(
            File recoveryFile);

    boolean isRecoveryFile(File file);

    void setDropboxRecoveryStateDir(File dropboxRecoveryStateDir);
}