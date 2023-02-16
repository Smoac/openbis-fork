/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

public class TestingDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
        implements ITestingDataSetHandler
{
    protected final TestingDataSetHandlerExpectations expectations;

    public TestingDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
            boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
    {
        super(globalState);

        this.expectations =
                new TestingDataSetHandlerExpectations(shouldRegistrationFail,
                        shouldReThrowRollbackException);
    }

    @Override
    public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
            NewExternalData data) throws Throwable
    {
        if (expectations.shouldRegistrationFail)
        {
            throw new UserFailureException("Didn't work.");
        } else
        {
            super.registerDataSetInApplicationServer(dataSetInformation, data);
        }
    }

    @Override
    public void rollback(DataSetRegistrationService<DataSetInformation> service, Throwable throwable)
    {
        super.rollback(service, throwable);
        expectations.didServiceRollbackHappen = true;
        expectations.handleRollbackException(throwable);
    }

    @Override
    public void didRollbackTransaction(DataSetRegistrationService<DataSetInformation> service,
            DataSetRegistrationTransaction<DataSetInformation> transaction,
            DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner, Throwable throwable)
    {
        super.didRollbackTransaction(service, transaction, algorithmRunner, throwable);

        expectations.didTransactionRollbackHappen = true;

        expectations.handleRollbackException(throwable);
    }

    @Override
    public TestingDataSetHandlerExpectations getExpectations()
    {
        return expectations;
    }

}
