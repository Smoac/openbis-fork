/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor which delegates to a wrapped {@link IStorageProcessorTransactional}. In
 * addition a {@link IPostRegistrationDatasetHandler} handles the data set.
 * <p>
 * The processor uses following properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY}. All the
 * properties are also passed for the default processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public abstract class DelegatingStorageProcessorWithDropbox extends
        AbstractDelegatingStorageProcessor
{

    /**
     * Each transaction is assigned a separate instance of {@link IPostRegistrationDatasetHandler}.
     */
    public abstract IPostRegistrationDatasetHandler createPostRegistrationDataSetHandler();

    protected final Properties properties;

    protected final IFileOperations fileOperations;

    public DelegatingStorageProcessorWithDropbox(Properties properties)
    {
        this(properties, AbstractDelegatingStorageProcessor
                .createDelegateStorageProcessor(properties), FileOperations.getInstance());
    }

    @Private
    DelegatingStorageProcessorWithDropbox(Properties properties,
            IStorageProcessorTransactional delegateStorageProcessor, IFileOperations fileOperations)
    {
        super(delegateStorageProcessor);
        this.properties = properties;
        this.fileOperations = fileOperations;
    }


    //
    // AbstractStorageProcessor
    //

    @Override
    public IStorageProcessorTransaction createTransaction()
    {
        return new StorageProcessorWithDropboxTransaction(super.createTransaction());
    }

    public final class StorageProcessorWithDropboxTransaction extends
            AbstractDelegatingStorageProcessorTransaction
    {
        private final IPostRegistrationDatasetHandler dropboxHandler =
                createPostRegistrationDataSetHandler();

        private StorageProcessorWithDropboxTransaction(IStorageProcessorTransaction transaction)
        {
            super(transaction);
        }

        @Override
        protected File storeData(DataSetInformation dataSetInformation,
                ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            nestedTransaction.storeData(dataSetInformation, typeExtractor, mailClient,
                    incomingDataSetDirectory, rootDirectory);
            File originalData = nestedTransaction.tryGetProprietaryData();
            getPostReigstrationHandler().handle(originalData, dataSetInformation, null);
            return nestedTransaction.getStoredDataDirectory();
        }

        @Override
        protected void executeCommit()
        {
            nestedTransaction.commit();
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            getPostReigstrationHandler().undoLastOperation();
            return nestedTransaction.rollback(ex);
        }

        public IPostRegistrationDatasetHandler getPostReigstrationHandler()
        {
            return dropboxHandler;
        }
    }

}
