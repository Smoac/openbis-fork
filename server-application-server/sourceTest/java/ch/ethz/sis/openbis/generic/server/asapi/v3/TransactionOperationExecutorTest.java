package ch.ethz.sis.openbis.generic.server.asapi.v3;

import org.testng.annotations.Test;

public class TransactionOperationExecutorTest
{

    @Test
    public void test()
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(new ITransactionOperationContext()
        {
            @Override public Object getTransaction(final String transactionId) throws Exception
            {
                return null;
            }

            @Override public void prepareTransaction(final String transactionId, final Object transaction) throws Exception
            {

            }

            @Override public void rollbackTransaction(final String transactionId, final Object transaction) throws Exception
            {

            }

            @Override public void commitTransaction(final String transactionId, final Object transaction) throws Exception
            {

            }
        });
    }
}
