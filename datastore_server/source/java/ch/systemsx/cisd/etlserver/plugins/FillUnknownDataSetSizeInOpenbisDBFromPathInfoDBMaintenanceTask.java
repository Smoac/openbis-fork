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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
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

    static final String CHUNK_SIZE_KEY = "data-set-chunk-size";

    static final String TIME_LIMIT_KEY = "time-limit";

    static final int DEFAULT_CHUNK_SIZE = 100;

    static final int DEFAULT_TIME_LIMIT = 1000 * 60 * 60;

    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    static final String LAST_SEEN_DATA_SET_FILE_DEFAULT = "fillUnknownDataSetSizeTaskLastSeen";

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    private ITimeProvider timeProvider;

    private IConfigProvider configProvider;

    private int chunkSize;

    private long timeLimit;

    private File lastSeenDataSetFile;

    public FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask()
    {
        service = ServiceProvider.getOpenBISService();
        dao = createDAO();
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        configProvider = createConfigProvider();
    }

    public FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask(IEncapsulatedOpenBISService service, IPathsInfoDAO dao,
            ITimeProvider timeProvider, IConfigProvider configProvider)
    {
        this.service = service;
        this.dao = dao;
        this.timeProvider = timeProvider;
        this.configProvider = configProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE_KEY, DEFAULT_CHUNK_SIZE);
        timeLimit = DateTimeUtils.getDurationInMillis(properties, TIME_LIMIT_KEY, DEFAULT_TIME_LIMIT);

        String lastSeenDataSetFileProperty =
                properties.getProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY);

        if (lastSeenDataSetFileProperty == null)
        {
            lastSeenDataSetFile =
                    new File(configProvider.getStoreRoot(), LAST_SEEN_DATA_SET_FILE_DEFAULT);
        } else
        {
            lastSeenDataSetFile = new File(lastSeenDataSetFileProperty);
        }

        operationLog.info(pluginName + " initialized with chunk size = " + chunkSize + ", time limit = " + DateTimeUtils.renderDuration(timeLimit)
                + ", last seen file = " + lastSeenDataSetFile.getAbsolutePath());
    }

    @SuppressWarnings("null")
    @Override
    public void execute()
    {
        operationLog.info("Start filling.");

        List<SimpleDataSetInformationDTO> dataSets = null;

        long startTime = timeProvider.getTimeInMilliseconds();
        boolean foundDataSets = false;
        boolean reachedTimeLimit = false;
        int chunkIndex = 1;

        do
        {
            dataSets = service.listPhysicalDataSetsWithUnknownSize(chunkSize, getLastSeenDataSetCode());
            foundDataSets = dataSets != null && false == dataSets.isEmpty();

            if (foundDataSets)
            {
                operationLog.info("Found " + dataSets.size() + " dataset(s) with unknown size in openbis database (chunk: " + chunkIndex + ").");

                Set<String> codes = new HashSet<String>();

                for (SimpleDataSetInformationDTO dataSet : dataSets)
                {
                    codes.add(dataSet.getDataSetCode());
                }

                List<PathEntryDTO> pathInfoEntries = dao.listDataSetsSize(codes.toArray(new String[codes.size()]));

                if (pathInfoEntries != null && false == pathInfoEntries.isEmpty())
                {
                    Map<String, Long> sizeMap = new HashMap<String, Long>();

                    for (PathEntryDTO pathInfoEntry : pathInfoEntries)
                    {
                        if (pathInfoEntry.getSizeInBytes() != null)
                        {
                            sizeMap.put(pathInfoEntry.getDataSetCode(), pathInfoEntry.getSizeInBytes());
                        }
                    }

                    operationLog.info("Found sizes for " + sizeMap.size() + " dataset(s) in pathinfo database (chunk: " + chunkIndex + ").");

                    service.updatePhysicalDataSetsSize(sizeMap);
                }

                updateLastSeenDataSetCode(Collections.max(codes));
            } else
            {
                operationLog.info("Did not find any datasets with unknown size in openbis database (chunk: " + chunkIndex + ").");
            }

            reachedTimeLimit = timeProvider.getTimeInMilliseconds() > startTime + timeLimit;
            chunkIndex++;

        } while (foundDataSets && false == reachedTimeLimit);

        operationLog.info("Filling finished.");
    }

    protected String getLastSeenDataSetCode()
    {
        if (lastSeenDataSetFile.exists())
        {
            return FileUtilities.loadToString(lastSeenDataSetFile).trim();
        } else
        {
            return null;
        }
    }

    protected void updateLastSeenDataSetCode(String dataSetCode)
    {
        FileUtilities.writeToFile(lastSeenDataSetFile, dataSetCode);
    }

    private static IPathsInfoDAO createDAO()
    {
        return QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

    private IConfigProvider createConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

}
