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

import ch.systemsx.cisd.cifex.rpc.CRCCheckumMismatchException;
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
        NONE, UPLOAD, DOWNLOAD;
    }

    private final String sessionID;

    private final UserDTO user;

    private final String url;

    private File file;

    private FileDTO fileDTO;

    private RandomAccessFile randomAccessFile;

    private Operation operation;

    private long lastActiveMillis;

    private long oldFilePointer;

    private CloneableCRC32 oldCrc32;

    private long currentFilePointer;

    private CloneableCRC32 currentCrc32;

    Session(String sessionID, UserDTO user, String url)
    {
        this.sessionID = sessionID;
        this.user = user;
        this.url = url;
        this.oldFilePointer = -1L;
        this.currentFilePointer = 0L;
        this.currentCrc32 = new CloneableCRC32();
        operation = Operation.NONE;
        touchSession();
    }

    void reset()
    {
        touchSession();
        setFile(null);
        setRandomAccessFile(null);
        oldFilePointer = -1L;
        oldCrc32 = null;
        currentFilePointer = 0L;
        currentCrc32.reset();
        operation = Operation.NONE;
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public UserDTO getUser()
    {
        return user;
    }

    final String getUrl()
    {
        return url;
    }

    final File getFile()
    {
        return file;
    }

    final void setFile(File file)
    {
        this.file = file;
    }

    public FileDTO getFileDTO()
    {
        return fileDTO;
    }

    public void setFileDTO(FileDTO fileDTO)
    {
        this.fileDTO = fileDTO;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public void startUploadOperation() throws IllegalStateException
    {
        if (operation != Operation.NONE)
        {
            throw new IllegalStateException("Trying to start upload operation, but " + operation
                    + " currently in progress.");
        }
        operation = Operation.UPLOAD;
    }

    public boolean isUploadInProgress()
    {
        return operation == Operation.UPLOAD;
    }

    public void startDownloadOperation() throws IllegalStateException
    {
        if (operation != Operation.NONE)
        {
            throw new IllegalStateException("Trying to start download operation, but " + operation
                    + " currently in progress.");
        }
        operation = Operation.DOWNLOAD;
    }

    public boolean isDownloadInProgress()
    {
        return operation == Operation.DOWNLOAD;
    }

    public final RandomAccessFile getRandomAccessFile()
    {
        return randomAccessFile;
    }

    public final void setRandomAccessFile(RandomAccessFile randomAccessFile)
    {
        this.randomAccessFile = randomAccessFile;
        oldFilePointer = -1L;
        oldCrc32 = null;
        currentFilePointer = 0L;
        currentCrc32.reset();
    }

    public final void setRandomAccessFile(RandomAccessFile randomAccessFile, long filePointer,
            Integer crc32Value)
    {
        this.randomAccessFile = randomAccessFile;
        oldFilePointer = -1L;
        oldCrc32 = null;
        currentFilePointer = filePointer;
        if (crc32Value != null)
        {
            currentCrc32 = new CloneableCRC32(crc32Value);
        } else
        {
            currentCrc32.reset();
        }
    }

    public final void updateUploadProgress(long filePointer, int runningCrc32Value, byte[] block)
    {
        if (filePointer == currentFilePointer)
        {
            oldFilePointer = currentFilePointer;
            oldCrc32 = currentCrc32.clone();
            currentFilePointer += block.length;
        } else if (filePointer == oldFilePointer)
        {
            currentCrc32 = oldCrc32.clone();
        } else
        {
            throw new RuntimeException("Illegal value " + filePointer
                    + " for file pointer [allowed: " + oldFilePointer + "," + currentFilePointer
                    + "]");
        }
        currentCrc32.update(block);
        fileDTO.setSize(currentFilePointer);
        fileDTO.setCrc32Value(currentCrc32.getIntValue());
        // On CRC32 checksum mismatch, immediately bail out.
        if (fileDTO.getCrc32Value() != runningCrc32Value)
        {
            throw new CRCCheckumMismatchException(fileDTO.getName(), fileDTO.getSize(), fileDTO
                    .getCrc32Value(), runningCrc32Value);
        }
        if (fileDTO.getSize() > fileDTO.getCompleteSize())
        {
            throw new RuntimeException("Size of uploaded file exceeds initially set file size ("
                    + fileDTO.getSize() + " > " + fileDTO.getCompleteSize());
        }
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
