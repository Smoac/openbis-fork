/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * @author Piotr Buczek
 */
public class AutoArchiverTask implements IMaintenanceTask
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AutoArchiverTask.class);

    private IEncapsulatedOpenBISService openBISService;

    public void execute()
    {
        operationLog.info("start");
        List<ExternalData> datasets = openBISService.listActiveDataSets();
        // some additional filtering could be done here
        if (datasets.isEmpty())
        {
            operationLog.info("nothing to archive");
        } else
        {
            operationLog.info("archiving: "
                    + CollectionUtils.abbreviate(Code.extractCodes(datasets), 10));
            openBISService.archiveDataSets(Code.extractCodes(datasets));
        }
        operationLog.info("end");
    }

    public void setUp(String pluginName, Properties properties)
    {
        openBISService = ServiceProvider.getOpenBISService();
        operationLog.info("Plugin initialized");
    }
}
