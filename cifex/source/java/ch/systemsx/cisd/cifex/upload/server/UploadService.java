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
import static ch.systemsx.cisd.cifex.upload.UploadState.FINISHED;
import static ch.systemsx.cisd.cifex.upload.UploadState.INITIALIZED;
import static ch.systemsx.cisd.cifex.upload.UploadState.READY_FOR_NEXT_FILE;
import static ch.systemsx.cisd.cifex.upload.UploadState.UPLOADING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadService implements IExtendedUploadService
{
    private static final String PREFIX = "$";
    
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, UploadService.class);

    private final UploadSessionManager sessionManager = new UploadSessionManager();
    private final IFileManager fileManager;
    
    public UploadService(IDomainModel domainModel)
    {
        this(domainModel.getFileManager());
    }
    
    public UploadService(IFileManager fileManager)
    {
        this.fileManager = fileManager;
    }

    public String createSession(UserDTO user, String url)
    {
        return sessionManager.createSession(user, url).getSessionID();
    }

    public UploadStatus getUploadStatus(String uploadSessionID)
    {
        logInvocation(uploadSessionID, "Obtain upload status.");
        return sessionManager.getSession(uploadSessionID).getUploadStatus();
    }
    
    public UploadStatus defineUploadParameters(String uploadSessionID, String[] files, String recipients, String comment)
    {
        List<String> fileNames = extractFileNames(files);
        logInvocation(uploadSessionID, "Upload files " + fileNames);
        UploadSession session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = getStatusAndCheckState(session, INITIALIZED);
        
        status.setFiles(files);
        status.setUploadState(fileNames.isEmpty() ? FINISHED : READY_FOR_NEXT_FILE);
        session.setRecipients(StringUtilities.tokenize(recipients).toArray(new String[0]));
        session.setComment(comment);
        return status;
    }

    public UploadStatus cancel(String uploadSessionID)
    {
        logInvocation(uploadSessionID, "Cancel.");
        UploadSession session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = session.getUploadStatus();
        try
        {
            session.getRandomAccessFile().close();
        } catch (IOException ex)
        {
            operationLog.warn("Cannot close random access file", ex);
        }
        List<File> tempFiles = session.getTempFiles();
        for (File file : tempFiles)
        {
            if (file.exists() && file.delete() == false)
            {
                operationLog.warn("Cannot delete temporary file " + file);
            }
        }
        status.setUploadState(UploadState.ABORTED);
        sessionManager.removeSession(uploadSessionID);
        return status;
    }

    public void finish(String uploadSessionID, boolean successful)
    {
        logInvocation(uploadSessionID, successful ? "Successfully finished." : "Aborted.");
        sessionManager.getSession(uploadSessionID);
        sessionManager.removeSession(uploadSessionID);
    }

    public UploadStatus startUploading(String uploadSessionID)
    {
        UploadSession session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = getStatusAndCheckState(session, READY_FOR_NEXT_FILE);
        String nameOfCurrentFile = status.getNameOfCurrentFile();
        logInvocation(uploadSessionID, "Start uploading " + nameOfCurrentFile);
        String fileName =
                FilenameUtilities.ensureMaximumSize(nameOfCurrentFile,
                        MAX_FILENAME_LENGTH);
        File file = fileManager.createFile(session.getUser(), fileName);
        session.setFile(file);
        File tempFile = createTempFile(file);
        session.addTempFile(tempFile);
        RandomAccessFile randomAccessFile = createRandomAccessFile(tempFile);
        session.setRandomAccessFile(randomAccessFile);
        status.setUploadState(UPLOADING);
        return status;
    }

    public UploadStatus uploadBlock(String uploadSessionID, byte[] block, int blockSize, boolean lastBlock)
    {
        UploadSession session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = getStatusAndCheckState(session, UPLOADING);
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Upload " + (lastBlock ? "last block" : "block"));
        }
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
                createTempFile(file).renameTo(file);
                String contentType = FilenameUtilities.getMimeType(status.getNameOfCurrentFile());
                String comment = session.getComment();
                UserDTO user = session.getUser();
                String nameOfCurrentFile = status.getNameOfCurrentFile();
                String[] recipients = session.getRecipients();
                String url = session.getUrl();
                fileManager.registerFileLinkAndInformRecipients(user, nameOfCurrentFile, comment,
                        contentType, file, recipients, url);
                status.next();
            } else
            {
                status.setUploadState(UploadState.UPLOADING);
                status.setFilePointer(filePointer + blockSize);
            }
            return status;
        } catch (IOException ex)
        {
            throw new WrappedIOException(ex);
        }
    }
    
    private void logInvocation(String uploadSessionID, String message)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("[" + uploadSessionID + "]: " + message);
        }

    }

    private List<String> extractFileNames(String[] files)
    {
        List<String> fileNames = new ArrayList<String>();
        for (String path : files)
        {
            fileNames.add(FilenameUtils.getName(path));
        }
        return fileNames;
    }

    private UploadStatus getStatusAndCheckState(UploadSession session, UploadState expectedState)
    {
        UploadStatus status = session.getUploadStatus();
        UploadState state = status.getUploadState();
        if (state != expectedState)
        {
            throw new IllegalStateException("Expected state " + expectedState + " but was " + state);
        }
        return status;
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

    private File createTempFile(File file)
    {
        return new File(file.getParent(), PREFIX + file.getName());
    }

}
