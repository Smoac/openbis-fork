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

import static ch.systemsx.cisd.cifex.server.AbstractFileUploadServlet.MAX_FILENAME_LENGTH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.server.business.DomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadService implements IExtendedUploadService
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, UploadService.class);

    private final UploadSessionManager sessionManager = new UploadSessionManager();
    private final IFileManager fileManager;
    
    public UploadService(DomainModel domainModel)
    {
        this(domainModel.getFileManager());
    }
    
    UploadService(IFileManager fileManager)
    {
        this.fileManager = fileManager;
    }

    public String createSession(UserDTO user, String url)
    {
        return sessionManager.createSession(user, url).getSessionID();
    }

    public void add(String uploadSessionID, String[] files, String[] recipients, String comment)
    {
        UploadSession session = sessionManager.getSession(uploadSessionID);
        session.setUploadStatus(new UploadStatus(files));
        session.setRecipients(recipients);
        session.setComment(comment);
        
    }

    public UploadStatus getUploadStatus(String uploadSessionID)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Obtain upload status for session " + uploadSessionID);
        }
        UploadSession session = sessionManager.getSession(uploadSessionID);
        if (session == null)
        {
            throw new InvalidSessionException("Invalid upload session ID: " + uploadSessionID);
        }
        return session.getUploadStatus();
    }

    public UploadStatus uploadBlock(String uploadSessionID, byte[] block, int blockSize, boolean lastBlock)
    {
        UploadSession session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = session.getUploadStatus();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Upload " + (lastBlock ? "last block" : "block") + ": " + status);
        }
        UploadState state = status.getUploadState();
        UserDTO user = session.getUser();
        if (state == UploadState.INIT)
        {
            String fileName =
                    FilenameUtilities.ensureMaximumSize(status.getNameOfCurrentFile(),
                            MAX_FILENAME_LENGTH);
            File file = fileManager.createFile(user, fileName);
            session.setFile(file);
            RandomAccessFile randomAccessFile = createRandomAccessFile(file);
            session.setRandomAccessFile(randomAccessFile);
        }
        if (state == UploadState.INIT || state == UploadState.UPLOADING)
        {
            RandomAccessFile randomAccessFile = session.getRandomAccessFile();
            long filePointer = status.getFilePointer();
            try
            {
                randomAccessFile.seek(filePointer);
                randomAccessFile.write(block, 0, blockSize);
                if (lastBlock)
                {
                    randomAccessFile.close();
                    File file = session.getFile();
                    String contentType = FilenameUtilities.getMimeType(file.getName());
                    String comment = session.getComment();
                    fileManager.registerFileLinkAndInformRecipients(user, status.getNameOfCurrentFile(), comment,
                            contentType, file, session.getRecipients(), session.getUrl());
                    status.next();
                } else
                {
                    status.setUploadState(UploadState.UPLOADING);
                    status.setFilePointer(filePointer + blockSize);
                }
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Block uploaded: " + status);
                }
                return status;
            } catch (IOException ex)
            {
                throw new WrappedIOException(ex);
            }
        }
        throw new UserFailureException("Invalid status: " + status);
    }

    private RandomAccessFile createRandomAccessFile(File file)
    {
        try
        {
            return new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException ex)
        {
            throw new WrappedIOException(ex);
        }
    }

}
