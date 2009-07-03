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

package ch.systemsx.cisd.etlserver.plugin_tasks.demo;

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.plugin_tasks.framework.DatasetDescription;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.IProcessingPluginTask;

/**
 * Processing plugin which can be used for demonstration purposes.
 * 
 * @author Tomasz Pylak
 */
public class DemoProcessingPlugin implements IProcessingPluginTask
{
    public DemoProcessingPlugin(Properties properties)
    {
    }

    public void process(List<DatasetDescription> datasets)
    {
        // TODO 2009-07-03, Tomasz Pylak: implement me!
    }
}
