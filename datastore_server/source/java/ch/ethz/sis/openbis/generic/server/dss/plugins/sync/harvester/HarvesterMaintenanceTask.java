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

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

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
    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HarvesterMaintenanceTask.class);

    final DateFormat formatter = new SimpleDateFormat("dd-MM-yy HH-mm-ss", Locale.ENGLISH);

    private static final String HARVESTER_CONFIG_FILE_PROPERTY_NAME = "harvester-config-file";

    private static final String DEFAULT_HARVESTER_CONFIG_FILE_NAME = "../../harvester-config.txt";

    private File storeRoot;

    private IEncapsulatedOpenBISService service;

    private DataSetProcessingContext context;

    private Date lastSyncTimestamp;

    private File harvesterConfigFile;

    private IMailClient mailClient;

    private String dataStoreCode;

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
                operationLog
                        .info("Start synchronization from data source: " + config.getDataSourceOpenbisURL() + " for user " + config.getUser());

                String fileName = config.getLastSyncTimestampFileName();
                File lastSyncTimestampFile = new File(fileName);
                lastSyncTimestamp = getLastSyncTimeStamp(lastSyncTimestampFile);

                String notSyncedEntitiesFileName = config.getNotSyncedEntitiesFileName();
                Set<String> notSyncedDataSetCodes = getNotSyncedDataSetCodes(notSyncedEntitiesFileName);
                Set<String> notSyncedAttachmentHolderCodes = getNotSyncedAttachmentHolderCodes(notSyncedEntitiesFileName);
                Set<String> blackListedDataSetCodes = getBlackListedDataSetCodes(notSyncedEntitiesFileName);

                // save the current time into a temp file as last sync time
                File newLastSyncTimeStampFile = new File(fileName + ".new");
                Date syncStartTimestamp = new Date();
                FileUtilities.writeToFile(newLastSyncTimeStampFile, formatter.format(syncStartTimestamp));

                EntitySynchronizer synchronizer =
                        new EntitySynchronizer(service, dataStoreCode, storeRoot, lastSyncTimestamp, notSyncedDataSetCodes,
                                blackListedDataSetCodes,
                                notSyncedAttachmentHolderCodes,
                                context, config,
                                operationLog);
                Date resourceListTimestamp = synchronizer.syncronizeEntities();
                if (resourceListTimestamp.before(syncStartTimestamp))
                {
                    FileUtilities.writeToFile(newLastSyncTimeStampFile, formatter.format(resourceListTimestamp));
                }

                operationLog.info("Saving the timestamp of sync start to file");
                saveSyncTimestamp(newLastSyncTimeStampFile, lastSyncTimestampFile);

                operationLog.info(this.getClass() + " finished executing.");

            } catch (Exception e)
            {
                operationLog.error("Sync failed: ", e);
                sendErrorEmail(config, "Synchronization failed");
            }
        }
    }

    private Date getLastSyncTimeStamp(File lastSyncTimestampFile) throws ParseException
    {
        if (lastSyncTimestampFile.exists())
        {
            String timeStr = FileUtilities.loadToString(lastSyncTimestampFile).trim();
            return formatter.parse(timeStr);
        }
        else
        {
            return new Date(0L);
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
        return getLinesFromNotSyncedEntitiesFile(fileName, new ILineFilter()
            {
                @Override
                public <E> boolean acceptLine(ILine<E> line)
                {
                    assert line != null : "Unspecified line";
                    final String trimmed = line.getText().trim();
                    return trimmed.length() > 0 && trimmed.startsWith("DATA_SET") == true;
                }
            });
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
                            (trimmed.startsWith("SAMPLE") == true
                                    || trimmed.startsWith("EXPERIMENT") == true
                                    || trimmed.startsWith("PROJECT") == true);
                }
            });
    }

    private Set<String> getBlackListedDataSetCodes(String fileName)
    {
        return getLinesFromNotSyncedEntitiesFile(fileName, new ILineFilter()
            {
                @Override
                public <E> boolean acceptLine(ILine<E> line)
                {
                    assert line != null : "Unspecified line";
                    final String trimmed = line.getText().trim();
                    return trimmed.length() > 0 && trimmed.startsWith("#") == true;
                }
            });
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

    private void saveSyncTimestamp(File newLastSyncTimeStampFile, File lastSyncTimestampFile)
    {
        newLastSyncTimeStampFile.renameTo(lastSyncTimestampFile);
    }
}
