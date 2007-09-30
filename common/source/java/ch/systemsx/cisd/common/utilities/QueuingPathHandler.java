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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Asynchronous path handler. Queues tasks and processes them in a separate thread. Use {{@link #terminate()} to clean
 * resources if you do not need the instance of this class anymore.
 * 
 * @author Tomasz Pylak on Aug 24, 2007
 */
public class QueuingPathHandler implements ITerminable, IPathHandler
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, QueuingPathHandler.class);
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, QueuingPathHandler.class);

    private final PathHandlerThread thread;

    private QueuingPathHandler(PathHandlerThread thread)
    {
        this.thread = thread;
    }

    public static QueuingPathHandler create(final IPathHandler handler, String threadName)
    {
        PathHandlerThread thread = new PathHandlerThread(handler);
        final QueuingPathHandler lazyHandler = new QueuingPathHandler(thread);
        thread.setName(threadName);
        thread.start();
        return lazyHandler;
    }

    private static class PathHandlerThread extends Thread
    {
        private final BlockingQueue<File> queue;

        private final IPathHandler handler;

        public PathHandlerThread(IPathHandler handler)
        {
            this.queue = new LinkedBlockingQueue<File>();
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
                        File path = queue.take(); // blocks if empty
                        if (operationLog.isTraceEnabled())
                        {
                            operationLog.trace("Processing path '" + path + "'");
                        }
                        handler.handle(path);
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

        private synchronized void queue(File resource)
        {
            queue.add(resource);
        }

    }

    /** cleans resources */
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
}
