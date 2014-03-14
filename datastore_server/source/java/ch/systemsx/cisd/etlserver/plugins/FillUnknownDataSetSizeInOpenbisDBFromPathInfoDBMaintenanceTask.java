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

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    static final String CHUNK_SIZE_PROPERTY = "data-set-chunk-size";

    static final int CHUNK_SIZE_DEFAULT = 100;

    static final String TIME_LIMIT_PROPERTY = "time-limit";

    static final long TIME_LIMIT_DEFAULT = DateUtils.MILLIS_PER_HOUR;

    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    static final String LAST_SEEN_DATA_SET_FILE_DEFAULT = "fillUnknownDataSetSizeTaskLastSeen";

    static final String DROP_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY = "drop-last-seen-data-set-file-interval";

    static final long DROP_LAST_SEEN_DATA_SET_FILE_INTERVAL_DEFAULT = DateUtils.MILLIS_PER_DAY * 7;

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    private ITimeProvider timeProvider;

    private IConfigProvider configProvider;

    private int chunkSize;

    private long timeLimit;

    private File lastSeenDataSetFile;

    private long dropLastSeenDataSetFileInterval;

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
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE_PROPERTY, CHUNK_SIZE_DEFAULT);
        timeLimit = DateTimeUtils.getDurationInMillis(properties, TIME_LIMIT_PROPERTY, TIME_LIMIT_DEFAULT);

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

        dropLastSeenDataSetFileInterval =
                DateTimeUtils.getDurationInMillis(properties, DROP_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY,
                        DROP_LAST_SEEN_DATA_SET_FILE_INTERVAL_DEFAULT);

        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append(pluginName + " initialized with\n");
        logBuilder.append(CHUNK_SIZE_PROPERTY + ": " + chunkSize + "\n");
        logBuilder.append(TIME_LIMIT_PROPERTY + ": " + DateTimeUtils.renderDuration(timeLimit) + "\n");
        logBuilder.append(LAST_SEEN_DATA_SET_FILE_PROPERTY + ": " + lastSeenDataSetFile.getAbsolutePath() + "\n");
        logBuilder.append(DROP_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY + ": " + DateTimeUtils.renderDuration(dropLastSeenDataSetFileInterval));

        operationLog.info(logBuilder.toString());
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

        prepareLastSeenDataSetFile();

        do
        {
            LastSeenDataSetFileContent lastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenDataSetFile);

            dataSets = service.listPhysicalDataSetsWithUnknownSize(chunkSize, lastSeenContent.getLastSeenDataSetCode());
            foundDataSets = dataSets != null && false == dataSets.isEmpty();

            operationLog.info("Last seen data set code: " + lastSeenContent.getLastSeenDataSetCode() + " (chunk: " + chunkIndex + ").");

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

                    if (false == sizeMap.isEmpty())
                    {
                        service.updatePhysicalDataSetsSize(sizeMap);
                    }
                }

                LastSeenDataSetFileContent newLastSeenContent = new LastSeenDataSetFileContent();
                newLastSeenContent.setFileCreationTime(lastSeenContent.getFileCreationTime());
                newLastSeenContent.setLastSeenDataSetCode(Collections.max(codes));
                newLastSeenContent.writeToFile(lastSeenDataSetFile);

            } else
            {
                operationLog.info("Did not find any datasets with unknown size in openbis database (chunk: " + chunkIndex + ").");
            }

            reachedTimeLimit = timeProvider.getTimeInMilliseconds() > startTime + timeLimit;
            chunkIndex++;

        } while (foundDataSets && false == reachedTimeLimit);

        operationLog.info("Filling finished.");
    }

    private void prepareLastSeenDataSetFile()
    {
        LastSeenDataSetFileContent content = LastSeenDataSetFileContent.readFromFile(lastSeenDataSetFile);

        if (content == null)
        {
            content = new LastSeenDataSetFileContent();
            content.setFileCreationTime(timeProvider.getTimeInMilliseconds());
            content.writeToFile(lastSeenDataSetFile);

            operationLog.info("Created last seen data set file.");

        } else if (content.getFileCreationTime() == null
                || timeProvider.getTimeInMilliseconds() > content.getFileCreationTime() + dropLastSeenDataSetFileInterval)
        {
            lastSeenDataSetFile.delete();

            operationLog.info("Deleted last seen data set file because its age was unknown or its age was greater than "
                    + DROP_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY
                    + " interval value.");

            content = new LastSeenDataSetFileContent();
            content.setFileCreationTime(timeProvider.getTimeInMilliseconds());
            content.writeToFile(lastSeenDataSetFile);
        }
    }

    private static IPathsInfoDAO createDAO()
    {
        return QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

    private IConfigProvider createConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

    public static class LastSeenDataSetFileContent
    {
        private static final String FILE_CREATION_TIME_PROPERTY = "file-creation-time";

        private static final String LAST_SEEN_DATA_SET_CODE_PROPERTY = "last-seen-data-set-code";

        private Long fileCreationTime;

        private String lastSeenDataSetCode;

        public void setFileCreationTime(Long fileCreationTime)
        {
            this.fileCreationTime = fileCreationTime;
        }

        public Long getFileCreationTime()
        {
            return fileCreationTime;
        }

        public void setLastSeenDataSetCode(String lastSeenDataSetCode)
        {
            this.lastSeenDataSetCode = lastSeenDataSetCode;
        }

        public String getLastSeenDataSetCode()
        {
            return lastSeenDataSetCode;
        }

        @SuppressWarnings("unchecked")
        public static LastSeenDataSetFileContent readFromFile(File file)
        {
            if (file.exists())
            {
                Map<String, Object> map = null;

                try
                {
                    map = new ObjectMapper().readValue(file, Map.class);
                } catch (Exception e)
                {
                    throw new IllegalArgumentException("Could not read the last seen data set file", e);
                }

                LastSeenDataSetFileContent content = new LastSeenDataSetFileContent();
                content.setFileCreationTime(readPropertyAsLong(map, FILE_CREATION_TIME_PROPERTY));
                content.setLastSeenDataSetCode(readPropertyAsString(map, LAST_SEEN_DATA_SET_CODE_PROPERTY));
                return content;

            } else
            {
                return null;
            }
        }

        private static Long readPropertyAsLong(Map<String, Object> propertyMap, String propertyName)
        {
            try
            {
                Object propertyValue = propertyMap.get(propertyName);

                if (propertyValue == null)
                {
                    return null;
                } else if (propertyValue instanceof String)
                {
                    return Long.parseLong((String) propertyValue);
                } else if (propertyValue instanceof Number)
                {
                    return ((Number) propertyValue).longValue();
                } else
                {
                    throw new IllegalArgumentException("Cannot convert value: " + propertyValue + " of type: " + propertyValue.getClass()
                            + " to long.");
                }
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Could not read " + propertyName + " property", e);
            }
        }

        private static String readPropertyAsString(Map<String, Object> propertyMap, String propertyName)
        {
            Object value = propertyMap.get(propertyName);

            try
            {
                return (String) value;
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Could not read " + propertyName + " property", e);
            }
        }

        public void writeToFile(File file)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(FILE_CREATION_TIME_PROPERTY, getFileCreationTime());
            map.put(LAST_SEEN_DATA_SET_CODE_PROPERTY, getLastSeenDataSetCode());

            try
            {
                new ObjectMapper().writeValue(file, map);
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Could not write the last seen data set file", e);
            }
        }

    }

}
