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
    
    private IMailClient mailClient;
    
    private PasswordGenerator passwordGenerator;

    public final int getFileRetention()
    {
        return fileRetention;
    }

    public final void setFileRetention(int fileRetention)
    {
        this.fileRetention = fileRetention;
    }

    public final File getFileStore()
    {
        return fileStore;
    }

    public final void setFileStore(File fileStore)
    {
        this.fileStore = fileStore;
    }

    public final int getUserRetention()
    {
        return userRetention;
    }

    public final void setUserRetention(int userRetention)
    {
        this.userRetention = userRetention;
    }

    public final int getMaxUploadRequestSizeInMB()
    {
        return maxUploadRequestSizeInMB;
    }

    public final void setMaxUploadRequestSizeInMB(int maxUploadRequestSizeInMB)
    {
        this.maxUploadRequestSizeInMB = maxUploadRequestSizeInMB;
    }

    public final IMailClient getMailClient()
    {
        return mailClient;
    }
    
    public final void setMailClient(IMailClient mailClient)
    {
        this.mailClient = mailClient;
    }
    
    public final PasswordGenerator getPasswordGenerator()
    {
        return passwordGenerator;
    }

    public final void setPasswordGenerator(PasswordGenerator passwordGenerator)
    {
        this.passwordGenerator = passwordGenerator;
    }

}
