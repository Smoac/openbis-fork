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

package ch.systemsx.cisd.etlserver.registrator.v1;

import java.io.File;
import java.util.List;

import org.python.core.PyException;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IJavaDataSetRegistrationDropboxV1;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageAlgorithmRunner.IPrePostRegistrationHook;
import ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler.ProgrammableDropboxObjectFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractProgrammableTopLevelDataSetHandler<T extends DataSetInformation>
        extends AbstractOmniscientTopLevelDataSetRegistrator<T>
{

    /**
     * @param globalState
     */
    protected AbstractProgrammableTopLevelDataSetHandler(
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    @Override
    public abstract boolean shouldNotAddToFaultyPathsOrNull(File file);

    @Override
    abstract protected void handleDataSet(DataSetFile dataSetFile,
            DataSetRegistrationService<T> service) throws Throwable;

    protected void waitUntilApplicationIsReady(DataSetFile incomingDataSetFile)
    {
        while (false == DssRegistrationHealthMonitor.getInstance().isApplicationReady(
                incomingDataSetFile.getRealIncomingFile().getParentFile()))
        {
            waitTheRetryPeriod(10);
            // do nothing. just repeat until the application is ready
        }
    }

    protected void waitTheRetryPeriod(int retryPeriod)
    {
        ConcurrencyUtilities.sleep(retryPeriod * 1000); // in seconds
    }

    @Override
    public void didRollbackTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable ex)
    {
        IJavaDataSetRegistrationDropboxV1<T> v1Dropbox = getV1DropboxProgram();

        try
        {
            v1Dropbox.rollbackTransaction(service, transaction, algorithmRunner, ex);
        } catch (NotImplementedException exc)
        {
            try
            {
                // No Rollback transaction function was called, see if the rollback service
                // function
                // was
                // defined, and call it.
                v1Dropbox.rollbackService(service, ex);
            } catch (NotImplementedException exception)
            {
                // silently ignore if function is not implemented
            }
        }

        super.didRollbackTransaction(service, transaction, algorithmRunner, ex);
    }

    @Override
    public void didCommitTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction)
    {
        try
        {
            getV1DropboxProgram().commitTransaction(service, transaction);
        } catch (NotImplementedException ex)
        {
            // silently ignore if function is not implemented
        }
    }

    @Override
    public void didPreRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        // ignore
    }

    @Override
    public void didPostRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        // ignore
    }

    @Override
    public void didEncounterSecondaryTransactionErrors(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
        try
        {
            getV1DropboxProgram().didEncounterSecondaryTransactionErrors(service, transaction,
                    secondaryErrors);
        } catch (NotImplementedException e)
        {
            // silently ignore if function is not implemented
        }
    }

    /**
     * Set the factory available to the python script. Subclasses may want to override.
     */
    @SuppressWarnings("unchecked")
    protected IDataSetRegistrationDetailsFactory<T> createObjectFactory(
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return (IDataSetRegistrationDetailsFactory<T>) new ProgrammableDropboxObjectFactory<DataSetInformation>(
                getRegistratorState(), userProvidedDataSetInformationOrNull)
            {
                @Override
                protected DataSetInformation createDataSetInformation()
                {
                    return new DataSetInformation();
                }
            };
    }

    /**
     * Create a registration service.
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return createJythonDataSetRegistrationService(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate);
    }

    /**
     * Create a registration service.
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationService(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new DataSetRegistrationService<T>(this, incomingDataSetFile,
                this.createObjectFactory(userProvidedDataSetInformationOrNull),
                cleanAfterwardsAction, delegate);
    }

    protected abstract RecoveryHookAdaptor getRecoveryHookAdaptor(File incoming);

    protected abstract IJavaDataSetRegistrationDropboxV1<T> getV1DropboxProgram();

    interface IRecoveryCleanupDelegate
    {
        void execute(boolean shouldStopRecovery, boolean shouldIncreaseTryCount);
    }

    /**
     * Create an adaptor that offers access to the recovery hook functions.
     */
    protected abstract class RecoveryHookAdaptor implements IPrePostRegistrationHook<T>
    {
        protected abstract IJavaDataSetRegistrationDropboxV2 getV2DropboxProgramInternal();

        protected final File incoming;

        public RecoveryHookAdaptor(File incoming)
        {
            this.incoming = incoming;
        }

        @Override
        public void executePreRegistration(
                DataSetRegistrationContext.IHolder registrationContextHolder)
        {
            throw new NotImplementedException("Recovery cannot execute pre-registration hook.");
        }

        @Override
        public void executePostRegistration(
                DataSetRegistrationContext.IHolder registrationContextHolder)
        {
            try
            {
                getV2DropboxProgramInternal().postMetadataRegistration(
                        registrationContextHolder.getRegistrationContext());
            } catch (NotImplementedException e)
            {
                // ignore
            }
        }

        /**
         * This method does not belong to the IPrePostRegistrationHook interface. Is called directly
         * by recovery.
         */
        public void executePostStorage(DataSetRegistrationContext.IHolder registrationContextHolder)
        {
            try
            {
                getV2DropboxProgramInternal().postStorage(
                        registrationContextHolder.getRegistrationContext());
            } catch (NotImplementedException e)
            {
                // ignore
            }
        }

        public void executePreRegistrationRollback(
                DataSetRegistrationContext.IHolder registrationContextHolder, Throwable throwable)
        {
            try
            {
                getV2DropboxProgramInternal().rollbackPreRegistration(
                        registrationContextHolder.getRegistrationContext(), throwable);
            } catch (NotImplementedException e)
            {
                // ignore
            }
        }
    }

    @Override
    protected Throwable asSerializableException(Throwable throwable)
    {
        if (throwable instanceof PyException)
        {
            return new RuntimeException(throwable.toString());
        }

        return super.asSerializableException(throwable);
    }
}
