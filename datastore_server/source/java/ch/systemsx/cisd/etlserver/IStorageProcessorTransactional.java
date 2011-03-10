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
 * A storage processor that returns an object with the state necessary to commit or rollback.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IStorageProcessorTransactional extends IStoreRootDirectoryHolder
{
    /** Properties key prefix to find the {@link IStorageProcessorTransactional} implementation. */
    public static final String STORAGE_PROCESSOR_KEY = "storage-processor";

    /**
     * Instructs the dataset handler what to do with the data in incoming directory if there was an
     * error during registration in openbis.
     */
    public enum UnstoreDataAction
    {
        /**
         * moved the data to the error directory
         */
        MOVE_TO_ERROR,
        /**
         * leave the data in the incoming directory
         */
        LEAVE_UNTOUCHED,
        /**
         * delete the data from the incoming directory
         */
        DELETE
    }

    public static interface IStorageProcessorTransaction
    {
        /**
         * Stores the specified incoming data set file to the specified directory. In general some
         * processing and/or transformation of the incoming data takes place.
         * <p>
         * Do not try/catch exceptions that could occur here. Preferably let the upper layer handle
         * them.
         * </p>
         * 
         * @param dataSetInformation Information about the data set.
         * @param typeExtractor the {@link ITypeExtractor} implementation.
         * @param mailClient mail client.
         * @param incomingDataSetDirectory folder to store. Do not remove it after the
         *            implementation has finished processing. {@link TransferredDataSetHandler}
         *            takes care of this.
         * @param rootDir directory to whom the data will be stored.
         */
        public void storeData(final DataSetInformation dataSetInformation,
                final ITypeExtractor typeExtractor, final IMailClient mailClient,
                final File incomingDataSetDirectory, final File rootDir);

        /**
         * Commits the changes done by the recent
         * {@link #storeData(DataSetInformation, ITypeExtractor, IMailClient, File, File)} call if
         * the dataset has been also successfully registered openBIS.
         * <p>
         * This operation is useful when the storage processor adds the data to an additional
         * database. If all the storage processor operations are done on the file system, the
         * implementation of this method will be usually empty.
         * </p>
         */
        public void commit();

        /**
         * Performs a rollback of
         * {@link #storeData(DataSetInformation, ITypeExtractor, IMailClient, File, File)} The data
         * created in <code>directory</code> will also be removed.
         * <p>
         * Call to this method is safe as implementations should try/catch exceptions that could
         * occur here.
         * </p>
         * 
         * @param exception an exception which has caused that the unstore operation has to be
         *            performed
         * @return an instruction what to do with the data in incoming directory
         */
        public UnstoreDataAction rollback(Throwable exception);

        /**
         * Returns the directory where the data set is stored.
         */
        public File getStoredDataDirectory();

        /**
         * Returns the data set in the original proprietary format (before being processed) if
         * available, or <code>null</code>, if the original data set is no longer available.
         * <p>
         * <strong>Consider the data in the returned file / directory read only!</strong>
         */
        public File tryGetProprietaryData();
    }

    /**
     * Create a new {@link IStorageProcessorTransaction} object.
     */
    public IStorageProcessorTransaction createTransaction();

    /**
     * Returns the format that this storage processor is storing data sets in.
     */
    public StorageFormat getStorageFormat();

    /**
     * Return the action to take when an exception occurs. This might be called outside the context
     * of a transaction.
     * 
     * @param exception an exception which has caused that the unstore operation has to be performed
     * @return an instruction what to do with the data in incoming directory
     */
    public UnstoreDataAction getDefaultUnstoreDataAction(Throwable exception);

}
