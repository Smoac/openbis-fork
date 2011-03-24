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

package ch.systemsx.cisd.etlserver.postregistration;

import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * Interface of a task executed after registration of a data set. Implementing class should have a
 * public constructor with two parameters: First is of type {@link Properties} and second is of type
 * {@link IEncapsulatedOpenBISService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPostRegistrationTask
{
    /**
     * Returns <code>true</code> if this task needs a lock onto the data store.
     */
    public boolean requiresDataStoreLock();
    
    /**
     * Creates a cleanup task for the specified data set. This method will be invoked before the
     * actual task is performed by the method {@link #execute(String)}.
     */
    public ICleanupTask createCleanupTask(String dataSetCode);
    
    /**
     * Perform the task for the specified data set.
     */
    public void execute(String dataSetCode);
    
    
}
