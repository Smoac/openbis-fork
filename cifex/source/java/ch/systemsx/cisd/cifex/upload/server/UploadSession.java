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

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.upload.UploadStatus;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class UploadSession
{
    private final String sessionID;
    
    private final UserDTO user;
    
    private final UploadStatus uploadStatus;
    
    private String url;
    
    private File file;
    
    private List<File> temporaryFiles = new ArrayList<File>();
    
    private RandomAccessFile randomAccessFile;
    
    private String[] recipients;
    
    private String comment;

    UploadSession(String sessionID, UserDTO user, String url)
    {
        this.sessionID = sessionID;
        this.user = user;
        this.url = url;
        uploadStatus = new UploadStatus();
    }
    
    void reset()
    {
        temporaryFiles.clear();
        setFile(null);
        setRandomAccessFile(null);
        uploadStatus.reset();
    }

    final String getSessionID()
    {
        return sessionID;
    }

    final UserDTO getUser()
    {
        return user;
    }

    final String getUrl()
    {
        return url;
    }

    public final void setUrl(String url)
    {
        this.url = url;
    }

    final File getFile()
    {
        return file;
    }

    final void setFile(File file)
    {
        this.file = file;
    }
    
    final void addTempFile(File tempFile)
    {
        temporaryFiles.add(tempFile);
    }
    
    final List<File> getTempFiles()
    {
        return temporaryFiles;
    }

    public final RandomAccessFile getRandomAccessFile()
    {
        return randomAccessFile;
    }

    public final void setRandomAccessFile(RandomAccessFile randomAccessFile)
    {
        this.randomAccessFile = randomAccessFile;
    }

    final UploadStatus getUploadStatus()
    {
        return uploadStatus;
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
