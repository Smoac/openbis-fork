package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.queue;

import java.util.Deque;
import java.util.LinkedList;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/*
 * This class models a simple queue with tasks and starts a thread that consumes the tasks.
 */
public final class ConsumerQueue
{
    private static final Deque<ConsumerTask> consumerQueue = new LinkedList<ConsumerTask>();
    private static final Logger trackingLog = LogFactory.getLogger(LogCategory.TRACKING, ConsumerQueue.class);
    
    public static final synchronized void addTaskAsLast(ConsumerTask task) {
        consumerQueue.addLast(task);
    }
    
    private static final synchronized ConsumerTask getNextTask() {
        return consumerQueue.pollFirst();
    }
    
    // Consumer Thread
    static {
        Thread consumerThread = new Thread() {
            
            @Override
            public void run() {
                 while(true) {
                     //We start with a null task for save coding
                     ConsumerTask consumerTask = new ConsumerTask() {
                        @Override
                        public String getTaskName() { return "Null Task"; }

                        @Override
                        public void executeTask() {}
                     };
                     
                     try {
                         consumerTask = getNextTask();
                         if(consumerTask != null) {
                             consumerTask.executeTask();
                         } else {
                             Thread.sleep(1000 * 5);
                         }
                     } catch(Throwable anyError) {
                         trackingLog.log(Level.ERROR, consumerTask.getTaskName(),  anyError);
                     }
                 }
            }
        };
        consumerThread.start();
    }
}
