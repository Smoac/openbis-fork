package ch.systemsx.cisd.etlserver.registrator;

import java.util.List;

import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.api.v1.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

class TestingDataSetHandlerExpectationsAndRealizations
{
    /*
     * shouldas
     */
    protected final boolean shouldRegistrationFail;

    protected final boolean shouldReThrowRollbackException;

    /*
     * happends
     */
    protected boolean didRollbackServiceFunctionRun = false;

    protected boolean didTransactionRollbackHappen = false;

    protected boolean didRollbackTransactionFunctionRunHappen = false;

    protected boolean didCommitTransactionFunctionRunHappen = false;

    protected boolean didSecondaryTransactionErrorNotificationHappen = false;

    protected boolean didPostRegistrationFunctionRunHappen = false;

    protected boolean didPreRegistrationFunctionRunHappen = false;

    protected boolean didPostStorageFunctionRunHappen = false;

    protected boolean didServiceRollbackHappen = false;

    protected String registrationContextError;

    public TestingDataSetHandlerExpectationsAndRealizations(boolean shouldRegistrationFail,
            boolean shouldReThrowRollbackException)
    {
        super();
        this.shouldRegistrationFail = shouldRegistrationFail;
        this.shouldReThrowRollbackException = shouldReThrowRollbackException;
    }

    /**
     * reads boolean or false if null from interpreter
     */
    private static boolean readBoolean(PythonInterpreter interpreter, String variable)
    {
        Boolean retVal = interpreter.get(variable, Boolean.class);
        if (retVal == null)
            return false;
        return retVal;
    }

    public void checkPythonInterpreterVariables(
            DataSetRegistrationService<DataSetInformation> service)
    {
        PythonInterpreter interpreter =
                ((JythonDataSetRegistrationService<DataSetInformation>) service).getInterpreter();

        didRollbackServiceFunctionRun = readBoolean(interpreter, "didRollbackServiceFunctionRun");
        didRollbackTransactionFunctionRunHappen =
                readBoolean(interpreter, "didTransactionRollbackHappen");
        didCommitTransactionFunctionRunHappen =
                readBoolean(interpreter, "didTransactionCommitHappen");
        didPreRegistrationFunctionRunHappen =
                readBoolean(interpreter, "didPreRegistrationFunctionRunHappen");
        didPostRegistrationFunctionRunHappen =
                readBoolean(interpreter, "didPostRegistrationFunctionRunHappen");
        didPostStorageFunctionRunHappen =
                readBoolean(interpreter, "didPostStorageFunctionRunHappen");
        didSecondaryTransactionErrorNotificationHappen =
                readBoolean(interpreter, "didSecondaryTransactionErrorNotificationHappen");

        registrationContextError = interpreter.get("contextTestFailed", String.class);
    }

    public void handleRollbackException(Throwable throwable)
    {
        if (shouldReThrowRollbackException)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } else
        {
            throwable.printStackTrace();
        }
    }
}

public class TestingDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{
    protected final TestingDataSetHandlerExpectationsAndRealizations expectations;

    public TestingDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
            boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
    {
        super(globalState);

        this.expectations =
                new TestingDataSetHandlerExpectationsAndRealizations(shouldRegistrationFail,
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
    protected void invokeFuncion(
            ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService<DataSetInformation> service,
            PyFunction function, Object... args)
    {
        super.invokeFuncion(service, function, args);
        expectations.checkPythonInterpreterVariables(service);
    }

}
