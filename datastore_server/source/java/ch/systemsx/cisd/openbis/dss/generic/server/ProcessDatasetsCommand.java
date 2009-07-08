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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Command which processes datasets using the specified plugin instance.
 * 
 * @author Tomasz Pylak
 */
public class ProcessDatasetsCommand implements IDataSetCommand
{
    private static final long serialVersionUID = 1L;

    private final IProcessingPluginTask task;

    private final List<DatasetDescription> datasets;

    public ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets)
    {
        this.task = task;
        this.datasets = datasets;
    }

    public void execute(File store)
    {
        task.process(datasets);
    }
}
