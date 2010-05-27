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
import java.util.Set;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * Context needed by the business layer. Usually it is provided by some configuration parameters.
 * 
 * @author Franz-Josef Elmer
 */
public interface IBusinessContext
{
    /**
     * Returns the directory which contains all uploaded files.
     */
    public File getFileStore();

    /**
     * Returns the default file retention time in days.
     */
    public int getFileRetention();

    /**
     * Returns the maximum file retention time in days.
     */
    public int getMaxFileRetention();
    
    /**
     * Returns the default retention time of temporary users in days.
     */
    public int getUserRetention();

    /**
     * Returns the maximum retention time of temporary users in days.
     */
    public int getMaxUserRetention();
    
    /**
     * Returns the maximum sum of the size of all files allowed to be uploaded per quota group (in MB).
     */
    public Long getMaxFileSizePerQuotaGroupInMB();
    
    /**
     * Returns the maximum number of all files allowed to be uploaded per quota group.
     */
    public Integer getMaxFileCountPerQuotaGroup();
    
    /**
     * Returns whether new externally authenticated users should start active or not.
     */
    public boolean isNewExternallyAuthenticatedUserStartActive();

    /**
     * Returns the number of trigger permits for this server. This determines the number of
     * asynchronous triggers that can run in parallel. (More expensive triggers may use up more than
     * one permit.)
     */
    public int getTriggerPermits();

    /**
     * Returns the mail client.
     */
    public IMailClient getMailClient();

    /**
     * Returns the password generator.
     */
    public PasswordGenerator getPasswordGenerator();

    /**
     * Returns the user session invalidator implementation.
     */
    public IUserSessionInvalidator getUserSessionInvalidator();

    /**
     * Returns the logger of user behavior for pure HTTP sessions.
     */
    public IUserActionLog getUserActionLogHttp();

    /**
     * Returns the logger of user behavior for RPC sessions..
     */
    public IUserActionLog getUserActionLogRpc();

    /**
     * Returns the URL to be used for links in emails, or an empty String, if the base URL from the
     * HTTP request should be used.
     */
    public String getOverrideURL();

    /** Returns the full version information of this server. */
    public String getSystemVersion();
    
    /**
     * Returns the set of allowed IPs from which setting the session user explicitely is allowed.
     */
    public Set<String> getAllowedIPsForSetSessionUser();

}