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

/**
 * A {@link Thread} that knows its pool name.
 * 
 * @author Bernd Rinn
 */
public class PoolNameThread extends Thread
{
    private final String poolName;

    public PoolNameThread(Runnable target, String poolName)
    {
        super(target, poolName);
        this.poolName = poolName;
    }

    /**
     * Sets the thread's name to be a combination of the name of the thread in the pool and the name
     * of the runnable, separated by '::'.
     */
    public void setRunnableName(String runnableName)
    {
        setName(poolName + "::" + runnableName);
    }

    /** Clears the name of the runnable, setting the name of the thread to the pool name. */
    public void clearRunnableName()
    {
        setName(poolName);
    }
}
