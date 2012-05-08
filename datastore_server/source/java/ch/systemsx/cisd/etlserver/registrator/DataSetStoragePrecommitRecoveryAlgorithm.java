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
import java.io.Serializable;

import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DataStoreStrategyKey;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class DataSetStoragePrecommitRecoveryAlgorithm<T extends DataSetInformation> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final DataStoreStrategyKey dssKey;

    private final File incomingDataSetFile;

    private final File stagingDirectory;

    private final File preCommitDirectory;

    private final String dataStoreCode;

    // registration details
    // dataset information
    // storageprocessor
    // datastorecode
    // datasettype
    // mail client
    // state

    // we also need to store the precommitted state precommitted state
    private final IStorageProcessorTransaction transaction;

    private final File markerFile;
    
    private final DataSetStorageAlgorithm.DataSetStoragePaths dataSetStoragePaths;

    public DataSetStoragePrecommitRecoveryAlgorithm(DataStoreStrategyKey dataStoreStrategyKey,
            File incomingDataSetFile, File stagingDirectory, File preCommitDirectory,
            String dataStoreCode,
            
            DataSetStorageAlgorithm.DataSetStoragePaths dataSetStoragePaths,
            File markerFile, IStorageProcessorTransaction transaction
            )
    {
        this.dssKey = dataStoreStrategyKey;
        this.incomingDataSetFile = incomingDataSetFile;
        this.stagingDirectory = stagingDirectory;
        this.preCommitDirectory = preCommitDirectory;

        this.dataStoreCode = dataStoreCode;
        
        this.dataSetStoragePaths = dataSetStoragePaths;
        this.transaction = transaction;
        this.markerFile = markerFile;
    }

    public DataSetStorageAlgorithm<T> recoverDataSetStorageAlgorithm(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        IDataStoreStrategy dataStoreStrategy =
                state.getDataStrategyStore().getDataStoreStrategy(getDataStoreStrategyKey());

        IMailClient mailClient = state.getGlobalState().getMailClient();
        IFileOperations fileOperations = state.getFileOperations();

        IStorageProcessorTransactional storageProcessor = state.getStorageProcessor();
        
        return new DataSetStorageAlgorithm<T>(
                dataStoreStrategy,
               storageProcessor, 
                fileOperations,
                mailClient,
                this);
        
    }
    
    public DataStoreStrategyKey getDataStoreStrategyKey()
    {
        return dssKey;
    }

    public File getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public File getStagingDirectory()
    {
        return stagingDirectory;
    }

    public File getPreCommitDirectory()
    {
        return preCommitDirectory;
    }

    public String getDataStoreCode()
    {
        return dataStoreCode;
    }
    
    public IStorageProcessorTransaction getTransaction()
    {
        return transaction;
    }

    public File getMarkerFile()
    {
        return markerFile;
    }

    public DataSetStorageAlgorithm.DataSetStoragePaths getDataSetStoragePaths()
    {
        return dataSetStoragePaths;
    }
}
