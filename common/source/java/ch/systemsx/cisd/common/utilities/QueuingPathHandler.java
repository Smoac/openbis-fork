/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Asynchronous path handler. Queues tasks and processes them in a separate thread. Use {{@link #terminate()} to clean
 * resources if you do not need the instance of this class anymore.
 * 
 * @author Tomasz Pylak on Aug 24, 2007
 * @author Bernd Rinn
 */
public class QueuingPathHandler implements ITerminable, IPathHandler
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, QueuingPathHandler.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, QueuingPathHandler.class);

    private final PathHandlerThread thread;

    /** An interface for special conditions. */
    public interface ISpecialCondition
    {
        /** Handle the special condition. */
        public void handle();
    }

    private QueuingPathHandler(PathHandlerThread thread)
    {
        this.thread = thread;
    }

    public static QueuingPathHandler create(final IPathHandler handler, String threadName)
    {
        assert handler != null;
        assert threadName != null;

        final PathHandlerThread thread = new PathHandlerThread(handler);
        final QueuingPathHandler lazyHandler = new QueuingPathHandler(thread);
        thread.setName(threadName);
        thread.start();
        return lazyHandler;
    }

    /**
     * A class representing an incident in the {@link QueuingPathHandler}.
     */
    private static class Incident
    {
        private final File path;

        private final ISpecialCondition condition;

        private final boolean blocking;

        Incident(File path)
        {
            this.path = path;
            this.condition = null;
            this.blocking = false;
        }

        Incident(ISpecialCondition condition, boolean blocking)
        {
            this.condition = condition;
            this.blocking = blocking;
            this.path = null;
        }

        File getPath()
        {
            return path;
        }

        boolean isSpecialCondition()
        {
            return (condition != null);
        }

        void handleSpecialCondition()
        {
            assert condition != null;
            condition.handle();
        }

        boolean isBlocking()
        {
            return blocking;
        }
    }

    private static class PathHandlerThread extends Thread
    {
        private final Semaphore specialIncidentSemaphore = new Semaphore(0);

        private final BlockingQueue<Incident> queue;

        private final IPathHandler handler;

        public PathHandlerThread(IPathHandler handler)
        {
            this.queue = new LinkedBlockingQueue<Incident>();
            this.handler = handler;
        }

        @Override
        public void run()
        {
            try
            {
                while (isInterrupted() == false)
                {
                    try
                    {
                        if (operationLog.isTraceEnabled())
                        {
                            operationLog.trace("Waiting for new element in queue.");
                        }
                        final Incident incident = queue.take(); // blocks if empty
                        if (incident.isSpecialCondition())
                        {
                            incident.handleSpecialCondition();
                            if (incident.isBlocking())
                            {
                                specialIncidentSemaphore.release();
                            }
                        } else
                        {
                            final File path = incident.getPath();
                            if (operationLog.isTraceEnabled())
                            {
                                operationLog.trace("Processing path '" + path + "'");
                            }
                            handler.handle(path);
                        }
                    } catch (InterruptedException ex)
                    {
                        return;
                    }
                }
            } catch (Exception ex)
            {
                // Just log it but ensure that the thread won't die.
                notificationLog.error("An exception has occurred. (thread still running)", ex);
            }
        }

        void queue(File resource)
        {
            queue.add(new Incident(resource));
        }

        void queue(ISpecialCondition specialCondition, boolean blocking)
        {
            queue.add(new Incident(specialCondition, blocking));
        }

        synchronized void handleSpecialCondition(ISpecialCondition condition, String nameForLogging, boolean blocking) throws InterruptedException
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Handling special condition '" + nameForLogging + "'.");
            }
            queue(condition, blocking);
            if (blocking)
            {
                specialIncidentSemaphore.acquire();
            }
        }
    }

    /**
     * Interrupts the processing thread.
     */
    public boolean terminate()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Terminating thread '" + thread.getName() + "'");
        }
        thread.interrupt();
        return true;
    }

    /**
     * Queues <var>path</var> processing and exits immediately.
     */
    public void handle(File path)
    {
        assert thread.isInterrupted() == false;

        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Queing path '" + path + "'");
        }
        thread.queue(path);
    }
    
    /**
     * Handles a special condition out of order.
     * 
     * @param specialCondition The condition to handle.
     * @param nameForLogging The name of the condition as written to the log.
     * @param blocking If <code>true</code>, the method will only return when the condition has been handled.
     */
    public void handle(ISpecialCondition specialCondition, String nameForLogging, boolean blocking)
    {
        try
        {
            thread.handleSpecialCondition(specialCondition, nameForLogging, blocking);
        } catch (InterruptedException ex)
        {
            // terminate() has been called.
        }
    }
    
}
