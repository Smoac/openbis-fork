/*
 * Copyright 2014 ETH Zuerich, CISD
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author pkupczyk
 */
public class FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask implements IMaintenanceTask
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.class);

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    public FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask()
    {
    }

    public FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask(IEncapsulatedOpenBISService service, IPathsInfoDAO dao)
    {
        this.service = service;
        this.dao = dao;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        service = ServiceProvider.getOpenBISService();
        dao = createDAO();
    }

    @Override
    public void execute()
    {
        operationLog.info("Start filling.");

        List<SimpleDataSetInformationDTO> dataSets = service.listPhysicalDataSetsWithUnknownSize();
        Set<String> codes = new HashSet<String>();

        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            codes.add(dataSet.getDataSetCode());
        }

        operationLog.info("Found " + codes.size() + " dataset(s) with unknown size in openbis database.");

        if (codes.size() > 0)
        {
            List<PathEntryDTO> pathInfoEntries = dao.listDataSetsSize(codes.toArray(new String[codes.size()]));
            Map<String, Long> sizeMap = new HashMap<String, Long>();

            for (PathEntryDTO pathInfoEntry : pathInfoEntries)
            {
                if (pathInfoEntry.getSizeInBytes() != null)
                {
                    sizeMap.put(pathInfoEntry.getDataSetCode(), pathInfoEntry.getSizeInBytes());
                }
            }

            operationLog.info("Found sizes for " + sizeMap.size() + " dataset(s) in pathinfo database.");

            service.updatePhysicalDataSetsSize(sizeMap);

        }
        operationLog.info("Filling finished.");
    }

    private static IPathsInfoDAO createDAO()
    {
        return QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

}
