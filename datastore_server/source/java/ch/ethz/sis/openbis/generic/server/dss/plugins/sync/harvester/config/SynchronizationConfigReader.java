/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class SynchronizationConfigReader
{
    private static final String DEFAULT_HARVESTER_TEMP_DIR = "targets/store";

    private static final String DATA_SOURCE_URL_PROPERTY_NAME = "resource-list-url";

    private static final String DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME = "data-source-openbis-url";

    private static final String DATA_SOURCE_DSS_URL_PROPERTY_NAME = "data-source-dss-url";

    private static final String DATA_SOURCE_SPACES_PROPERTY_NAME = "data-source-spaces";

    private static final String DATA_SOURCE_ALIAS_PROPERTY_NAME = "data-source-alias";

    private static final String DATA_SOURCE_AUTH_REALM_PROPERTY_NAME = "data-source-auth-realm";

    private static final String DATA_SOURCE_AUTH_USER_PROPERTY_NAME = "data-source-auth-user";

    private static final String DATA_SOURCE_AUTH_PASS_PROPERTY_NAME = "data-source-auth-pass";

    private static final String HARVESTER_USER_PROPERTY_NAME = "harvester-user";

    private static final String HARVESTER_PASS_PROPERTY_NAME = "harvester-pass";

    private static final String HARVESTER_SPACES_PROPERTY_NAME = "harvester-spaces";

    private static final String HARVESTER_TEMP_DIR_PROPERTY_NAME = "harvester-tmp-dir";

    private static final String DEFAULT_LOG_FILE_NAME = "../../syncronization.log";

    private static final String HARVESTER_LAST_SYNC_TIMESTAMP_FILE_PROPERTY_NAME = "last-sync-timestamp-file";

    private static final String HARVESTER_NOT_SYNCED_DATA_SETS_FILE_NAME = "not-synced-data-sets-file";

    private static final String EMAIL_ADDRESSES_PROPERTY_NAME = "email-addresses";

    private String defaultLastSyncTimestampFileName = "last-sync-timestamp-file_{alias}.txt";

    private String defaultNotSyncedDataSetsFileName = "not-synced-datasets_{alias}.txt";

    private static final String LOG_FILE_PROPERTY_NAME = "log-file";

    List<SyncConfig> configs = new ArrayList<>();

    public List<SyncConfig> readConfiguration(File harvesterConfigFile, Logger logger) throws IOException
    {
        ConfigReader reader = new ConfigReader(harvesterConfigFile);
        int sectionCount = reader.getSectionCount();
        for (int i = 0; i < sectionCount; i++)
        {
            String section = reader.getSection(i);
            SyncConfig config = new SyncConfig();
            config.setEmailAddresses(reader.getString(section, EMAIL_ADDRESSES_PROPERTY_NAME, null, true));
            config.setLogFilePath(reader.getString(section, LOG_FILE_PROPERTY_NAME, DEFAULT_LOG_FILE_NAME, false));
            if (config.getLogFilePath() != null)
            {
                configureFileAppender(config, logger);
            }

            config.setDataSourceAlias(reader.getString(section, DATA_SOURCE_ALIAS_PROPERTY_NAME, null, true));
            config.setDataSourceURI(reader.getString(section, DATA_SOURCE_URL_PROPERTY_NAME, null, true));
            config.setDataSourceOpenbisURL(reader.getString(section, DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME, null, true));
            config.setDataSourceDSSURL(reader.getString(section, DATA_SOURCE_DSS_URL_PROPERTY_NAME, null, true));
            String realm = reader.getString(section, DATA_SOURCE_AUTH_REALM_PROPERTY_NAME, null, true);
            String dataSourceUser = reader.getString(section, DATA_SOURCE_AUTH_USER_PROPERTY_NAME, null, true);
            String dataSourcePassword = reader.getString(section, DATA_SOURCE_AUTH_PASS_PROPERTY_NAME, null, true);
            config.setAuthCredentials(realm, dataSourceUser, dataSourcePassword);

            config.setHarvesterUser(reader.getString(section, HARVESTER_USER_PROPERTY_NAME, null, true));
            config.setHarvesterPass(reader.getString(section, HARVESTER_PASS_PROPERTY_NAME, null, true));

            String dsSpaces = reader.getString(section, DATA_SOURCE_SPACES_PROPERTY_NAME, null, false);
            if (dsSpaces != null)
            {
                config.setDataSourceSpaces(dsSpaces);
            }
            String hrvSpaces = reader.getString(section, HARVESTER_SPACES_PROPERTY_NAME, null, false);
            if (hrvSpaces != null)
            {
                config.setHarvesterSpaces(hrvSpaces);
            }
            if (dsSpaces != null && hrvSpaces != null)
            {
                createDataSourceToHarvesterSpaceMappings(config);
            }

            config.setHarvesterTempDir(reader.getString(section, HARVESTER_TEMP_DIR_PROPERTY_NAME, DEFAULT_HARVESTER_TEMP_DIR, false));

            defaultLastSyncTimestampFileName = defaultLastSyncTimestampFileName.replaceFirst(Pattern.quote("{alias}"), config.getDataSourceAlias());
            config.setLastSyncTimestampFileName(
                    reader.getString(section, HARVESTER_LAST_SYNC_TIMESTAMP_FILE_PROPERTY_NAME, defaultLastSyncTimestampFileName, false));

            defaultNotSyncedDataSetsFileName = defaultNotSyncedDataSetsFileName.replaceFirst(Pattern.quote("{alias}"), config.getDataSourceAlias());
            config.setNotSyncedDataSetsFileName(
                    reader.getString(section, HARVESTER_NOT_SYNCED_DATA_SETS_FILE_NAME, defaultNotSyncedDataSetsFileName, false));
            configs.add(config);
        }
        return configs;
    }

    private void configureFileAppender(SyncConfig config, Logger logger)
    {
        DailyRollingFileAppender console = new DailyRollingFileAppender(); // create appender
        // configure the appender
        console.setName("bdfile");
        String PATTERN = "%d %-5p [%t] %c - %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        // console.setThreshold(Level.FATAL);
        console.setAppend(false);
        console.setFile(config.getLogFilePath());
        console.activateOptions();
        // add appender to any Logger (here is root)
        logger.addAppender(console);
        logger.setAdditivity(false);
    }

    private void createDataSourceToHarvesterSpaceMappings(SyncConfig config)
    {
        List<String> dataSourceSpaceList = config.getDataSourceSpaces();
        List<String> harvesterSpaceList = config.getHarvesterSpaces();
        if (dataSourceSpaceList.size() != harvesterSpaceList.size())
        {
            throw new ConfigurationFailureException("Please specify a harvester space for each data source space.");
        }
        for (int i = 0; i < dataSourceSpaceList.size(); i++)
        {
            String harvesterSpace = harvesterSpaceList.get(i);
            Space destSpace = ServiceProvider.getOpenBISService().tryGetSpace(new SpaceIdentifier(harvesterSpace));
            if (destSpace == null)
            {
                throw new ConfigurationFailureException("Space " + harvesterSpace + " does not exist");
            }
            config.getSpaceMappings().put(dataSourceSpaceList.get(i), harvesterSpace);
        }
    }
}
