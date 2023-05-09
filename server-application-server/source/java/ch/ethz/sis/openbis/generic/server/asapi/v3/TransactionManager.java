package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

@Component
public class TransactionManager implements ITransactionManager
{

    private static final String APPLICATION_SERVER_URL = "http://127.0.0.1:8888";

    private static final String SECRET = "i_am_secret";

    private static final long TIMEOUT = 10000;

    private IApplicationServerApiWithTransactions applicationServerApi;

    @PostConstruct
    public void init()
    {
        applicationServerApi = HttpInvokerUtils.createServiceStub(IApplicationServerApiWithTransactions.class,
                APPLICATION_SERVER_URL + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL, TIMEOUT,
                new InvocationFactoryWithTransactionAttributes());
    }

    @Override public void beginTransaction(final String transactionId)
    {
        applicationServerApi.beginTransaction(transactionId);
    }

    @Override public void commitTransaction(final String transactionId)
    {
        applicationServerApi.commitTransaction(transactionId);
    }

    @Override public void rollbackTransaction(final String transactionId)
    {
        applicationServerApi.rollbackTransaction(transactionId);
    }

    @Override public int getMajorVersion()
    {
        return 1;
    }

    @Override public int getMinorVersion()
    {
        return 0;
    }

    private static class InvocationFactoryWithTransactionAttributes extends DefaultRemoteInvocationFactory
    {
        @Override public RemoteInvocation createRemoteInvocation(final MethodInvocation methodInvocation)
        {
            String methodName = methodInvocation.getMethod().getName();

            if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(methodName) || TransactionConst.COMMIT_TRANSACTION_METHOD.equals(
                    methodName) || TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(methodName))
            {
                Map<String, Serializable> attributes = new HashMap<>();
                attributes.put(TransactionConst.TRANSACTION_ID_ATTRIBUTE, (String) methodInvocation.getArguments()[0]);
                attributes.put(TransactionConst.TRANSACTION_MANAGER_SECRET_ATTRIBUTE, SECRET);

                RemoteInvocation remoteInvocation = super.createRemoteInvocation(methodInvocation);
                remoteInvocation.setAttributes(attributes);
                return remoteInvocation;
            } else
            {
                throw new IllegalArgumentException(
                        "Only transaction management calls are allowed. Tried to call " + methodName + " method.");
            }
        }
    }

    private interface IApplicationServerApiWithTransactions extends IApplicationServerApi
    {
        void beginTransaction(String transactionId);

        void commitTransaction(String transactionId);

        void rollbackTransaction(String transactionId);
    }

}
