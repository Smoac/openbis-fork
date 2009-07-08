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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;

/**
 * Factory of Reporting Plugin Tasks.
 * 
 * @author Tomasz Pylak
 */
public class ReportingPluginTaskFactory extends AbstractPluginTaskFactory<IReportingPluginTask>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProcessingPluginTaskFactory.class);

    public ReportingPluginTaskFactory(SectionProperties sectionProperties, String datastoreCode)
    {
        super(sectionProperties, datastoreCode);
    }

    @Override
    public IReportingPluginTask createPluginInstance(File storeRoot)
    {
        return createPluginInstance(IReportingPluginTask.class, storeRoot);
    }

    @Override
    public void logConfiguration()
    {
        operationLog.info(String.format("Reporting plugin '%s' configuration:",
                getPluginDescription().getKey()));
        logPropertiesConfiguration();
    }
}
