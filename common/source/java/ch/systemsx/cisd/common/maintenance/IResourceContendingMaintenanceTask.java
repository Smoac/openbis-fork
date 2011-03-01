/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.maintenance;

import java.util.Properties;

/**
 * A {@link IMaintenanceTask} that requires an exclusive access to a shared system resource.
 * 
 * @author Kaloyan Enimanev
 */
public interface IResourceContendingMaintenanceTask extends IMaintenanceTask
{

    /**
     * Maintenance tasks can run concurrently and some of the tasks can require exclusive access to
     * a certain system resource (e.g. two tasks altering the contents the same directory).
     * <p>
     * Instead of dealing with such concurrency issues on a case-by-case basis, tasks have the
     * possibility to "reserve" an abstract system resource for the time of their execution. If two
     * maintenance tasks declare they require the same system resource they are never executed
     * simultaneously.
     * <p>
     * The method is executed only *once* per task instance immediately after
     * {@link #setUp(String, Properties)}.
     * <p>
     * NOTE: Currently, maintenance tasks can only declare they need a single resource. Should we
     * discover a use case where a task needs to acquire multiple resources, we can change the
     * interface and implement a more complex synchronization strategy.
     * 
     * @return the name of a resource that the task "locks" for the time of its execution, or
     *         <code>null</code> if no such resources are needed.
     */
    public String getRequiredResourceLock();

}
