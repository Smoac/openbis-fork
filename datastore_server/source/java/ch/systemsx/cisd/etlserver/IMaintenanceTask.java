/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

/**
 * The interface that should be implemented by all maintenance tasks.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaintenanceTask
{

    /**
     * Performs the maintenance task.
     */
    public void execute();

    /**
     * Prepares the task for execution and checks that it has been configured correctly.
     */
    public void setUp(String pluginName);

}
