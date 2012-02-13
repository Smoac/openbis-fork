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

package ch.systemsx.cisd.etlserver.registrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;

/**
 * An algorithm that implements the logic running many data set storage algorithms in one logical
 * transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageAlgorithmRunner<T extends DataSetInformation>
{

    public static interface IRollbackDelegate<T extends DataSetInformation>
    {
        /**
         * @param algorithm The algorithm that is rolling back
         * @param ex The throwable that forced the rollback
         * @param errorType The point in the execution of the algorithm that rollback happened
         */
        public void didRollbackStorageAlgorithmRunner(DataSetStorageAlgorithmRunner<T> algorithm,
                Throwable ex, ErrorType errorType);
    }

    /**
     * Interface for code that is run to register a new data set.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IDataSetInApplicationServerRegistrator<T extends DataSetInformation>
    {
        public void registerDataSetsInApplicationServer(List<DataSetRegistrationInformation<T>> data)
                throws Throwable;
    }

    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageAlgorithmRunner.class);

    public static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            "Registration of data set '%s' failed.";

    public static final String DATA_SET_STORAGE_FAILURE_TEMPLATE = "Storing data set '%s' failed.";

    public static final String SUCCESSFULLY_REGISTERED = "Successfully registered data set: [";

    private final ArrayList<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms;

    private final IDataSetInApplicationServerRegistrator<T> applicationServerRegistrator;

    private final IRollbackDelegate<T> rollbackDelegate;

    private final IRollbackStack rollbackStack;

    private final DssRegistrationLogger dssRegistrationLog;

    private final IEncapsulatedOpenBISService openBISService;

    public DataSetStorageAlgorithmRunner(List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            IRollbackDelegate<T> rollbackDelegate,
            IDataSetInApplicationServerRegistrator<T> applicationServerRegistrator,
            IRollbackStack rollbackStack, DssRegistrationLogger dssRegistrationLog,
            IEncapsulatedOpenBISService openBISService)
    {
        this.dataSetStorageAlgorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>(dataSetStorageAlgorithms);
        this.rollbackDelegate = rollbackDelegate;
        this.applicationServerRegistrator = applicationServerRegistrator;
        this.rollbackStack = rollbackStack;
        this.dssRegistrationLog = dssRegistrationLog;
        this.openBISService = openBISService;
    }

    /**
     * Prepare registration of a data set.
     */
    public final void prepare()
    {
        // Log information about the prepare
        StringBuilder registrationSummary = new StringBuilder();
        registrationSummary.append("Prepared registration of ");
        registrationSummary.append(dataSetStorageAlgorithms.size());
        if (1 == dataSetStorageAlgorithms.size())
        {
            registrationSummary.append(" data set:");
        } else
        {
            registrationSummary.append(" data sets:");
        }
        registrationSummary.append("\n\t");

        // Do the prepare
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            IStorageProcessorTransaction transaction = storageAlgorithm.prepare(rollbackStack);
            ITransactionalCommand command = new StorageProcessorTransactionCommand(transaction);
            rollbackStack.pushAndExecuteCommand(command);

            // Collect logging information
            registrationSummary.append(storageAlgorithm.getDataSetInformation().getDataSetCode());
            registrationSummary.append(",");
        }

        registrationSummary.deleteCharAt(registrationSummary.length() - 1);

        dssRegistrationLog.logTruncatingIfNecessary(registrationSummary.toString());
    }

    /**
     * This object will live in the persistent stack of the transaction. In case the server process
     * is killed in the middle of a transaction, the {@link #rollback()} will attempt to rollback
     * the storage processor transaction after restart.
     */
    public static class StorageProcessorTransactionCommand implements ITransactionalCommand
    {

        private static final long serialVersionUID = 1L;

        final IStorageProcessorTransaction transaction;

        StorageProcessorTransactionCommand(IStorageProcessorTransaction transaction)
        {
            this.transaction = transaction;
        }

        public void execute()
        {

        }

        public void rollback()
        {
            transaction.rollback(null);
        }

    }

    /**
     * Register the data sets.
     * 
     * @return true if some data sets were registered
     */
    public boolean runStorageAlgorithms()
    {
        // all algorithms are now in
        // PREPARED STATE

        try
        {
            // move data to precommited directory

            // Runs or throws a throwable
            preCommitStorageAlgorithms();

        } catch (final Throwable throwable)
        {
            rollbackDuringStorageProcessorRun(throwable);
            return false;
        }

        dssRegistrationLog.log("Data has been moved to the pre-commit directory.");

        // PRECOMMITED STATE

        try
        {
            // registers data set with yet non-existing store path.

            // Runs or throw a throwable
            registerDataSetsInApplicationServer();
        } catch (final Throwable throwable)
        {
            rollbackDuringMetadataRegistration(throwable);
            return false;
        }

        dssRegistrationLog.log("Data has been registered with the openBIS Application Server.");

        try
        {
            // Should always succeed
            commitStorageProcessors();

            dssRegistrationLog.log("Storage processors have committed.");

        } catch (final Throwable throwable)
        {
            // Something has gone really wrong
            rollbackAfterStorageProcessorAndMetadataRegistration(throwable);
            return false;
        }

        // COMMITED

        try
        {
            storeCommitedDatasets();

            logSuccessfulRegistration();
            dssRegistrationLog.log("Data has been moved to the final store.");
        } catch (final Throwable throwable)
        {
            // Something has gone really wrong
            return false;
        }

        // move files to the store

        try
        {
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                openBISService.setStorageConfirmed(storageAlgorithm.getDataSetInformation()
                        .getDataSetCode());
            }

            dssRegistrationLog.log("Storage has been confirmed in openBIS Application Server.");
        } catch (final Exception ex)
        {
            // as this case doesn't allow rollbacking, we don't have to catch aggresively (throwables).
            // There is nothing we can do about this at the moment,
            //Graceful recovery should (and will) take care of this case

        }

        return !dataSetStorageAlgorithms.isEmpty();

        // confirm storage in AS

        // STORAGECONFIRMED

    }

    /**
     * @returns true if some datasets have been registered
     */
    public boolean prepareAndRunStorageAlgorithms()
    {
        prepare();
        return runStorageAlgorithms();
    }

    private void rollbackDuringStorageProcessorRun(Throwable ex)
    {
        operationLog.error("Failed to run storage processor");
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.STORAGE_PROCESSOR_ERROR);
    }

    private void rollbackDuringMetadataRegistration(Throwable ex)
    {
        operationLog.error("Failed to register metadata", ex);
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.OPENBIS_REGISTRATION_FAILURE);
    }

    private void rollbackAfterStorageProcessorAndMetadataRegistration(Throwable ex)
    {
        operationLog.error("Failed to complete transaction", ex);
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.POST_REGISTRATION_ERROR);
    }

    /**
     * Committed => Stored
     */
    private void storeCommitedDatasets() throws Throwable
    {
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            storageAlgorithm.moveToTheStore();
        }
    }

    /**
     * Precommitted => Committed
     */
    private void commitStorageProcessors()
    {
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            storageAlgorithm.commitStorageProcessor();
        }
    }

    private void registerDataSetsInApplicationServer() throws Throwable
    {
        ArrayList<DataSetRegistrationInformation<T>> registrationData =
                new ArrayList<DataSetRegistrationInformation<T>>();
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            registrationData.add(new DataSetRegistrationInformation<T>(storageAlgorithm
                    .getDataSetInformation(), storageAlgorithm.createExternalData()));

        }
        applicationServerRegistrator.registerDataSetsInApplicationServer(registrationData);
    }

    /**
     * Prepared => Precommit
     */
    private void preCommitStorageAlgorithms() throws Throwable
    {
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            storageAlgorithm.preCommit();
        }
    }

    private void rollbackStorageProcessors(Throwable ex)
    {
        operationLog.error(
                "Error during dataset registration: " + ExceptionUtils.getRootCauseMessage(ex), ex);

        // Errors which are not AssertionErrors leave the system in a state that we don't
        // know and can't trust. Thus we will not perform any operations any more in this
        // case.
        if (ex instanceof Error && ex instanceof AssertionError == false)
        {
            return;
        }

        // Rollback in the reverse order
        for (int i = dataSetStorageAlgorithms.size() - 1; i >= 0; --i)
        {
            DataSetStorageAlgorithm<T> storageAlgorithm = dataSetStorageAlgorithms.get(i);
            storageAlgorithm.transitionToRolledbackState(ex);
            storageAlgorithm.transitionToUndoneState();
        }
    }

    private void logSuccessfulRegistration()
    {
        if (getOperationLog().isInfoEnabled())
        {
            String msg = getSuccessRegistrationMessage();
            getOperationLog().info(msg);
        }
    }

    private final String getSuccessRegistrationMessage()
    {
        final StringBuilder buffer = new StringBuilder();

        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            buffer.append(SUCCESSFULLY_REGISTERED);
            buffer.append(storageAlgorithm.getSuccessRegistrationMessage());
            buffer.append(']');
        }
        return buffer.toString();
    }

    private Logger getOperationLog()
    {
        return operationLog;
    }
}
