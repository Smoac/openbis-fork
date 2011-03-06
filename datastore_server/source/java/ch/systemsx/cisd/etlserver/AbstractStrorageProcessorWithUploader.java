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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor which uses an {@link IDataSetUploader} after data set has been stored by a
 * wrapped {@link IStorageProcessorTransactional}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractStrorageProcessorWithUploader extends
        AbstractDelegatingStorageProcessor
{
    private final IDataSetUploader uploader;

    public AbstractStrorageProcessorWithUploader(IStorageProcessorTransactional processor,
            IDataSetUploader uploader)
    {
        super(processor);
        this.uploader = uploader;
    }

    /**
     * Creates an instance with a wrapped storage processor which will be created from the specified
     * properties.
     */
    public AbstractStrorageProcessorWithUploader(Properties properties, IDataSetUploader uploader)
    {
        super(properties);
        this.uploader = uploader;
    }

    @Override
    public IStorageProcessorTransaction createTransaction()
    {
        IStorageProcessorTransaction nestedTransaction = super.createTransaction();

        return new AbstractDelegatingStorageProcessorTransaction(nestedTransaction)
            {

                @Override
                protected File storeData(DataSetInformation dataSetInformation,
                        ITypeExtractor typeExtractor, IMailClient mailClient)
                {

                    nestedTransaction.storeData(dataSetInformation, typeExtractor, mailClient,
                            incomingDataSetDirectory, rootDirectory);
                    File storeData = nestedTransaction.getStoredDataDirectory();
                    File originalData = nestedTransaction.tryGetProprietaryData();
                    if (originalData == null)
                    {
                        throw new ConfigurationFailureException(
                                "The original data is no longer available by the wrapped storage processor. "
                                        + "Another storage processor should be used.");
                    }
                    uploader.upload(originalData, dataSetInformation);
                    return storeData;
                }

                @Override
                public UnstoreDataAction executeRollback(Throwable exception)
                {
                    try
                    {
                        nestedTransaction.rollback(exception);
                    } finally
                    {
                        uploader.rollback();
                    }
                    logDataSetFileError(incomingDataSetDirectory, exception);
                    return UnstoreDataAction.LEAVE_UNTOUCHED;
                }

                @Override
                public void executeCommit()
                {
                    nestedTransaction.commit();
                    uploader.commit();
                }

            };

    }

    /**
     * Logs an error for the specified data set and exception.
     */
    protected abstract void logDataSetFileError(File incomingDataSetDirectory, Throwable exception);

}
