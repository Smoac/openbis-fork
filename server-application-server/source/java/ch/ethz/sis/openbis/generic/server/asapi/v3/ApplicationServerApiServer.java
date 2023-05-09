/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;

/**
 * @author Franz-Josef Elmer
 */
@Controller
public class ApplicationServerApiServer extends AbstractApiServiceExporter
{

    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final Map<String, TransactionThread> threadMap = new HashMap<>();

    @Override
    public void afterPropertiesSet()
    {
        establishService(IApplicationServerApi.class, service, IApplicationServerApi.SERVICE_NAME,
                IApplicationServerApi.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
            { IApplicationServerApi.SERVICE_URL, "/openbis" + IApplicationServerApi.SERVICE_URL,
                    "/openbis/openbis" + IApplicationServerApi.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }

    @Override protected Object invoke(final RemoteInvocation invocation, final Object targetObject)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        String transactionId = (String) invocation.getAttribute(TransactionConst.TRANSACTION_ID_ATTRIBUTE);

        if (transactionId != null)
        {
            TransactionThread thread;

            synchronized (this)
            {
                logger.info("Two phase transaction id: " + transactionId);

                thread = threadMap.get(transactionId);

                if (thread == null)
                {
                    if (threadMap.size() >= TransactionConst.THREAD_COUNT_LIMIT)
                    {
                        throw new RuntimeException("Too many two phase transaction threads running");
                    }

                    logger.info("Creating two phase transaction thread");

                    thread = new TransactionThread(transactionId);
                    threadMap.put(transactionId, thread);
                    thread.start();
                } else
                {
                    logger.info("Found existing two phase transaction thread");
                }
            }

            try
            {
                return thread.execute(invocation);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            } finally
            {
                synchronized (this)
                {
                    if (!thread.isAlive())
                    {
                        logger.info("Two phase transaction " + transactionId + " finished. Removing its thread.");
                        threadMap.remove(transactionId);
                    }
                }
            }
        } else
        {
            return super.invoke(invocation, targetObject);
        }
    }

    class TransactionThread
    {

        private final Object lock = new Object();

        private final Thread thread;

        private final String transactionId;

        private TransactionStatus transaction;

        private RemoteInvocation invocation;

        private Object result;

        private Exception exception;

        public TransactionThread(String transactionId)
        {
            this.transactionId = transactionId;
            this.thread = new Thread(new Runnable()
            {
                @Override public void run()
                {
                    synchronized (lock)
                    {
                        while (true)
                        {
                            try
                            {
                                if (invocation != null)
                                {
                                    if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(invocation.getMethodName()))
                                    {
                                        checkTransactionNotStartedYet();
                                        checkTransactionManagerSecret(invocation);
                                        transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
                                    } else if (TransactionConst.COMMIT_TRANSACTION_METHOD.equals(invocation.getMethodName()))
                                    {
                                        checkTransactionAlreadyStarted();
                                        checkTransactionManagerSecret(invocation);
                                        transactionManager.commit(transaction);
                                    } else if (TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(invocation.getMethodName()))
                                    {
                                        checkTransactionAlreadyStarted();
                                        checkTransactionManagerSecret(invocation);
                                        transactionManager.rollback(transaction);
                                    } else
                                    {
                                        checkTransactionAlreadyStarted();
                                        result = invocation.invoke(service);
                                    }

                                    logger.info("Two phase transaction " + transactionId + " method " + invocation.getMethodName()
                                            + " executed.");

                                    exception = null;
                                    invocation = null;
                                    lock.notifyAll();

                                    if (transaction.isCompleted())
                                    {
                                        return;
                                    }
                                } else
                                {
                                    logger.info("Two phase transaction " + transactionId + " thread waiting for the next call.");
                                    lock.wait();
                                }

                            } catch (Exception e)
                            {
                                logger.error(
                                        "Two phase transaction " + transactionId + " method " + (invocation != null ? invocation.getMethodName() : "")
                                                + " failed or got interrupted.",
                                        e);

                                if (transaction != null)
                                {
                                    transactionManager.rollback(transaction);
                                }

                                exception = e;
                                result = null;
                                invocation = null;
                                lock.notifyAll();

                                return;
                            }
                        }
                    }
                }

                private void checkTransactionNotStartedYet()
                {
                    if (transaction != null)
                    {
                        throw new IllegalStateException(
                                "Two phase transaction " + transactionId + " has been already started.");
                    }
                }

                private void checkTransactionAlreadyStarted()
                {
                    if (transaction == null)
                    {
                        throw new IllegalStateException("Two phase transaction " + transactionId + " hasn't been started yet.");
                    }
                }

                private void checkTransactionManagerSecret(final RemoteInvocation invocation)
                {
                    String secret = (String) invocation.getAttribute(TransactionConst.TRANSACTION_MANAGER_SECRET_ATTRIBUTE);

                    if (secret == null || secret.isBlank())
                    {
                        throw new IllegalStateException("Two phase transaction manager secret missing.");
                    }
                }
            });
        }

        public void start()
        {
            thread.start();
        }

        public boolean isAlive()
        {
            return thread.isAlive();
        }

        public Object execute(RemoteInvocation newInvocation) throws Exception
        {
            synchronized (lock)
            {
                try
                {
                    if (invocation != null)
                    {
                        throw new IllegalStateException(
                                "Cannot schedule another two phase transaction " + transactionId + " call as the previous execution for method "
                                        + invocation.getMethodName()
                                        + " hasn't finished yet.");
                    }

                    invocation = newInvocation;
                    result = null;
                    exception = null;
                    lock.notifyAll();

                    logger.info("Two phase transaction " + transactionId + " method " + newInvocation.getMethodName() + " call scheduled.");

                    while (invocation != null)
                    {
                        lock.wait();
                    }

                } catch (Exception e)
                {
                    logger.error(
                            "Scheduling of the next two phase transaction method " + invocation.getMethodName() + " call failed or got interrupted.",
                            e);
                    throw e;
                }

                if (exception != null)
                {
                    throw exception;
                } else
                {
                    return result;
                }
            }
        }
    }

}
