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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
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
        public void rollback(DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex);
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

    public DataSetStorageAlgorithmRunner(IEncapsulatedOpenBISService openBisService,
            List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            IRollbackDelegate<T> rollbackDelegate)
    {
        this(dataSetStorageAlgorithms, rollbackDelegate,
                new DefaultApplicationServerRegistrator<T>(openBisService));
    }

    public DataSetStorageAlgorithmRunner(List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            IRollbackDelegate<T> rollbackDelegate,
            IDataSetInApplicationServerRegistrator<T> applicationServerRegistrator)
    {
        this.dataSetStorageAlgorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>(dataSetStorageAlgorithms);
        this.rollbackDelegate = rollbackDelegate;
        this.applicationServerRegistrator = applicationServerRegistrator;
    }

    /**
     * Prepare registration of a data set.
     */
    public final void prepare()
    {
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            storageAlgorithm.prepare();
        }
    }

    /**
     * Register the data sets.
     */
    public List<DataSetInformation> runStorageAlgorithms()
    {
        try
        {
            // Runs or throws a throwable
            runStorageProcessors();

        } catch (final HighLevelException ex)
        {
            rollbackDuringStorageProcessorRun(ex);
            return Collections.emptyList();
        } catch (final Throwable throwable)
        {
            rollbackDuringStorageProcessorRun(throwable);
            return Collections.emptyList();
        }

        try
        {
            // Runs or throw a throwable
            registerDataSetsInApplicationServer();

        } catch (final HighLevelException ex)
        {
            rollbackDuringMetadataRegistration(ex);
            return Collections.emptyList();
        } catch (final Throwable throwable)
        {
            rollbackDuringMetadataRegistration(throwable);
            return Collections.emptyList();
        }

        try
        {
            // Should always succeed
            commitStorageProcessors();

            logSuccessfulRegistration();

            ArrayList<DataSetInformation> dataSetInformationCollection =
                    new ArrayList<DataSetInformation>();
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                dataSetInformationCollection.add(storageAlgorithm.getDataSetInformation());

            }

            return dataSetInformationCollection;
        } catch (final Throwable throwable)
        {
            // Something has gone really wrong
            rollbackAfterStorageProcessorAndMetadataRegistration(throwable);
            return Collections.emptyList();
        }
    }

    public List<DataSetInformation> prepareAndRunStorageAlgorithms()
    {
        prepare();
        return runStorageAlgorithms();
    }

    private void rollbackDuringStorageProcessorRun(Throwable ex)
    {
        rollbackStorageProcessors(ex);
        rollbackDelegate.rollback(this, ex);
    }

    private void rollbackDuringMetadataRegistration(Throwable ex)
    {
        rollbackStorageProcessors(ex);
        rollbackDelegate.rollback(this, ex);
    }

    private void rollbackAfterStorageProcessorAndMetadataRegistration(Throwable ex)
    {
        rollbackStorageProcessors(ex);
        rollbackDelegate.rollback(this, ex);
    }

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

    private void runStorageProcessors() throws Throwable
    {
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            storageAlgorithm.runStorageProcessor();
        }
    }

    private void rollbackStorageProcessors(Throwable ex)
    {
        // Errors which are not AssertionErrors leave the system in a state that we don't
        // know and can't trust. Thus we will not perform any operations any more in this
        // case.
        if (ex instanceof Error && ex instanceof AssertionError == false)
        {
            return;
        }

        // Don't rollback when this exception happens
        boolean stopped = ex instanceof InterruptedExceptionUnchecked;

        // Rollback in the reverse order
        for (int i = dataSetStorageAlgorithms.size() - 1; i >= 0; --i)
        {
            DataSetStorageAlgorithm<T> storageAlgorithm = dataSetStorageAlgorithms.get(i);
            storageAlgorithm.rollbackStorageProcessor(ex);

            if (stopped == false)
            {
                storageAlgorithm.executeUndoStoreAction();
            }
        }
    }

    private static class DefaultApplicationServerRegistrator<T extends DataSetInformation>
            implements IDataSetInApplicationServerRegistrator<T>
    {
        private final IEncapsulatedOpenBISService openBisService;

        DefaultApplicationServerRegistrator(IEncapsulatedOpenBISService openBisService)
        {
            this.openBisService = openBisService;
        }

        public void registerDataSetsInApplicationServer(List<DataSetRegistrationInformation<T>> data)
                throws Throwable
        {
            for (DataSetRegistrationInformation<T> datum : data)
            {
                openBisService.registerDataSet(datum.getDataSetInformation(),
                        datum.getExternalData());
            }
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
