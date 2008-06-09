/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that gives (non-standard) names to new threads. If a name is a
 * {@link NamedRunnable}, the name provided by {@link NamedRunnable#getRunnableName()} will be
 * used, otherwise the <var>defaultName</var>. The thread count (number of already created threads
 * in this factory) will always be appended.
 * 
 * @author Bernd Rinn
 */
public class NamingThreadFactory implements ThreadFactory
{

    private final String poolName;

    private boolean createDaemonThreads;
    
    private boolean addPoolName;
    
    private int threadCount;

    public NamingThreadFactory(String poolName)
    {
        this.poolName = poolName;
        this.addPoolName = true;
        this.createDaemonThreads = false;
        this.threadCount = 0;
    }

    public Thread newThread(Runnable r)
    {
        ++threadCount;
        final String completePoolName = poolName + "-T" + threadCount;
        final Thread thread = new PoolNameThread(r, completePoolName, addPoolName);
        thread.setDaemon(createDaemonThreads);
        return thread;
    }

    String getPoolName()
    {
        return poolName;
    }

    public final boolean isCreateDaemonThreads()
    {
        return createDaemonThreads;
    }

    public final void setCreateDaemonThreads(boolean createDaemonThreads)
    {
        this.createDaemonThreads = createDaemonThreads;
    }

    public final int getThreadCount()
    {
        return threadCount;
    }

    public final boolean isAddPoolName()
    {
        return addPoolName;
    }

    public final void setAddPoolName(boolean addPoolName)
    {
        this.addPoolName = addPoolName;
    }
}
