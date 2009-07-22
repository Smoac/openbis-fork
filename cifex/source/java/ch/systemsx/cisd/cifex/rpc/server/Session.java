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

package ch.systemsx.cisd.cifex.rpc.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import ch.systemsx.cisd.cifex.rpc.UploadStatus;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * A session for rpc based calls.
 *
 * @author Franz-Josef Elmer
 */
public final class Session
{
    public enum Operation
    {
        UPLOAD, DOWNLOAD;
    }
    
    private final String sessionID;
    
    private final UserDTO user;
    
    private final UploadStatus uploadStatus;
    
    private String url;
    
    private File file;
    
    private FileDTO fileInfo;
    
    private List<File> temporaryFiles = new ArrayList<File>();
    
    private RandomAccessFile randomAccessFile;
    
    private String[] recipients;
    
    private String comment;
    
    private Operation operation;
    
    private long lastActiveMillis;
    
    private CRC32 crc32;

    Session(String sessionID, UserDTO user, String url)
    {
        this.sessionID = sessionID;
        this.user = user;
        this.url = url;
        this.uploadStatus = new UploadStatus();
        this.crc32 = new CRC32();
        touchSession();
    }
    
    void reset()
    {
        touchSession();
        temporaryFiles.clear();
        setFile(null);
        setRandomAccessFile(null);
        crc32.reset();
        uploadStatus.reset();
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public  UserDTO getUser()
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
    
    public FileDTO getFileInfo()
    {
        return fileInfo;
    }

    public void setFileInfo(FileDTO fileInfo)
    {
        this.fileInfo = fileInfo;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public void setOperation(Operation operation)
    {
        this.operation = operation;
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
        crc32.reset();
    }

    public CRC32 getCrc32()
    {
        return crc32;
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

    public long getLastActiveMillis()
    {
        return lastActiveMillis;
    }
    
    public void touchSession()
    {
        lastActiveMillis = System.currentTimeMillis();
    }

}
