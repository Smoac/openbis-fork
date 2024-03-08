package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import ch.ethz.sis.transaction.AbstractTransactionNode;

public abstract class AbstractTransactionNodeApi implements ApplicationListener<ApplicationEvent>
{

    private static final String FINISH_TRANSACTIONS_THREAD_NAME = "finish-transactions";

    private final TransactionConfiguration transactionConfiguration;

    public AbstractTransactionNodeApi(final TransactionConfiguration transactionConfiguration)
    {
        this.transactionConfiguration = transactionConfiguration;
    }

    protected abstract AbstractTransactionNode<?> getTransactionNode();

    @Override public void onApplicationEvent(final ApplicationEvent event)
    {
        Object source = event.getSource();
        if (source instanceof AbstractApplicationContext)
        {
            AbstractApplicationContext appContext = (AbstractApplicationContext) source;
            if ((event instanceof ContextStartedEvent) || (event instanceof ContextRefreshedEvent))
            {
                if (appContext.getParent() != null)
                {
                    final AbstractTransactionNode<?> transactionNode = getTransactionNode();

                    transactionNode.recoverTransactionsFromTransactionLog();

                    new Timer(FINISH_TRANSACTIONS_THREAD_NAME, true).schedule(new TimerTask()
                                                                              {
                                                                                  @Override public void run()
                                                                                  {
                                                                                      transactionNode.finishFailedOrAbandonedTransactions();
                                                                                  }
                                                                              },
                            transactionConfiguration.getFinishTransactionsIntervalInSeconds() * 1000L,
                            transactionConfiguration.getFinishTransactionsIntervalInSeconds() * 1000L);
                }
            }
        }
    }

}
