/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.server.queue;

import java.io.StringWriter;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;

import org.apache.log4j.Logger;

/*
 * This class models a simple queue with tasks and starts a thread that consumes the tasks.
 */
public final class ConsumerQueue
{
    public ConsumerQueue(MailClientParameters mailClientParameters)
    {
        this.mailClientParameters = mailClientParameters;
    }

    public final synchronized void addTaskAsLast(ConsumerTask task)
    {
        consumerQueue.addLast(task);
    }

    private final synchronized ConsumerTask getNextTask()
    {
        return consumerQueue.pollFirst();
    }

    private final Deque<ConsumerTask> consumerQueue = new LinkedList<ConsumerTask>();

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ConsumerQueue.class);

    private final MailClientParameters mailClientParameters;

    // Consumer Thread
    // {
    // Thread consumerThread = new Thread() {
    //
    // @Override
    // public void run() {
    // while(true) {
    // StringWriter writer = new StringWriter();
    // boolean success = true;
    // Date startDate = new Date();
    // ConsumerTask consumerTask = null;
    // try {
    // consumerTask = getNextTask();
    // if(consumerTask != null) {
    // success = consumerTask.doAction(writer);
    // } else {
    // Thread.sleep(1000 * 5);
    // }
    // } catch(Throwable anyError) {
    // operationLog.error("Asynchronous action '" + consumerTask.getName() + "' failed. ", anyError);
    // success = false;
    // } finally {
    // if(consumerTask != null) {
    // try {
    // final IMailClient mailClient = new MailClient(mailClientParameters);
    // sendEmail(mailClient, writer.toString(), getSubject(consumerTask.getName(), startDate, success), consumerTask.getUserEmail());
    // } catch(Throwable anyErrorOnMail) {
    // operationLog.error("Could not send email about asynchronous action '" + consumerTask.getName() + "' result. ", anyErrorOnMail);
    // }
    // }
    // }
    // }
    // }
    // };
    // consumerThread.start();
    // }

    {
        Thread consumerThread = new Thread()
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        final ConsumerTask consumerTask = getNextTask();
                        try
                        {
                            if (consumerTask != null)
                            {
                                Thread thread = new Thread()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            boolean success = true;
                                            StringWriter writer = new StringWriter();
                                            Date startDate = new Date();
                                            try
                                            {
                                                consumerTask.doAction(writer);
                                            } catch (Throwable anyError)
                                            {
                                                operationLog.error("Asynchronous action '" + consumerTask.getName() + "' failed. ", anyError);
                                                success = false;
                                            } finally
                                            {
                                                if (consumerTask != null)
                                                {
                                                    try
                                                    {
                                                        final IMailClient mailClient = new MailClient(mailClientParameters);
                                                        sendEmail(mailClient, writer.toString(),
                                                                getSubject(consumerTask.getName(), startDate, success), consumerTask.getUserEmail());
                                                    } catch (Throwable anyErrorOnMail)
                                                    {
                                                        operationLog.error(
                                                                "Could not send email about asynchronous action '" + consumerTask.getName()
                                                                        + "' result. ", anyErrorOnMail);
                                                    }
                                                }
                                            }
                                        }
                                    };
                                thread.start();
                            } else
                            {
                                Thread.sleep(1000 * 5);
                            }
                        } catch (Throwable anyError)
                        {
                            operationLog.error("Asynchronous action '" + consumerTask.getName() + "' failed. ", anyError);
                        }
                    }
                }
            };
        consumerThread.start();
    }

    //
    // Mail management
    //
    private void sendEmail(IMailClient mailClient, String content, String subject,
            String... recipient)
    {
        mailClient.sendMessage(subject, content, null, null, recipient);
    }

    private String getSubject(String actionName, Date startDate, boolean success)
    {
        return addDate(actionName + " " + (success ? "successfully performed" : "failed"),
                startDate);
    }

    private String addDate(String subject, Date startDate)
    {
        return subject + " (initiated at " + startDate + ")";
    }
}
