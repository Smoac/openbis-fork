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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * This class adapts an old-style {@link IStorageProcessor} to
 * {@link IStorageProcessorTransactional}.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Kaloyan Enimanev
 */
public class StorageProcessorTransactionalWrapper implements IStorageProcessorTransactional
{
    
    private final IStorageProcessor wrappedStorageProcessor;

    /**
     * ctor.
     */
    public StorageProcessorTransactionalWrapper(IStorageProcessor wrappedStorageProcessor)
    {
        this.wrappedStorageProcessor = wrappedStorageProcessor;
    }

    /**
     * wraps an {@link IStorageProcessor} into a {@link IStorageProcessorTransactional} instance if
     * necessary.
     */
    public static IStorageProcessorTransactional wrapIfNecessary(IStorageProcessor storageProcessor)
    {
        if (storageProcessor instanceof IStorageProcessorTransactional)
        {
            return (IStorageProcessorTransactional) storageProcessor;
        }
        return new StorageProcessorTransactionalWrapper(storageProcessor);
    }

    public IStorageProcessorTransaction createTransaction()
    {

        return new AbstractStorageProcessorTransactionalState()
            {
                @Override
                public File doStoreData(DataSetInformation dataSetInformation,
                        ITypeExtractor typeExtractor, IMailClient mailClient,
                        File incomingDataDirectory, File rootDir)
                {
                    return wrappedStorageProcessor.storeData(dataSetInformation, typeExtractor,
                            mailClient, incomingDataDirectory, rootDir);
                }

                @Override
                protected void doCommit()
                {
                    wrappedStorageProcessor.commit(incomingDataSetDirectory, rootDirectory);
                }

                @Override
                protected UnstoreDataAction doRollback(Throwable ex)
                {
                    return wrappedStorageProcessor.rollback(incomingDataSetDirectory,
                            rootDirectory, ex);
                }

            };
    }

    //
    // non-transactional methods (not supported)
    //
    public final File storeData(DataSetInformation dataSetInformation,
            ITypeExtractor typeExtractor,
            IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
    {
        throw new IllegalStateException(
                "This method is deprecated. Please use transactions (see 'createTransaction').");
    }

    public final void commit(File incomingDataSetDirectory, File storedDataDirectory)
    {
        throw new IllegalStateException("You can only call 'commit' on a transaction object "
                + "obtained via the method 'storeDataTransactionally'.");
    }

    public final UnstoreDataAction rollback(File incomingDataSetDirectory,
            File storedDataDirectory,
            Throwable exception)
    {
        throw new IllegalStateException("You can only call 'rollback' on a transaction object "
                + "obtained via the method 'storeDataTransactionally'.");
    }

    //
    // methods delegated to the wrapped storage processor
    //

    public StorageFormat getStorageFormat()
    {
        return wrappedStorageProcessor.getStorageFormat();
    }

    public File tryGetProprietaryData(File storedDataDirectory)
    {
        return wrappedStorageProcessor.tryGetProprietaryData(storedDataDirectory);
    }

    public File getStoreRootDirectory()
    {
        return wrappedStorageProcessor.getStoreRootDirectory();
    }

    public void setStoreRootDirectory(File storeRootDirectory)
    {
        wrappedStorageProcessor.setStoreRootDirectory(storeRootDirectory);
    }

}
