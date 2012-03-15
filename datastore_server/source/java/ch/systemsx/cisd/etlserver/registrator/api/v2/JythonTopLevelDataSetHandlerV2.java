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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandlerV2<T extends DataSetInformation> extends
        ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler<T>
{

    /**
     * Constructor.
     * 
     * @param globalState
     */
    public JythonTopLevelDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    /**
     * Create a registration service that includes a python interpreter (we need the interpreter in
     * the service so we can use it in error handling).
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return createJythonDataSetRegistrationServiceV2(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                new PythonInterpreter(), getGlobalState());
    }

    /**
     * Create a Jython registration service that includes access to the interpreter.
     * 
     * @param pythonInterpreter
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationServiceV2(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter pythonInterpreter,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        return new JythonDataSetRegistrationServiceV2<T>(this, incomingDataSetFile,
                userProvidedDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                pythonInterpreter, globalState);
    }

    public static class JythonDataSetRegistrationServiceV2<T extends DataSetInformation>
            extends
            ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService<T>
    {

        public JythonDataSetRegistrationServiceV2(JythonTopLevelDataSetHandlerV2<T> registrator,
                DataSetFile incomingDataSetFile,
                DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter,
                TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(registrator, incomingDataSetFile, userProvidedDataSetInformationOrNull,
                    globalCleanAfterwardsAction, delegate, interpreter, globalState);
        }

        /** Creates the transaction object. Can be overriden in subclasses. */
        @Override
        protected DataSetRegistrationTransaction<T> createTransaction(
                File rollBackStackParentFolder, File workingDir, File stagingDir,
                IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
        {
            if (transactions.isEmpty())
            {
                return new DataSetRegistrationTransaction<T>(rollBackStackParentFolder, workingDir,
                        stagingDir, this, registrationDetailsFactory);
            } else
            {
                throw new IllegalStateException(
                        "Failed to create transaction. Transaction has already been created before.");
            }
        }

        private DataSetRegistrationTransaction<T> getTransaction()
        {
            if (transactions.isEmpty())
            {
                return null;
            } else if (transactions.size() > 1)
            {
                throw new IllegalStateException(
                        "This version of DataSetRegistator doesn't allow multiple transactions, but there are some.");
            } else
            {
                return transactions.get(0);
            }
        }

        /**
         * Commit any scheduled changes.
         */
        @Override
        public void commit()
        {
            DataSetRegistrationTransaction<T> transaction = getTransaction();

            transaction.commit();

            logDssRegistrationResult();

            // Execute the clean afterwards action as successful only if no errors occurred and we
            // registered data sets
            executeGlobalCleanAfterwardsAction(false == (didErrorsArise() || transaction.isRolledback()));
        }
    }

}
