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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin which can be used for demonstration purposes.
 * 
 * @author Tomasz Pylak
 */
public class DemoProcessingPlugin implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    public DemoProcessingPlugin(Properties properties, File storeRoot)
    {
    }

    public ProcessingStatus process(List<DatasetDescription> datasets)
    {
        System.out.println("Processing of the following datasets has been requested: " + datasets);
        System.out.println("sleeping for 2 sec");
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
        System.out.println("Processing done.");
        return null;
    }
}
