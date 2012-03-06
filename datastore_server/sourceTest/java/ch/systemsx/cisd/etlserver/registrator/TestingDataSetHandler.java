package ch.systemsx.cisd.etlserver.registrator;

import java.util.List;

import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

public class TestingDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{
    protected final boolean shouldRegistrationFail;

    protected final boolean shouldReThrowRollbackException;

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

    public TestingDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
            boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
    {
        super(globalState);
        this.shouldRegistrationFail = shouldRegistrationFail;
        this.shouldReThrowRollbackException = shouldReThrowRollbackException;
    }

    @Override
    public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
            NewExternalData data) throws Throwable
    {
        if (shouldRegistrationFail)
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
        didServiceRollbackHappen = true;
        if (shouldReThrowRollbackException)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } else
        {
            throwable.printStackTrace();
        }
    }

    @Override
    public void didRollbackTransaction(DataSetRegistrationService<DataSetInformation> service,
            DataSetRegistrationTransaction<DataSetInformation> transaction,
            DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner, Throwable throwable)
    {
        super.didRollbackTransaction(service, transaction, algorithmRunner, throwable);

        didTransactionRollbackHappen = true;
        if (shouldReThrowRollbackException)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } else
        {
            throwable.printStackTrace();
        }
    }

    @Override
    protected void invokeRollbackServiceFunction(PyFunction function,
            DataSetRegistrationService<DataSetInformation> service, Throwable throwable)
    {
        super.invokeRollbackServiceFunction(function, service, throwable);
        PythonInterpreter interpreter =
                ((JythonDataSetRegistrationService<DataSetInformation>) service).getInterpreter();
        didRollbackServiceFunctionRun = readBoolean(interpreter, "didRollbackServiceFunctionRun");
    }

    @Override
    protected void invokeRollbackTransactionFunction(PyFunction function,
            DataSetRegistrationService<DataSetInformation> service,
            DataSetRegistrationTransaction<DataSetInformation> transaction,
            DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner, Throwable throwable)
    {
        super.invokeRollbackTransactionFunction(function, service, transaction, algorithmRunner,
                throwable);

        PythonInterpreter interpreter =
                ((JythonDataSetRegistrationService<DataSetInformation>) service).getInterpreter();
        didRollbackTransactionFunctionRunHappen =
                readBoolean(interpreter, "didTransactionRollbackHappen");
    }

    @Override
    protected void invokeServiceTransactionFunction(PyFunction function,
            DataSetRegistrationService<DataSetInformation> service,
            DataSetRegistrationTransaction<DataSetInformation> transaction)
    {
        super.invokeServiceTransactionFunction(function, service, transaction);

        PythonInterpreter interpreter =
                ((JythonDataSetRegistrationService<DataSetInformation>) service).getInterpreter();
        didCommitTransactionFunctionRunHappen =
                readBoolean(interpreter, "didTransactionCommitHappen");
    }

    @Override
    protected void invokeTransactionFunctionWithContext(PyFunction function,
            DataSetRegistrationService<DataSetInformation> service,
            DataSetRegistrationTransaction<DataSetInformation> transaction)
    {
        super.invokeTransactionFunctionWithContext(function, service, transaction);
        PythonInterpreter interpreter =
                ((JythonDataSetRegistrationService<DataSetInformation>) service).getInterpreter();

        didPreRegistrationFunctionRunHappen =
                readBoolean(interpreter, "didPreRegistrationFunctionRunHappen");

        didPostRegistrationFunctionRunHappen =
                readBoolean(interpreter, "didPostRegistrationFunctionRunHappen");

        didPostStorageFunctionRunHappen =
                readBoolean(interpreter, "didPostStorageFunctionRunHappen");

        registrationContextError = interpreter.get("contextTestFailed", String.class);
    }

    /**
     * reads boolean or false if null from interpreter
     */
    protected boolean readBoolean(PythonInterpreter interpreter, String variable)
    {
        Boolean retVal = interpreter.get(variable, Boolean.class);
        if (retVal == null)
            return false;
        return retVal;
    }

    @Override
    public void didEncounterSecondaryTransactionErrors(
            DataSetRegistrationService<DataSetInformation> service,
            DataSetRegistrationTransaction<DataSetInformation> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
        super.didEncounterSecondaryTransactionErrors(service, transaction, secondaryErrors);

        PythonInterpreter interpreter =
                ((JythonDataSetRegistrationService<DataSetInformation>) service).getInterpreter();
        didSecondaryTransactionErrorNotificationHappen =
                readBoolean(interpreter, "didSecondaryTransactionErrorNotificationHappen");
    }
}
