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

package ch.systemsx.cisd.cifex.upload.server;

import ch.systemsx.cisd.cifex.upload.UploadStatus;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class UploadSession
{
    private final String sessionID;
    
    private UploadStatus uploadStatus;
    
    private String[] recipients;
    
    private String comment;

    UploadSession(String sessionID)
    {
        this.sessionID = sessionID;
    }

    final String getSessionID()
    {
        return sessionID;
    }

    final UploadStatus getUploadStatus()
    {
        return uploadStatus;
    }

    final void setUploadStatus(UploadStatus uploadStatus)
    {
        this.uploadStatus = uploadStatus;
    }

    final String[] getRecipients()
    {
        return recipients;
    }

    final void setRecipients(String[] recipients)
    {
        this.recipients = recipients;
    }

    final String getComment()
    {
        return comment;
    }

    final void setComment(String comments)
    {
        this.comment = comments;
    }

}
