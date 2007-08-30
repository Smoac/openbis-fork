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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;

/**
 * Asynchronous path handler. Queues tasks and processes them in a separate thread. Use {{@link #terminate()} to clean
 * resources if you do not need the instance of this class anymore.
 * 
 * @author Tomasz Pylak on Aug 24, 2007
 */
public class LazyPathHandler implements ITerminable, IPathHandler
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, LazyPathHandler.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, LazyPathHandler.class);

    private final PathHandlerThread thread;

    private LazyPathHandler(PathHandlerThread thread)
    {
        this.thread = thread;
    }

    public static LazyPathHandler create(final IPathHandler handler, String threadName)
    {
        PathHandlerThread thread = new PathHandlerThread(handler);
        final LazyPathHandler lazyHandler = new LazyPathHandler(thread);
        thread.setName(threadName);
        thread.start();
        return lazyHandler;
    }

    private static class PathHandlerThread extends Thread
    {
        private final BlockingQueue<File> queue;

        private final IPathHandler handler;

        private boolean terminate;

        public PathHandlerThread(IPathHandler handler)
        {
            this.queue = new LinkedBlockingQueue<File>();
            this.handler = handler;
            this.terminate = false;
        }

        @Override
        public void run()
        {
            while (terminate == false)
            {
                try
                {
                    File resource = queue.take(); // blocks if empty
                    boolean ok = handler.handle(resource);
                    logHandlingResult(resource, ok);
                } catch (InterruptedException ex)
                {
                    if (!terminate)
                    {
                        operationLog.info("Processing was unexpectedly interrupted. Thread stops.");
                    }
                    return;
                }
            }
        }

        private void logHandlingResult(File resource, boolean ok)
        {
            if (ok)
            {
                operationLog.info("Processing succeded: " + resource.getAbsolutePath());
            } else
            {
                notificationLog.error("Processing failed: " + resource.getAbsolutePath());
            }
        }

        public synchronized void process(File resource)
        {
            queue.add(resource);
        }

        public synchronized void terminate()
        {
            this.terminate = true;
        }

        public synchronized boolean isTerminated()
        {
            return terminate;
        }
    }

    /** cleans resources */
    public boolean terminate()
    {
        thread.terminate();
        thread.interrupt();
        return true;
    }

    /**
     * queues resource processing and exits immediately
     * 
     * @return always true
     */
    public boolean handle(File resource)
    {
        assert thread.isTerminated() == false;
        thread.process(resource);
        return true;
    }
}
