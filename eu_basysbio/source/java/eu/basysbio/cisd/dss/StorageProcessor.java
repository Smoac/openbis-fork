/*
 * Copyright 2009 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.AbstractStrorageProcessorWithUploader;
import ch.systemsx.cisd.etlserver.DelegatingStorageProcessorWithDropbox;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.NullStorageProcessorTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * @author Franz-Josef Elmer
 */
public class StorageProcessor extends DelegatingStorageProcessorWithDropbox
{

    public StorageProcessor(Properties properties)
    {
        super(properties);
    }

    @Override
    public IPostRegistrationDatasetHandler createPostRegistrationDataSetHandler()
    {
        return new DataSetHandler(properties, ServiceProvider.getOpenBISService());
    }

    @Override
    public IStorageProcessorTransaction createTransaction(
            final StorageProcessorTransactionParameters parameters)
    {
        StorageProcessorWithDropboxTransaction superTransaction =
                (StorageProcessorWithDropboxTransaction) super.createTransaction(parameters);

        StorageProcessorWithUploader storageProcessorWithUploader =
                new StorageProcessorWithUploader(new DummyStorageProcessor(),
                        (IDataSetUploader) superTransaction.getPostReigstrationHandler());

        IStorageProcessorTransaction uploaderTransaction =
                storageProcessorWithUploader.createTransaction(parameters);

        return new StorageProcessorTransaction(parameters, superTransaction, uploaderTransaction);

    }

    private static final class StorageProcessorWithUploader extends
            AbstractStrorageProcessorWithUploader
    {
        public StorageProcessorWithUploader(IStorageProcessorTransactional processor,
                IDataSetUploader uploader)
        {
            super(processor, uploader);
        }

        @Override
        protected void logDataSetFileError(File incomingDataSetDirectory, Throwable exception)
        {
        }

    }

    static class StorageProcessorTransaction extends AbstractDelegatingStorageProcessorTransaction
    {

        private static final long serialVersionUID = 1L;

        private final IStorageProcessorTransaction uploaderTransaction;

        StorageProcessorTransaction(StorageProcessorTransactionParameters parameters,
                IStorageProcessorTransaction nestedTransaction,
                IStorageProcessorTransaction uploaderTransaction)
        {
            super(parameters, nestedTransaction);
            this.uploaderTransaction = uploaderTransaction;
        }

        @Override
        protected File executeStoreData(ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            nestedTransaction.storeData(typeExtractor, mailClient, incomingDataSetDirectory);

            uploaderTransaction.storeData(typeExtractor, mailClient, incomingDataSetDirectory);

            return nestedTransaction.getStoredDataDirectory();
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            nestedTransaction.rollback(ex);
            return uploaderTransaction.rollback(ex);
        }

        @Override
        protected void executeCommit()
        {
            uploaderTransaction.commit();
            nestedTransaction.commit();
        }
    }

    private final class DummyStorageProcessor implements IStorageProcessorTransactional
    {
        public File getStoreRootDirectory()
        {
            return null;
        }

        public void setStoreRootDirectory(File storeRootDirectory)
        {
        }

        public IStorageProcessorTransaction createTransaction(
                StorageProcessorTransactionParameters parameters)
        {
            return new NullStorageProcessorTransaction();
        }

        public StorageFormat getStorageFormat()
        {
            return null;
        }

        public UnstoreDataAction getDefaultUnstoreDataAction(Throwable exception)
        {
            return null;
        }
    }

}
