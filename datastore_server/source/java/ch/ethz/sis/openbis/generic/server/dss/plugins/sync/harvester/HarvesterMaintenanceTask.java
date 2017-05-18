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
//TODO should we use permId in hash tables instead of identifier, fore exp in samplesToUpdate
//TODO try to implement sample relationship sync like DS rel. sync
//TODO check if already loaded harvesterEntityGraph can be used in most cases
//TODO check if harvesterEntityGraph can be partially loaded as required
//TODO correctly handle saving of last sync timestamp
//TODO different last sync timestamp files for different plugins - 
//this is actually handled by setting up different harvester plugins with different files
//TODO when deleting make sure we are not emptying all the trash but just the ones we synchronized
//TODO checksum checkss for data set files
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.ConfigReader;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SynchronizationConfigReader;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.EntitySynchronizer;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.parser.ILine;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

/**
 * @author Ganime Betul Akin
 */
public class HarvesterMaintenanceTask<T extends DataSetInformation> implements IMaintenanceTask
{
    private static final String TIMESTAMPS_SECTION_HEADER = "TIMESTAMPS";

    private static final String LAST_FULL_SYNC_TIMESTAMP = "last-full-sync-timestamp";

    private static final String LAST_INCREMENTAL_SYNC_TIMESTAMP = "last-incremental-sync-timestamp";

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HarvesterMaintenanceTask.class);

    final DateFormat formatter = new SimpleDateFormat("dd-MM-yy HH-mm-ss", Locale.ENGLISH);

    private static final String HARVESTER_CONFIG_FILE_PROPERTY_NAME = "harvester-config-file";

    private static final String DEFAULT_HARVESTER_CONFIG_FILE_NAME = "../../harvester-config.txt";

    private File storeRoot;

    private IEncapsulatedOpenBISService service;

    private DataSetProcessingContext context;

    private File harvesterConfigFile;

    private IMailClient mailClient;

    private String dataStoreCode;

    class Timestamps
    {
        final Date lastIncSyncTimestamp;

        final Date lastFullSyncTimestamp;

        Timestamps(Date lastIncSyncTimestamp, Date lastFullSyncTimestamp)
        {
            super();
            this.lastIncSyncTimestamp = lastIncSyncTimestamp;
            this.lastFullSyncTimestamp = lastFullSyncTimestamp;
        }

    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        service = ServiceProvider.getOpenBISService();
        context = new DataSetProcessingContext(null, null, null, null, null, null);
        dataStoreCode = getConfigProvider().getDataStoreCode();
        storeRoot = new File(DssPropertyParametersUtil.loadServiceProperties().getProperty(PluginTaskInfoProvider.STOREROOT_DIR_KEY));
        mailClient = ServiceProvider.getDataStoreService().createEMailClient();


        String configFileProperty = properties.getProperty(HARVESTER_CONFIG_FILE_PROPERTY_NAME);
        if (configFileProperty == null)
        {
            harvesterConfigFile =
                    new File(getConfigProvider().getStoreRoot(), DEFAULT_HARVESTER_CONFIG_FILE_NAME);
        } else
        {
            harvesterConfigFile = new File(configFileProperty);
        }
    }

    private IConfigProvider getConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

    @Override
    public void execute()
    {
        operationLog.info(this.getClass() + " started.");

        SynchronizationConfigReader syncConfigReader = new SynchronizationConfigReader();
        List<SyncConfig> configs;
        try
        {
            configs = syncConfigReader.readConfiguration(harvesterConfigFile, operationLog);
        } catch (Exception e)
        {
            operationLog.error("", e);
            return;
        }

        for (SyncConfig config : configs)
        {
            try
            {
                operationLog.info("-------------------------------------------------------------------------------------------");
                operationLog.info("-------------------------------------------------------------------------------------------");
                operationLog
                        .info((config.isDryRun() ? "Dry " : "") + "Running synchronization from data source: " + config.getDataSourceOpenbisURL()
                                + " for user " + config.getUser());

                operationLog.info("verbose =  " + config.isVerbose());
                String fileName = config.getLastSyncTimestampFileName();
                File lastSyncTimestampFile = new File(fileName);
                Timestamps timestamps = loadCutOffTimeStamps(lastSyncTimestampFile);

                Date cutOffTimestamp = timestamps.lastIncSyncTimestamp;
                boolean isFullSync = lastSyncTimestampFile.exists() == false || isTimeForFullSync(config, timestamps.lastFullSyncTimestamp);
                operationLog.info("Last incremental sync timestamp: " + timestamps.lastIncSyncTimestamp);
                operationLog.info("Last full sync timestamp: " + timestamps.lastFullSyncTimestamp);
                if (isFullSync == true)
                {
                    cutOffTimestamp = new Date(0L);
                    if (lastSyncTimestampFile.exists() == false)
                    {
                        operationLog.info("Performing a full initial sync");
                    }
                    else
                    {
                        operationLog.info("Performing a full sync as a minimum of " + config.getFullSyncInterval()
                                + " day(s) have elapsed since last full sync.");
                    }
                }
                else
                {
                    operationLog.info("Performing an incremental sync");
                }
                String notSyncedEntitiesFileName = config.getNotSyncedEntitiesFileName();
                Set<String> notSyncedDataSetCodes = getNotSyncedDataSetCodes(notSyncedEntitiesFileName);
                Set<String> notSyncedAttachmentHolderCodes = getNotSyncedAttachmentHolderCodes(notSyncedEntitiesFileName);
                Set<String> blackListedDataSetCodes = getBlackListedDataSetCodes(notSyncedEntitiesFileName);

                Date newCutOffTimestamp = new Date();

                EntitySynchronizer synchronizer =
                        new EntitySynchronizer(service, dataStoreCode, storeRoot, cutOffTimestamp, notSyncedDataSetCodes,
                                blackListedDataSetCodes,
                                notSyncedAttachmentHolderCodes,
                                context, config,
                                operationLog);
                Date resourceListTimestamp = synchronizer.syncronizeEntities();
                if (resourceListTimestamp.before(newCutOffTimestamp))
                {
                    newCutOffTimestamp = resourceListTimestamp;
                }
                Date newLastIncSyncTimestamp = newCutOffTimestamp;
                Date newLastFullSyncTimestamp = timestamps.lastFullSyncTimestamp;
                if (isFullSync == true)
                {
                    newLastFullSyncTimestamp = newCutOffTimestamp;
                }

                if (config.isDryRun() == false)
                {
                    operationLog.info("Saving the timestamp of sync start to file");
                    saveSyncTimestamp(lastSyncTimestampFile, newLastIncSyncTimestamp, newLastFullSyncTimestamp);
                }
                else
                {
                    operationLog.info("Dry run finished");
                }

                operationLog.info(this.getClass() + " finished executing.");
            } catch (Exception e)
            {
                operationLog.error("Sync failed: ", e);
                sendErrorEmail(config, "Synchronization failed");
            }
        }
    }

    private Timestamps loadCutOffTimeStamps(File lastSyncTimestampFile) throws IOException, ParseException
    {
        if (lastSyncTimestampFile.exists())
        {
            ConfigReader reader = new ConfigReader(lastSyncTimestampFile);
            String incSyncTimestampStr = reader.getString(TIMESTAMPS_SECTION_HEADER, LAST_INCREMENTAL_SYNC_TIMESTAMP, null, true);
            String fullSyncTimestampStr = reader.getString(TIMESTAMPS_SECTION_HEADER, LAST_FULL_SYNC_TIMESTAMP, incSyncTimestampStr, false);
            return new Timestamps(formatter.parse(incSyncTimestampStr), formatter.parse(fullSyncTimestampStr));
        }
        else
        {
            return new Timestamps(new Date(0L), new Date(0L));
        }
    }

    private boolean isTimeForFullSync(SyncConfig config, Date lastFullSyncTimestamp) throws IOException, ParseException
    {
        if (config.isFullSyncEnabled())
        {
            Integer fullSyncInterval = config.getFullSyncInterval();
            Date now = new Date();
            long diff = now.getTime() - lastFullSyncTimestamp.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days >= fullSyncInterval)
            {
                // do full sync
                return true;
            }
            else
            {
                // do incremental sync
                return false;
            }
        }
        else
        {
            // do incremental sync
            return false;
        }
    }

    private Set<String> getLinesFromNotSyncedEntitiesFile(String fileName, ILineFilter linefilter)
    {
        File notSyncedEntitiesFile = new File(fileName);
        if (notSyncedEntitiesFile.exists())
        {
            List<String> list = FileUtilities.loadToStringList(notSyncedEntitiesFile, linefilter);
            return new LinkedHashSet<String>(list);
        }
        else
        {
            return new LinkedHashSet<String>();
        }
    }

    private Set<String> getNotSyncedDataSetCodes(String fileName)
    {
        return getDataSetCodeLines(fileName, "DATA_SET");
    }

    private LinkedHashSet<String> getDataSetCodeLines(String fileName, final String prefix)
    {
        Set<String> lines = getLinesFromNotSyncedEntitiesFile(fileName, new ILineFilter()
            {
                @Override
                public <E> boolean acceptLine(ILine<E> line)
                {
                    assert line != null : "Unspecified line";
                    final String trimmed = line.getText().trim();
                    return trimmed.length() > 0 && trimmed.startsWith(prefix) == true;
                }
            });
        LinkedHashSet<String> dataSetCodes = new LinkedHashSet<String>();
        for (String line : lines)
        {
            dataSetCodes.add(line.substring(prefix.length() + 1)); // each line starts with "DATA_SET-"

        }
        return dataSetCodes;
    }

    private Set<String> getNotSyncedAttachmentHolderCodes(String fileName)
    {
        return getLinesFromNotSyncedEntitiesFile(fileName, new ILineFilter()
            {
                @Override
                public <E> boolean acceptLine(ILine<E> line)
                {
                    assert line != null : "Unspecified line";
                    final String trimmed = line.getText().trim();
                    return trimmed.length() > 0 && trimmed.startsWith("#") == false &&
                            (trimmed.startsWith(SyncEntityKind.SAMPLE.getLabel()) == true
                                    || trimmed.startsWith(SyncEntityKind.EXPERIMENT.getLabel()) == true
                                    || trimmed.startsWith(SyncEntityKind.PROJECT.getLabel()) == true);
                }
            });
    }

    private Set<String> getBlackListedDataSetCodes(String fileName)
    {
        return getDataSetCodeLines(fileName, "#DATA_SET");
    }

    private void sendErrorEmail(SyncConfig config, String subject)
    {
        if (config.getLogFilePath() != null)
        {
            // send the operation log as attachment
            DataSource dataSource = createDataSource(config.getLogFilePath()); // /Users/gakin/Documents/sync.log
            for (EMailAddress recipient : config.getEmailAddresses())
            {
                mailClient.sendEmailMessageWithAttachment(subject,
                        "See the attached file for details.",
                        "", new DataHandler(
                                dataSource), null, null, recipient);
            }
        }
        else
        {
            for (EMailAddress recipient : config.getEmailAddresses())
            {
                mailClient.sendEmailMessage(subject,
                        "See the data store server log for details.", null, null, recipient);
            }
        }
    }

    private DataSource createDataSource(final String filePath)
    {
        try
        {
            return new ByteArrayDataSource(new FileInputStream(filePath), "text/plain");
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void saveSyncTimestamp(File lastSyncTimestampFile, Date newLastIncSyncTimestamp, Date newLastFullSyncTimestamp)
    {
        FileUtilities.writeToFile(lastSyncTimestampFile, "[" + TIMESTAMPS_SECTION_HEADER + "]");
        FileUtilities.appendToFile(lastSyncTimestampFile, "\n", true);
        FileUtilities.appendToFile(lastSyncTimestampFile, LAST_INCREMENTAL_SYNC_TIMESTAMP + " = " + formatter.format(newLastIncSyncTimestamp), true);
        FileUtilities.appendToFile(lastSyncTimestampFile, LAST_FULL_SYNC_TIMESTAMP + " = " + formatter.format(newLastFullSyncTimestamp), true);
    }
}
