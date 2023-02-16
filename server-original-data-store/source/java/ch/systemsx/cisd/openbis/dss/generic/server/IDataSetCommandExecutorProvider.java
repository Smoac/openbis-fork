/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;

/**
 * Provider and factory of {@link IDataSetCommandExecutor} based on specified processing task.
 *
 * @author Franz-Josef Elmer
 */
public interface IDataSetCommandExecutorProvider
{
    public void init(File storeRoot);

    public IDataSetCommandExecutor getDefaultExecutor();
    
    public List<IDataSetCommandExecutor> getAllExecutors();
    
    public IDataSetCommandExecutor getExecutor(IProcessingPluginTask processingTask, String processingTaskKey);
}
