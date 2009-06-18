/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business;

import java.io.File;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * Bean implementing {@link IBusinessContext}.
 * 
 * @author Franz-Josef Elmer
 */
class BusinessContext implements IBusinessContext
{
    /** The root location where uploaded files are going to be stored. */
    private File fileStore;

    /** How long (in minutes) the file is going to stay in the system? */
    private int fileRetention;

    /** How long (in minutes) the user is going to stay in the system? */
    private int userRetention;

    /** The maximum size of an upload request in Megabyte. */
    private int maxUploadRequestSizeInMB;
    
    /** Whether new externally authenticated users should start active or not. */
    private boolean newExternallyAuthenticatedUserStartActive;

    private IMailClient mailClient;

    private PasswordGenerator passwordGenerator;

    private UserHttpSessionHolder userHttpSessionHolder;

    /** The logger object for user actions. */
    private IUserActionLog userActionLog;

    private String administratorEmail;

    /**
     * The URL to use for emails in override mode.
     */
    private String overrideURL;
    
    private String systemVersion;
    
    private int triggerPermits;

    public int getTriggerPermits()
    {
        return triggerPermits;
    }

    public void setTriggerPermits(int triggerPermits)
    {
        this.triggerPermits = triggerPermits;
    }

    public final IUserSessionInvalidator getUserSessionInvalidator()
    {
        return userHttpSessionHolder;
    }

    public final void setUserHttpSessionHolder(final UserHttpSessionHolder userHttpSessionHolder)
    {
        this.userHttpSessionHolder = userHttpSessionHolder;
    }

    /**
     * Returns the logger of user behavior.
     */
    public IUserActionLog getUserActionLog()
    {
        return userActionLog;
    }

    /**
     * Sets the logger of user behavior.
     */
    public void setUserActionLog(IUserActionLog userActionLog)
    {
        this.userActionLog = userActionLog;
    }

    public final int getFileRetention()
    {
        return fileRetention;
    }

    public final void setFileRetention(final int fileRetention)
    {
        this.fileRetention = fileRetention;
    }

    public final File getFileStore()
    {
        return fileStore;
    }

    public final void setFileStore(final File fileStore)
    {
        this.fileStore = fileStore;
    }

    public final int getUserRetention()
    {
        return userRetention;
    }

    public final void setUserRetention(final int userRetention)
    {
        this.userRetention = userRetention;
    }

    public final int getMaxUploadRequestSizeInMB()
    {
        return maxUploadRequestSizeInMB;
    }

    public final void setMaxUploadRequestSizeInMB(final int maxUploadRequestSizeInMB)
    {
        this.maxUploadRequestSizeInMB = maxUploadRequestSizeInMB;
    }

    public boolean isNewExternallyAuthenticatedUserStartActive()
    {
        return newExternallyAuthenticatedUserStartActive;
    }

    public void setNewExternallyAuthenticatedUserStartActive(
            boolean newExternallyAuthenticateUserStartActive)
    {
        this.newExternallyAuthenticatedUserStartActive = newExternallyAuthenticateUserStartActive;
    }

    public final IMailClient getMailClient()
    {
        return mailClient;
    }

    public final void setMailClient(final IMailClient mailClient)
    {
        this.mailClient = mailClient;
    }

    public final PasswordGenerator getPasswordGenerator()
    {
        return passwordGenerator;
    }

    public final void setPasswordGenerator(final PasswordGenerator passwordGenerator)
    {
        this.passwordGenerator = passwordGenerator;
    }

    public final void setAdministratorEmail(String administratorEmail)
    {
        this.administratorEmail = administratorEmail;
    }

    public final String getAdministratorEmail()
    {
        return administratorEmail;
    }

    public final String getSystemVersion()
    {
        return systemVersion;
    }

    public final void setOverrideURL(String overrideURL)
    {
        this.overrideURL = overrideURL;
    }

    public final String getOverrideURL()
    {
        return overrideURL;
    }

    public final void setSystemVersion(final String systemVersion)
    {
        this.systemVersion = systemVersion;
    }

}
