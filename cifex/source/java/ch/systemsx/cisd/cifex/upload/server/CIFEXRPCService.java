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
import static ch.systemsx.cisd.cifex.upload.UploadState.ABORTED;
import static ch.systemsx.cisd.cifex.upload.UploadState.FINISHED;
import static ch.systemsx.cisd.cifex.upload.UploadState.INITIALIZED;
import static ch.systemsx.cisd.cifex.upload.UploadState.READY_FOR_NEXT_FILE;
import static ch.systemsx.cisd.cifex.upload.UploadState.UPLOADING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.server.AbstractCIFEXService;
import ch.systemsx.cisd.cifex.server.HttpUtils;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class CIFEXRPCService extends AbstractCIFEXService implements IExtendedCIFEXRPCService
{
    public static final String PREFIX = "$";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXRPCService.class);

    private final UploadSessionManager sessionManager;

    private final IFileManager fileManager;

    // used externally by spring
    public CIFEXRPCService(final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLog,
            final IAuthenticationService externalAuthenticationService, final String testingFlag)
    {
        this(domainModel.getFileManager(), domainModel, requestContextProvider, userBehaviorLog,
                externalAuthenticationService, testingFlag);
    }

    public CIFEXRPCService(final IFileManager fileManager, final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLog,
            final IAuthenticationService externalAuthenticationService, final String testingFlag)
    {
        this(fileManager, new UploadSessionManager("true".equals(testingFlag)), domainModel,
                requestContextProvider, userBehaviorLog, externalAuthenticationService,
                createLoggingContextHandler(requestContextProvider));
        if ("true".equals(testingFlag))
        {
            UserDTO userDTO = new UserDTO();
            userDTO.setID(Long.parseLong(System.getProperty("test-user.id")));
            userDTO.setUserCode(System.getProperty("test-user.code"));
            sessionManager.createSession(userDTO, "test-url");
        }
    }

    private CIFEXRPCService(IFileManager fileManager, UploadSessionManager sessionManager,
            IDomainModel domainModel, IRequestContextProvider requestContextProvider,
            IUserActionLog userBehaviorLog, IAuthenticationService externalAuthenticationService,
            LoggingContextHandler loggingContextHandler)
    {
        super(domainModel, requestContextProvider, userBehaviorLog, externalAuthenticationService,
                loggingContextHandler);
        this.fileManager = fileManager;
        this.sessionManager = sessionManager;
    }

    public String login(final String userCode, final String plainPassword)
            throws EnvironmentFailureException, ch.systemsx.cisd.cifex.client.UserFailureException
    {
        logInvocation("session initialization", "Try to login user '" + userCode + "'.");
        UserDTO user = tryLoginUser(userCode, plainPassword);
        if (user == null)
        {
            throw new ch.systemsx.cisd.cifex.client.UserFailureException(
                    "Login failed: invalid user or password");
        }
        return createSession(user, getURLForEmail());
    }

    private String getURLForEmail()
    {
        HttpServletRequest request = requestContextProvider.getHttpServletRequest();
        return HttpUtils.getURLForEmail(request, domainModel.getBusinessContext());
    }

    public String createSession(UserDTO user, String url)
    {
        return sessionManager.createSession(user, url).getSessionID();
    }

    public UploadStatus getUploadStatus(String uploadSessionID)
    {
        return sessionManager.getSession(uploadSessionID).getUploadStatus();
    }

    public void defineUploadParameters(String uploadSessionID, String[] files, String recipients,
            String comment)
    {
        List<String> fileNames = extractFileNames(files);
        logInvocation(uploadSessionID, "Upload files " + fileNames);
        Session session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = getStatusAndCheckState(session, INITIALIZED);
        status.setFiles(files);
        status.setUploadState(fileNames.isEmpty() ? FINISHED : READY_FOR_NEXT_FILE);
        session.setRecipients(StringUtilities.tokenize(recipients).toArray(new String[0]));
        session.setComment(comment);
    }

    public void cancel(String uploadSessionID)
    {
        logInvocation(uploadSessionID, "Cancel.");
        Session session = sessionManager.getSession(uploadSessionID);
        cleanUpSession(session);
        session.getUploadStatus().setUploadState(ABORTED);
    }

    public void finish(String uploadSessionID, boolean successful)
    {
        logInvocation(uploadSessionID, successful ? "Successfully finished." : "Aborted.");
        Session session = sessionManager.getSession(uploadSessionID);
        cleanUpSession(session);
        if (successful == false)
        {
            session.getUploadStatus().setUploadState(UploadState.INITIALIZED);
        }
    }

    public void close(String uploadSessionID)
    {
        sessionManager.removeSession(uploadSessionID);
    }

    public void startUploading(String uploadSessionID)
    {
        Session session = sessionManager.getSession(uploadSessionID);
        UploadStatus status = getStatusAndCheckState(session, READY_FOR_NEXT_FILE);
        String nameOfCurrentFile = status.getNameOfCurrentFile();
        logInvocation(uploadSessionID, "Start uploading " + nameOfCurrentFile);
        String fileName =
                FilenameUtilities.ensureMaximumSize(nameOfCurrentFile, MAX_FILENAME_LENGTH);
        File file = fileManager.createFile(session.getUser(), fileName);
        session.setFile(file);
        File tempFile = createTempFile(file);
        session.addTempFile(tempFile);
        RandomAccessFile randomAccessFile = createRandomAccessFile(tempFile);
        session.setRandomAccessFile(randomAccessFile);
        status.setUploadState(UPLOADING);
    }

    public void uploadBlock(String uploadSessionID, byte[] block, int blockSize, boolean lastBlock)
    {
        Session session = sessionManager.getSession(uploadSessionID);
        if (session.getUploadStatus().getUploadState() == UploadState.ABORTED)
        {
            return;
        }
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
                List<String> invalidUserIdentifiers =
                        fileManager.registerFileLinkAndInformRecipients(user, nameOfCurrentFile,
                                comment, contentType, file, recipients, url);
                if (invalidUserIdentifiers.isEmpty() == false)
                {
                    throw new UserFailureException("Some user identifiers are invalid: "
                            + CollectionUtils.abbreviate(invalidUserIdentifiers, 10));
                }
                status.next();
            } else
            {
                status.setUploadState(UploadState.UPLOADING);
                status.setFilePointer(filePointer + blockSize);
            }
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

    private UploadStatus getStatusAndCheckState(Session session,
            UploadState... expectedStates)
    {
        UploadStatus status = session.getUploadStatus();
        UploadState state = status.getUploadState();
        List<UploadState> states = Arrays.asList(expectedStates);
        if (states.contains(state) == false)
        {
            throw new IllegalStateException("Expected one of " + states + " but was " + state);
        }
        return status;
    }

    private void cleanUpSession(Session session)
    {
        RandomAccessFile randomAccessFile = session.getRandomAccessFile();
        if (randomAccessFile != null)
        {
            try
            {
                randomAccessFile.close();
            } catch (IOException ex)
            {
                operationLog.warn("Cannot close random access file", ex);
            }
        }
        List<File> tempFiles = session.getTempFiles();
        for (File file : tempFiles)
        {
            if (file.exists() && file.delete() == false)
            {
                operationLog.warn("Cannot delete temporary file " + file);
            }
        }
        session.reset();
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
