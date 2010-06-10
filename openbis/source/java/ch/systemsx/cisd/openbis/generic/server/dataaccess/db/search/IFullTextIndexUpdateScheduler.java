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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

/**
 * Each implementation is able to schedule a <i>full-text</i> index update. Updates are expected to
 * be executed asynchronously in the order they had been scheduled. The thread executing updates
 * will run with low priority.
 * 
 * @author Piotr Buczek
 */
public interface IFullTextIndexUpdateScheduler
{
    /**
     * Schedules update of index for specified entities.
     */
    void scheduleUpdate(EntitiesToUpdate entities);
}