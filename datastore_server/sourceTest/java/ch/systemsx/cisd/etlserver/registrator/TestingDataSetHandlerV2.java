package ch.systemsx.cisd.etlserver.registrator;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonTopLevelDataSetHandlerV2;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

public class TestingDataSetHandlerV2 extends JythonTopLevelDataSetHandlerV2<DataSetInformation>
        implements ITestingDataSetHandler
{
    protected final TestingDataSetHandlerExpectations expectations;

    public TestingDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState,
            boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
    {
        super(globalState, DataSetInformation.class);

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
