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

package ch.systemsx.cisd.datamover.console.server;

import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;

/**
 * Interface to a Datamover.
 *
 * @author Franz-Josef Elmer
 */
public interface IDatamoverConsole
{
    /**
     * Obtains the current status of the datamover.
     */
    public DatamoverStatus obtainStatus();
    
    /**
     * Obtains the current target of the running datamover.
     * 
     * @return <code>null</code> if the datamover is not running.
     */
    public String tryToObtainTarget();
    
    /**
     * Initiates a shutdown of tzhe datamover.
     */
    public void shutdown();
    
    /**
     * Starts the datamover with the specified outgoing target.
     */
    public void start(String target);
}
