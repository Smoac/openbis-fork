package ch.systemsx.cisd.etlserver.registrator;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

public class TestingDataSetHandlerExpectations
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

    
    
    public boolean isShouldRegistrationFail()
    {
        return shouldRegistrationFail;
    }

    public boolean isShouldReThrowRollbackException()
    {
        return shouldReThrowRollbackException;
    }

    public TestingDataSetHandlerExpectations(boolean shouldRegistrationFail,
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
