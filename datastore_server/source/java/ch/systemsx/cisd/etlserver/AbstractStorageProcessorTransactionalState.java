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
import java.util.concurrent.atomic.AtomicBoolean;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.IStorageProcessor.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Abstract superclass that has the state necessary for most storage processors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractStorageProcessorTransactionalState implements
        IStorageProcessorTransaction
{

    protected File incomingDataSetDirectory;

    protected File rootDirectory;

    protected File storedDataDirectory;

    private AtomicBoolean isInitialState = new AtomicBoolean(true);

    // ---------------
    // abstract methods to be implemented by extenders
    // --------------

    protected abstract File doStoreData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataDirectory, final File rootDir);

    protected abstract void doCommit();

    protected abstract UnstoreDataAction doRollback(Throwable ex);

    //
    // Default implementation
    //

    public void storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataDirectory, final File rootDir)
    {

        this.incomingDataSetDirectory = incomingDataDirectory;
        this.rootDirectory = rootDir;
        this.storedDataDirectory =
                doStoreData(dataSetInformation, typeExtractor, mailClient, incomingDataDirectory,
                        rootDir);

    }

    public void commit()
    {
        ensureValidState("commit");
        doCommit();
        isInitialState.set(false);
    }

    public UnstoreDataAction rollback(Throwable ex)
    {
        ensureValidState("rollback");
        isInitialState.set(false);
        return doRollback(ex);
    }

    public File getStoredDataDirectory()
    {
        return storedDataDirectory;
    }

    private void ensureValidState(String operation)
    {
        if (false == isInitialState.get())
        {
            String error =
                    String.format("Illegal transaction state: '%s' is not allowed", operation);
            throw new IllegalStateException(error);
        }
    }
}