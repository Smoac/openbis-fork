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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.FileSizeExceededException;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.rpc.server.Session.Operation;
import ch.systemsx.cisd.cifex.server.AbstractCIFEXService;
import ch.systemsx.cisd.cifex.server.HttpUtils;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.PreCreatedFileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
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
    /** The minimal version that the client needs to have to be able to talk to this server. */
    public static final int MIN_CLIENT_VERSION = 3;

    private static final long MB = 1024 * 1024;

    private static final long DELAY_AFTER_FAILED_LOGIN_MILLIS = 500L;

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

    public int getMinClientVersion()
    {
        return MIN_CLIENT_VERSION;
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
            // Delay reporting of failure in order to make brute force password attacks
            // unattractive.
            ConcurrencyUtilities.sleep(DELAY_AFTER_FAILED_LOGIN_MILLIS);
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
        if (session.getOperation() != Operation.NONE)
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

    public void finish(String sessionID, boolean successful) throws InvalidSessionException
    {
        final Session session = sessionManager.getSession(sessionID);
        final Operation operationOrNull = session.getOperation();
        if (operationOrNull != null)
        {
            logInvocation(sessionID, operationOrNull
                    + (successful ? " successfully finished." : " aborted."));
        } else
        {
            operationLog.warn("[" + sessionID
                    + "]: finish() called when no operation was in progress.");
        }
        if (operationOrNull != null && userBehaviorLogOrNull != null)
        {
            if (operationOrNull == Operation.UPLOAD)
            {
                userBehaviorLogOrNull
                        .logUploadFileFinished(session.getFile().getName(), successful);
            } else if (operationOrNull == Operation.DOWNLOAD)
            {
                userBehaviorLogOrNull.logDownloadFileFinished(session.getFileDTO(), successful);
            }
        }
        cleanUpSession(session);
    }

    //
    // Info
    //

    public FileInfoDTO[] listDownloadFiles(String sessionID) throws InvalidSessionException
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

    public long startUploading(String sessionID, FilePreregistrationDTO file, String comment)
            throws InvalidSessionException
    {
        boolean success = false;
        String fileName = "UNKNOWN";
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            fileName = FilenameUtils.getName(file.getFilePathOnClient());
            logInvocation(sessionID, "Start uploading " + fileName);
            final PreCreatedFileDTO fileInfo =
                    fileManager.createFile(session.getUser(), file, comment);
            session.startUploadOperation();
            session.setFile(fileInfo.getFile());
            session.setFileDTO(fileInfo.getFileDTO());
            RandomAccessFile randomAccessFile = createRandomAccessFile(fileInfo.getFile());
            session.setRandomAccessFile(randomAccessFile);
            success = true;
            return fileInfo.getFileDTO().getID();
        } finally
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logUploadFileStart(fileName, success);
            }
        }
    }

    public void uploadBlock(String sessionID, long filePointer, int runningCrc32Value, byte[] block)
            throws InvalidSessionException, IOExceptionUnchecked, FileSizeExceededException,
            IllegalStateException
    {
        final Session session = sessionManager.getSession(sessionID);
        if (session.isUploadInProgress() == false)
        {
            throw new IllegalStateException("uploadBlock() called, but no upload in progress.");
        }
        final RandomAccessFile randomAccessFileOrNull = session.getRandomAccessFile();
        if (randomAccessFileOrNull == null)
        {
            throw new IllegalStateException(
                    "uploadBlock() called but no random access file defined.");
        }
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Upload block of file=" + session.getFile().getPath()
                    + " at position=" + filePointer);
        }
        session.updateUploadProgress(filePointer, runningCrc32Value, block);
        int maxUploadSize = getMaxUploadSize(session);
        if (session.getFileDTO().getSize() > maxUploadSize * MB)
        {
            throw new FileSizeExceededException(maxUploadSize);
        }
        fileManager.updateUploadProgress(session.getFileDTO());
        try
        {
            randomAccessFileOrNull.seek(filePointer);
            randomAccessFileOrNull.write(block, 0, block.length);
        } catch (Throwable th)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(th);
        }
    }

    public void shareFiles(String sessionID, List<Long> fileIDs, String recipients)
    {
        logInvocation(sessionID, "Share files=" + CollectionUtils.abbreviate(fileIDs, 3)
                + " with recipients='" + recipients + "'");
        final Session session = sessionManager.getSession(sessionID);
        final List<String> recipientList = StringUtilities.tokenize(recipients);
        final UserDTO user = session.getUser();
        final String url = session.getUrl();
        final Collection<FileDTO> files = new ArrayList<FileDTO>(fileIDs.size());
        for (long fileId : fileIDs)
        {
            try
            {
                files.add(fileManager.getFileInformation(fileId).getFileDTO());
            } catch (IllegalStateException ex)
            {
                operationLog.error("[" + sessionID + "]: shareFiles() failed because fileId="
                        + fileId + " is invalid: " + ex.getMessage());
                throw ex;
            }
        }
        final String comment = createCommentForSharingEmail(files);
        checkIfUserIsControllingFiles(sessionID, user, files, recipientList);
        final List<String> invalidUserIdentifiers =
                fileManager.shareFilesWith(url, user, recipientList, files, comment);
        if (invalidUserIdentifiers.isEmpty() == false)
        {
            throw new UserFailureException("Some user identifiers are invalid: "
                    + CollectionUtils.abbreviate(invalidUserIdentifiers, 10));
        }
    }

    /**
     * Checks if <var>user</var> is controlling all <var>files</var> and throws an
     * {@link InsufficientPrivilegesException} otherwise.
     */
    private void checkIfUserIsControllingFiles(String sessionID, final UserDTO user,
            final Collection<FileDTO> files, final List<String> recipientList)
            throws InsufficientPrivilegesException
    {
        for (FileDTO file : files)
        {
            if (fileManager.isControlling(user, file) == false)
            {
                operationLog.error("[" + sessionID + "]: shareFiles() failed because user "
                        + user.getUserCode() + " is not allowed control of fileId="
                        + file.toString());
                if (userBehaviorLogOrNull != null)
                {
                    userBehaviorLogOrNull.logShareFilesAuthorizationFailure(files, recipientList);
                }
                throw new InsufficientPrivilegesException("Insufficient privileges for user "
                        + user.getUserCode() + ".");
            }
        }
    }

    private String createCommentForSharingEmail(Collection<FileDTO> files)
    {
        final Set<String> comments = new HashSet<String>();
        for (FileDTO file : files)
        {
            if (StringUtils.isNotBlank(file.getComment()))
            {
                comments.add(file.getComment());
            }
        }
        if (comments.size() == 1)
        {
            return comments.iterator().next();
        } else
        {
            final StringBuilder builder = new StringBuilder();
            for (String comment : comments)
            {
                builder.append(comment);
                builder.append('\n');
            }
            return builder.toString();
        }
    }

    public FileInfoDTO startDownloading(String sessionID, long fileID)
            throws InvalidSessionException, IOExceptionUnchecked
    {
        logInvocation(sessionID, "Start downloading file id=" + fileID);
        boolean success = false;
        FileDTO file = new FileDTO();
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
            session.startDownloadOperation();
            session.setFileDTO(file);
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
        if (session.isDownloadInProgress() == false)
        {
            throw new IllegalStateException("downloadBlock() called, but no download in progress.");
        }
        final byte[] buf;
        try
        {
            final RandomAccessFile randomAccessFileOrNull = session.getRandomAccessFile();
            if (randomAccessFileOrNull == null)
            {
                throw new IllegalStateException(
                        "downloadBlock() called when no random access file is present in session.");
            }
            final long fileSize = randomAccessFileOrNull.length();
            final long bytesLeft = fileSize - filePointer;
            if (bytesLeft < 0)
            {
                throw new IOExceptionUnchecked(new IOException("File pointer > file size."));
            }
            buf = new byte[(int) Math.min(blockSize, bytesLeft)];
            randomAccessFileOrNull.seek(filePointer);
            randomAccessFileOrNull.readFully(buf);
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

    private int getMaxUploadSize(final Session session)
    {
        Long usersMaxUploadSize = session.getUser().getMaxUploadRequestSizeInMB();
        if (usersMaxUploadSize == null)
        {
            return domainModel.getBusinessContext().getMaxUploadRequestSizeInMB();
        }
        return usersMaxUploadSize.intValue();
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

}
