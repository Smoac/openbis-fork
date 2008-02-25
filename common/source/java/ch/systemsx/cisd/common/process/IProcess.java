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

package ch.systemsx.cisd.common.process;

/**
 * A <code>Runnable</code> extension that describes a process.
 * 
 * @author Christian Ribeaud
 */
public interface IProcess extends Runnable
{

    /**
     * Whether this <code>IProcess</code> exited successfully.
     * <p>
     * Is typically called after {@link Runnable#run()} has performed.
     * </p>
     * 
     * @return <code>true</code> if this <code>IProcess</code> succeeds, terminating so the whole running process.
     */
    public boolean succeeded();

    /**
     * The number of times we should try if this <code>IProcess</code> failed (including the first excecution).
     * <p>
     * This is a static method: it only gets called once during the initialization process.
     * </p>
     */
    public int getMaxRetryOnFailure();

    /**
     * The number of milliseconds we should wait before re-executing {@link Runnable#run()}.
     * <p>
     * This is a static method: it only gets called once during the initialization process.
     * </p>
     */
    public long getMillisToSleepOnFailure();
}
