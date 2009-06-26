/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.trigger;

import java.io.File;
import java.util.Date;

/**
 * A trigger request corresponding to an uploaded file.
 * 
 * @author Bernd Rinn
 */
public interface ITriggerRequest
{

    /**
     * Returns the file on the file system.
     */
    public File getFile();

    /**
     * Returns the file name as provided by the uploading user.
     */
    public String getFileName();

    /**
     * Returns the comment as provided by the uploading user.
     */
    public String getComment();

    /**
     * Returns the user id of the uploading user.
     */
    public String getUploadingUserId();

    /**
     * Returns the email address of the uploading user.
     */
    public String getUploadingUserEmail();

    /**
     * Returns the full name of the uploading user.
     */
    public String getUploadingUserFullName();

    /**
     * Returns the time when the request was made (i.e. the file was uploaded).
     */
    public Date getRequestTime();

    /**
     * Returns the time when the request will expire.
     */
    public Date getExpirationTime();

    /**
     * Mark the file of this request dismissiable, that is delete the file from CIFEX after the
     * trigger has been run.
     */
    public void dismiss();

}
