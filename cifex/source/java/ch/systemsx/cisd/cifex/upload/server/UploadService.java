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
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadService implements IExtendedUploadService
{
    private final UploadSessionManager sessionManager = new UploadSessionManager();

    public String createSession(String[] files, String[] recipients, String comment)
    {
        UploadSession session = sessionManager.createSession();
        session.setUploadStatus(new UploadStatus(files));
        session.setRecipients(recipients);
        session.setComment(comment);
        return session.getSessionID();
    }

    public UploadStatus getUploadStatus(String uploadSessionID)
    {
        UploadSession session = sessionManager.getSession(uploadSessionID);
        if (session == null)
        {
            throw new InvalidSessionException("Invalid upload session ID: " + uploadSessionID);
        }
        return session.getUploadStatus();
    }

    public UploadStatus uploadBlock(String uploadSessionID, byte[] block, boolean lastBlock)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
