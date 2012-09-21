/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.conversation.progress;

/**
 * A role that can receive progress updates from method executions initiated through the service
 * conversation framework by remote clients.
 * 
 * @author anttil
 */
public interface IServiceConversationProgressListener
{
    public void update(String phaseName, int totalItemsToProcess, int numItemsProcessed);

    public void close();
}
