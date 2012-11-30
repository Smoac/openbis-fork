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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.io.File;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.jython.PythonInterpreter;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.recovery.AutoRecoverySettings;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractProgrammableTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class JythonDataSetRegistrationServiceV2<T extends DataSetInformation> extends
        JythonTopLevelDataSetHandlerV2.JythonDataSetRegistrationService<T>
{

    public JythonDataSetRegistrationServiceV2(
            AbstractProgrammableTopLevelDataSetHandler<T> registrator,
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(registrator, incomingDataSetFile, userProvidedDataSetInformationOrNull,
                globalCleanAfterwardsAction, delegate, interpreter, globalState);
    }

    /**
     * The method creates a new dataset registration transaction. The
     * JythonDataSetRegistrationServiceV2 guarantees that this transatcion will be created only
     * once. To be extended in subclasses.
     */
    protected DataSetRegistrationTransaction<T> createV2DatasetRegistrationTransaction(
            File rollBackStackParentFolder, File workingDir, File stagingDir,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        return new DataSetRegistrationTransaction<T>(rollBackStackParentFolder, workingDir,
                stagingDir, this, registrationDetailsFactory,
                AutoRecoverySettings.USE_AUTO_RECOVERY);
    }

    public DataSetRegistrationTransaction<T> getTransaction()
    {
        return transaction;
    }

    /**
     * rolls back the existing transaction
     */
    public void rollbackAndForgetTransaction()
    {
        if (transaction != null)
        {
            transaction.rollback();
            transaction = null;
        }
    }

    /**
     * Commit any scheduled changes.
     */
    @Override
    public void commit()
    {
        transaction.commit();

        logDssRegistrationResult();

        // Execute the clean afterwards action as successful only if no errors occurred and we
        // registered data sets
        executeGlobalCleanAfterwardsAction(false == (didErrorsArise() || transaction.isRolledback()));
    }

    @Override
    protected void logDssRegistrationResult()
    {
        // If the transaction is not in recovery pending state, do the normal logging
        if (false == transaction.isRecoveryPending())
        {
            super.logDssRegistrationResult();
            return;
        }

        // Log that we are in recovery pending state
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Encountered errors, trying to recover.\n");
        for (Throwable error : getEncounteredErrors())
        {
            logMessage.append("\t");
            logMessage.append(error.toString());
        }
        dssRegistrationLog.log(logMessage.toString());
    }
}
