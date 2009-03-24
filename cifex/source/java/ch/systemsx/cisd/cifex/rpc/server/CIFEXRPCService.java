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

import static ch.systemsx.cisd.cifex.rpc.UploadState.ABORTED;
import static ch.systemsx.cisd.cifex.rpc.UploadState.INITIALIZED;
import static ch.systemsx.cisd.cifex.rpc.UploadState.READY_FOR_NEXT_FILE;
import static ch.systemsx.cisd.cifex.rpc.UploadState.UPLOADING;
import static ch.systemsx.cisd.cifex.server.AbstractFileUploadServlet.MAX_FILENAME_LENGTH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.FileSizeExceededException;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.UploadState;
import ch.systemsx.cisd.cifex.rpc.UploadStatus;
import ch.systemsx.cisd.cifex.rpc.server.Session.Operation;
import ch.systemsx.cisd.cifex.server.AbstractCIFEXService;
import ch.systemsx.cisd.cifex.server.HttpUtils;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * The implementation of {@link ICIFEXRPCService}.
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
public class CIFEXRPCService extends AbstractCIFEXService implements IExtendedCIFEXRPCService
{
    private static final long MB = 1024 * 1024;

    public static final String PREFIX = "$";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXRPCService.class);

    private final SessionManager sessionManager;

    private final IFileManager fileManager;

    // used by spring in applicationContext.xml
    public CIFEXRPCService(final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLog,
            final IAuthenticationService externalAuthenticationService,
            final SessionManager sessionManager, final long cleaningTimeInterval,
            final int sessionExpirationPeriodMinutes, final String testingFlag)
    {
        this(domainModel.getFileManager(), domainModel, requestContextProvider, userBehaviorLog,
                externalAuthenticationService, sessionManager, cleaningTimeInterval,
                sessionExpirationPeriodMinutes, testingFlag);
    }

    public CIFEXRPCService(final IFileManager fileManager, final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLog,
            final IAuthenticationService externalAuthenticationService,
            final SessionManager sessionManager, final long cleaningTimeInterval,
            final int sessionExpirationPeriodMinutes, final String testingFlag)
    {
        super(domainModel, requestContextProvider, userBehaviorLog, externalAuthenticationService,
                createLoggingContextHandler(requestContextProvider), sessionExpirationPeriodMinutes);
        this.fileManager = fileManager;
        this.sessionManager = sessionManager;
        if ("true".equals(testingFlag))
        {
            UserDTO userDTO = new UserDTO();
            userDTO.setID(Long.parseLong(System.getProperty("test-user.id")));
            userDTO.setUserCode(System.getProperty("test-user.code"));
            sessionManager.createSession(userDTO, "test-url");
        } else
        {
            startSessionExpirationTimer(cleaningTimeInterval, sessionExpirationPeriodMinutes);
        }
    }

    private void startSessionExpirationTimer(final long cleaningTimeInterval,
            final long sessionExpirationPeriodMinutes)
    {
        final Timer timer = new Timer("Session Expiration", true);
        final long sessionExpirationPeriodMillis = 60L * 1000 * sessionExpirationPeriodMinutes;
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    final long now = System.currentTimeMillis();
                    final List<Session> sessionsToRemove = new LinkedList<Session>();
                    synchronized (sessionManager)
                    {
                        for (Session session : sessionManager.getAllSessions())
                        {
                            if (now - session.getLastActiveMillis() > sessionExpirationPeriodMillis)
                            {
                                sessionsToRemove.add(session);
                            }
                        }
                        for (Session session : sessionsToRemove)
                        {
                            logout(session, true);
                        }
                    }
                }
            }, 0L, cleaningTimeInterval);
    }

    //
    // Version
    //

    public int getVersion()
    {
        return VERSION;
    }

    //
    // Session
    //

    public void checkSession(String sessionID) throws InvalidSessionException
    {
        sessionManager.getSession(sessionID);
    }

    public String login(final String userCode, final String plainPassword)
            throws AuthorizationFailureException, EnvironmentFailureException
    {
        logInvocation("session initialization", "Try to login user '" + userCode + "'.");
        final UserDTO user = tryLoginUser(userCode, plainPassword, false);
        if (user == null)
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logFailedLoginAttempt(userCode);
            }
            throw new AuthorizationFailureException("Login failed: invalid user or password");
        }
        return createSession(user, getURLForEmail());
    }

    public void logout(String sessionID) throws InvalidSessionException
    {
        final Session session = sessionManager.getSession(sessionID);
        logout(session, false);
    }

    private void logout(Session session, boolean sessionExpired) throws InvalidSessionException
    {
        if (UploadState.RUNNING_STATES.contains(session.getUploadStatus().getUploadState()))
        {
            logInvocation(session.getSessionID(), "Cancel.");
        }
        cleanUpSession(session);
        sessionManager.removeSession(session.getSessionID(), sessionExpired);
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

    //
    // Info
    //

    public ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO[] listDownloadFiles(String sessionID)
            throws InvalidSessionException
    {
        final Session session = sessionManager.getSession(sessionID);
        final List<FileDTO> files =
                domainModel.getFileManager().listDownloadFiles(session.getUser().getID());
        return BeanUtils.createBeanArray(ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO.class,
                files);
    }

    //
    // Upload
    //

    public UploadStatus getUploadStatus(String sessionID) throws InvalidSessionException
    {
        return sessionManager.getSession(sessionID).getUploadStatus();
    }

    public void defineUploadParameters(String sessionID, String[] files, String recipients,
            String comment)
    {
        final Session session = sessionManager.getSession(sessionID);
        final List<String> fileNames = extractFileNames(files);
        logInvocation(sessionID, "Upload files " + fileNames);
        UploadStatus status = getStatusAndCheckState(session, INITIALIZED);
        status.setFiles(files);
        session.setRecipients(StringUtilities.tokenize(recipients).toArray(new String[0]));
        session.setComment(comment);
    }

    public void cancel(String sessionID) throws InvalidSessionException
    {
        final Session session = sessionManager.getSession(sessionID);
        logInvocation(sessionID, "Cancel.");
        cleanUpSession(session);
        session.getUploadStatus().setUploadState(ABORTED);
    }

    public void finish(String sessionID, boolean successful) throws InvalidSessionException
    {
        final Session session = sessionManager.getSession(sessionID);
        logInvocation(sessionID, successful ? "Successfully finished." : "Aborted.");
        final Operation operationOrNull = session.getOperation();
        if (operationOrNull != null && userBehaviorLogOrNull != null)
        {
            if (operationOrNull == Operation.UPLOAD)
            {
                // Nothing to do, has already been logged in last call to uploadBlock()
            } else if (operationOrNull == Operation.DOWNLOAD)
            {
                userBehaviorLogOrNull.logDownloadFileFinished(session.getFileInfo(), successful);
            }
        }
        cleanUpSession(session);
    }

    public void startUploading(String sessionID) throws InvalidSessionException
    {
        boolean success = false;
        String fileName = "UNKNOWN";
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            UploadStatus status = getStatusAndCheckState(session, READY_FOR_NEXT_FILE);
            String nameOfCurrentFile = status.getNameOfCurrentFile();
            logInvocation(sessionID, "Start uploading " + nameOfCurrentFile);
            fileName = FilenameUtilities.ensureMaximumSize(nameOfCurrentFile, MAX_FILENAME_LENGTH);
            File file = fileManager.createFile(session.getUser(), fileName);
            session.setOperation(Operation.UPLOAD);
            session.setFile(file);
            File tempFile = createTempFile(file);
            session.addTempFile(tempFile);
            RandomAccessFile randomAccessFile = createRandomAccessFile(tempFile);
            session.setRandomAccessFile(randomAccessFile);
            status.setUploadState(UPLOADING);
            success = true;
        } finally
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logUploadFileStart(fileName, success);
            }
        }
    }

    public void uploadBlock(String sessionID, long filePointer, byte[] block, boolean lastBlock)
            throws InvalidSessionException, IOExceptionUnchecked, FileSizeExceededException,
            IllegalStateException
    {
        final Session session = sessionManager.getSession(sessionID);
        final RandomAccessFile randomAccessFileOrNull = session.getRandomAccessFile();
        if (randomAccessFileOrNull == null)
        {
            throw new IllegalStateException(
                    "uploadBlock() called without previous startUploading()");
        }
        if (session.getUploadStatus().getUploadState() == UploadState.ABORTED)
        {
            return;
        }
        final UploadStatus status = getStatusAndCheckState(session, UPLOADING);
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Upload " + (lastBlock ? "last block" : "block"));
        }
        final long newFilePointer = filePointer + block.length;
        if (newFilePointer > domainModel.getBusinessContext().getMaxUploadRequestSizeInMB() * MB)
        {
            throw new FileSizeExceededException(domainModel.getBusinessContext()
                    .getMaxUploadRequestSizeInMB());
        }
        try
        {
            randomAccessFileOrNull.seek(filePointer);
            randomAccessFileOrNull.write(block, 0, block.length);
            if (lastBlock)
            {
                boolean success = false;
                try
                {
                    randomAccessFileOrNull.close();
                    final File file = session.getFile();
                    createTempFile(file).renameTo(file);
                    String contentType =
                            FilenameUtilities.getMimeType(status.getNameOfCurrentFile());
                    String comment = session.getComment();
                    final UserDTO user = session.getUser();
                    final String nameOfCurrentFile = status.getNameOfCurrentFile();
                    final String[] recipients = session.getRecipients();
                    final String url = session.getUrl();
                    final List<String> invalidUserIdentifiers =
                            fileManager.registerFileLinkAndInformRecipients(user,
                                    nameOfCurrentFile, comment, contentType, file, recipients, url);
                    success = true;
                    if (invalidUserIdentifiers.isEmpty() == false)
                    {
                        throw new UserFailureException("Some user identifiers are invalid: "
                                + CollectionUtils.abbreviate(invalidUserIdentifiers, 10));
                    }
                    status.next();
                } finally
                {
                    if (userBehaviorLogOrNull != null)
                    {
                        userBehaviorLogOrNull.logUploadFileFinished(session.getFile().getName(),
                                success);
                    }
                }
            } else
            {
                status.setFilePointer(filePointer + block.length);
            }
        } catch (Throwable th)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(th);
        }
    }

    public FileInfoDTO startDownloading(String sessionID, long fileID)
            throws InvalidSessionException, IOExceptionUnchecked
    {
        logInvocation(sessionID, "Start downloading file id=" + fileID);
        boolean success = false;
        FileDTO file = new FileDTO(null);
        file.setName("id:" + fileID);
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            final FileInformation fileInfo = fileManager.getFileInformation(fileID);
            if (fileInfo.isFileAvailable() == false)
            {
                throw new IOExceptionUnchecked(new IOException(fileInfo.getErrorMessage()));
            }
            file = fileInfo.getFileDTO();
            if (fileManager.isAllowedAccess(session.getUser(), file) == false)
            {
                // Note: we send back the exact same error message as for a file that cannot be
                // found.
                // We do not want to give information out on whether the file exists or not.
                throw new IOExceptionUnchecked(new IOException(Constants
                        .getErrorMessageForFileNotFound(fileID)));
            }
            session.setOperation(Operation.DOWNLOAD);
            session.setFileInfo(file);
            try
            {
                session.setRandomAccessFile(new RandomAccessFile(fileInfo.getFile(), "r"));
            } catch (FileNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            success = true;
            return BeanUtils.createBean(ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO.class,
                    fileInfo.getFileDTO());
        } finally
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logDownloadFileStart(file, success);
            }
        }
    }

    public byte[] downloadBlock(String sessionID, long filePointer, int blockSize)
            throws InvalidSessionException, IOExceptionUnchecked, IllegalStateException
    {
        final Session session = sessionManager.getSession(sessionID);
        final byte[] buf;
        try
        {
            final RandomAccessFile randomAccessFileOrNull = session.getRandomAccessFile();
            if (randomAccessFileOrNull == null)
            {
                throw new IllegalStateException(
                        "downloadBlock() called without previous startDownloading()");
            }
            if (session.getUploadStatus().getUploadState() == UploadState.ABORTED)
            {
                throw new IllegalStateException("downloadBlock() called when download was aborted");
            }
            final long fileSize = randomAccessFileOrNull.length();
            final long bytesLeft = fileSize - filePointer;
            if (bytesLeft < 0)
            {
                throw new IOExceptionUnchecked(new IOException("File pointer > file size."));
            }
            buf = new byte[(int) Math.min(blockSize, bytesLeft)];
            session.getRandomAccessFile().readFully(buf);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return buf;
    }

    private void logInvocation(String sessionID, String message)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("[" + sessionID + "]: " + message);
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

    private UploadStatus getStatusAndCheckState(Session session, UploadState... expectedStates)
    {
        final UploadStatus status = session.getUploadStatus();
        final UploadState state = status.getUploadState();
        final List<UploadState> states = Arrays.asList(expectedStates);
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
        final List<File> tempFiles = session.getTempFiles();
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
            throw new IOExceptionUnchecked(ex);
        }
    }

    private File createTempFile(File file)
    {
        return new File(file.getParent(), PREFIX + file.getName());
    }

}
