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

package ch.ethz.bsse.cisd.dsu.tracking.main;

import static ch.systemsx.cisd.common.properties.PropertyUtils.getMandatoryProperty;

import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * @author Tomasz Pylak
 * @author Manuel Kohler
 */
public class Parameters
{
    private static final String OPENBIS_USER = "openbis-user";

    private static final String OPENBIS_PASSWORD = "openbis-password";

    private static final String OPENBIS_SERVER_URL = "openbis-server-url";

    private static final String PERMLINK_URL = "permlink-url";

    private static final String TRACKING_ADMIN_EMAIL = "tracking-admin-email";

    private static final String NOTIFICATION_EMAIL_FROM = "mail.from";
    
    private static final String SPACE_WHITELIST = "space-whitelist";
    
    private static final String DBM_SPACE_PREFIX = "dbm-space-prefix";
       
    // For Development Mode
    private static final String DEBUG = "debug";
    
    private static final String OLD_DATA_SET_BACKLOG_NUMBER = "old-data-set-backlog-number";
    
    private static final String DATA_SET_TYPE_LIST = "dataset-type-list";

    private static final String DESTINATION_FOLDER = "destination-folder";
    
    private static final String RSYNC_BINARY = "rsync-binary";

    private static final String RSYNC_FLAGS = "rsync-flags";
    
    private static final String DSS_ROOT_DIR = "dss-root-dir";
    
    private final String openbisUser;

    private final String openbisPassword;

    private final String openbisServerURL;

    private final String permlinkURL;

    private final IMailClient mailClient;

    private final String adminEmail;

    private final String notificationEmail;
    
    private final String spaceWhitelist;
    
    private final String dbmSpacePrefix;
    
    private final boolean debug;
    
    private final long oldDataSetBacklogNumber;
    
    private final String dataSetTypeList;
    
    private final String destinationFolder;
    
    private final String rsyncBinary;
    
    private final String rsyncFlags;
    
    private final String dssRootDir;

    public Parameters(Properties props)
    {
        this.openbisUser = getMandatoryProperty(props, OPENBIS_USER);
        this.openbisPassword = getMandatoryProperty(props, OPENBIS_PASSWORD);
        this.openbisServerURL = getMandatoryProperty(props, OPENBIS_SERVER_URL);
        this.permlinkURL = PropertyUtils.getProperty(props, PERMLINK_URL, openbisServerURL);
        this.mailClient = new MailClient(props);
        this.adminEmail = PropertyUtils.getProperty(props, TRACKING_ADMIN_EMAIL);
        this.notificationEmail = PropertyUtils.getProperty(props, NOTIFICATION_EMAIL_FROM);
        this.spaceWhitelist = PropertyUtils.getProperty(props, SPACE_WHITELIST);
        this.dbmSpacePrefix = PropertyUtils.getProperty(props, DBM_SPACE_PREFIX);
        this.debug = PropertyUtils.getBoolean(props, DEBUG, false);
        this.oldDataSetBacklogNumber = PropertyUtils.getInt(props, OLD_DATA_SET_BACKLOG_NUMBER, 0);
        this.dataSetTypeList = PropertyUtils.getProperty(props, DATA_SET_TYPE_LIST);
        this.destinationFolder= PropertyUtils.getProperty(props, DESTINATION_FOLDER);
        this.rsyncBinary = PropertyUtils.getProperty(props, RSYNC_BINARY);
        this.rsyncFlags = PropertyUtils.getProperty(props, RSYNC_FLAGS);
        this.dssRootDir = PropertyUtils.getProperty(props, DSS_ROOT_DIR);
    }

    public String getOpenbisUser()
    {
        return openbisUser;
    }

    public String getOpenbisPassword()
    {
        return openbisPassword;
    }

    public String getOpenbisServerURL()
    {
        return openbisServerURL;
    }

    public IMailClient getMailClient()
    {
        return mailClient;
    }

    public String getPermlinkURL()
    {
        return permlinkURL;
    }

    public String getAdminEmail()
    {
        return adminEmail;
    }

    public String getNotificationEmail()
    {
        return notificationEmail;
    }
    
    public String getSpaceWhitelist()
    {
        return spaceWhitelist;
    }
    
    public String getDbmSpacePrefix()
    {
        return dbmSpacePrefix;
    }
    
    public boolean getDebug() 
    {
    	return debug;
    }
    
    public long getoldDataSetBacklogNumber() 
    {
    	return oldDataSetBacklogNumber;
    }    
    
    public String getdataSetTypeList()
    {
    	return dataSetTypeList;
    }
    
    public String getDestinationFolder()
    {
    	return destinationFolder;
    }
    
    public String getRsyncBinary()
    {
    	return rsyncBinary;
    }
    
    public String [] getRsyncFlags()
    {
    	return rsyncFlags.split("\\s+");
    }
    
    public String getDssRoot()
    {
    	return dssRootDir;
    }
}