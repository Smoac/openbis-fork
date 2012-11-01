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
import java.io.IOException;
import java.io.InputStream;
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
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.QuotaExceededException;
import ch.systemsx.cisd.cifex.rpc.io.ISimpleChecksummingProgressListener;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingInputStream;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingInputStream.ChecksumHandling;
import ch.systemsx.cisd.cifex.server.AbstractCIFEXService;
import ch.systemsx.cisd.cifex.server.HttpUtils;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.BeanUtils;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * The implementation of {@link ICIFEXRPCService}.
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
public class CIFEXRPCService extends AbstractCIFEXService implements IExtendedCIFEXRPCService
{
    /** The minimal version that the client needs to have to be able to talk to this server. */
    public static final int MIN_CLIENT_VERSION = 4;

    private static final long MB = 1024 * 1024;

    private static final long DELAY_AFTER_FAILED_LOGIN_MILLIS = 500L;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXRPCService.class);

    private final SessionManager sessionManager;

    private final IFileManager fileManager;

    private final IUserManager userManager;

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
            final IUserActionLog userActionLog,
            final IAuthenticationService externalAuthenticationService,
            final SessionManager sessionManager, final long cleaningTimeInterval,
            final int sessionExpirationPeriodMinutes, final String testingFlag)
    {
        super(domainModel, requestContextProvider, userActionLog, externalAuthenticationService,
                createLoggingContextHandler(requestContextProvider), sessionExpirationPeriodMinutes);
        this.fileManager = fileManager;
        this.userManager = domainModel.getUserManager();
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

    @Override
    public int getVersion()
    {
        return VERSION;
    }

    @Override
    public int getMinClientVersion()
    {
        return MIN_CLIENT_VERSION;
    }

    //
    // Session
    //

    @Override
    public void checkSession(String sessionID) throws InvalidSessionException
    {
        try
        {
            sessionManager.getSession(sessionID);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(userActionLog.getUserHostSessionDescription()
                        + "Keep alive ping: OK");
            }
        } catch (RuntimeException ex)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(userActionLog.getUserHostSessionDescription()
                        + "Keep alive ping: FAILED");
            }
            throw ex;
        }
    }

    @Override
    public String login(final String userCode, final String plainPassword)
            throws AuthorizationFailureException, EnvironmentFailureException
    {
        logInvocation("session initialization", "Try to login user '" + userCode + "'.");
        final UserDTO user = tryLoginUser(userCode, plainPassword);
        if (user == null)
        {
            userActionLog.logFailedLoginAttempt(userCode);
            // Delay reporting of failure in order to make brute force password attacks
            // unattractive.
            ConcurrencyUtilities.sleep(DELAY_AFTER_FAILED_LOGIN_MILLIS);
            throw new AuthorizationFailureException("Login failed: invalid user or password");
        }
        return createSession(user, getURLForEmail());
    }

    @Override
    public void logout(String sessionID) throws InvalidSessionException
    {
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            logout(session, false);
        } catch (InvalidSessionException ex)
        {
            // Silence this as it happens when clients log out twice "to be sure"
        }
    }

    private void logout(Session session, boolean sessionExpired) throws InvalidSessionException
    {
        sessionManager.removeSession(session.getSessionID(), sessionExpired);
    }

    private String getURLForEmail()
    {
        HttpServletRequest request = requestContextProvider.getHttpServletRequest();
        return HttpUtils.getURLForEmail(request, domainModel.getBusinessContext());
    }

    @Override
    public String createSession(UserDTO user, String url)
    {
        return sessionManager.createSession(user, url).getSessionID();
    }

    //
    // Info
    //

    @Override
    public FileInfoDTO[] listDownloadFiles(String sessionID) throws InvalidSessionException
    {
        final Session session = sessionManager.getSession(sessionID);
        final List<FileDTO> files =
                domainModel.getFileManager().listDownloadFiles(session.getUser().getID());
        return BeanUtils.createBeanArray(FileInfoDTO.class, files);
    }

    @Override
    public FileInfoDTO[] listOwnedFiles(String sessionID) throws InvalidSessionException,
            EnvironmentFailureException
    {
        final Session session = sessionManager.getSession(sessionID);
        final List<FileDTO> files =
                domainModel.getFileManager().listOwnedFiles(session.getUser().getID());
        return BeanUtils.createBeanArray(FileInfoDTO.class, files);
    }

    @Override
    public FileInfoDTO getFileInfo(String sessionID, long fileID) throws InvalidSessionException,
            IOExceptionUnchecked
    {
        logInvocation(sessionID, "Download file id=" + fileID);
        FileDTO file = new FileDTO();
        file.setName("id:" + fileID);
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
        final FileInfoDTO fileInfoDTO =
                BeanUtils.createBean(FileInfoDTO.class, fileInfo.getFileDTO());
        return fileInfoDTO;
    }

    //
    // Upload
    //

    @Override
    public long upload(String sessionID, FilePreregistrationDTO file, String comment,
            InputStream contentStream) throws InvalidSessionException
    {
        boolean success = false;
        String fileName = "UNKNOWN";
        FileDTO fileDTO = null;
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            final UserDTO requestUser = session.getUser();
            fileName = FilenameUtils.getName(file.getFilePathOnClient());
            logInvocation(sessionID, "Start uploading " + fileName);
            userActionLog.logUploadFileStart(fileName, null, 0L);
            checkQuota(sessionID, requestUser, file);
            final String contentType = FilenameUtilities.getMimeType(fileName);
            fileDTO =
                    fileManager.saveFile(requestUser, fileName, comment, contentType, file
                            .getFileSize(), contentStream);
            success = true;
            return fileDTO.getID();
        } finally
        {
            userActionLog.logUploadFileFinished(fileName, fileDTO, success);
        }
    }

    @Override
    public void resumeUpload(String sessionID, long fileId, long startPosition, String comment,
            InputStream contentStream) throws InvalidSessionException
    {
        boolean success = false;
        String fileName = "UNKNOWN";
        FileDTO fileDTO = null;
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            final UserDTO requestUser = session.getUser();
            final FileInformation fileInfo = fileManager.getFileInformation(fileId);
            fileName = fileInfo.getFileDTO().getName();
            fileDTO = fileInfo.getFileDTO();
            final File file = fileInfo.getFile();
            logInvocation(sessionID, "Resume uploading of file " + fileName + " (id=" + fileId
                    + ")");
            userActionLog.logUploadFileStart(fileName, fileDTO, startPosition);
            if (fileManager.isControlling(requestUser, fileInfo.getFileDTO()) == false)
            {
                operationLog
                        .error("[" + sessionID + "]: resumeUpload() failed because user "
                                + requestUser.getUserCode() + " is not allowed to control fileId="
                                + fileId);
                throw new AuthorizationFailureException("Insufficient privileges for user "
                        + requestUser.getUserCode() + ".");
            }
            fileManager.resumeSaveFile(requestUser, fileDTO, file, comment, startPosition,
                    contentStream);
            success = true;
        } finally
        {
            userActionLog.logUploadFileFinished(fileName, fileDTO, success);
        }
    }

    private void checkQuota(String sessionID, final UserDTO requestUser, FilePreregistrationDTO file)
    {
        domainModel.getUserManager().refreshQuotaInformation(requestUser);
        final boolean countOK =
                (requestUser.getMaxFileCountPerQuotaGroup() == null)
                        || (requestUser.getCurrentFileCount() + 1 <= requestUser
                                .getMaxFileCountPerQuotaGroup());
        final boolean sizeOK =
                (requestUser.getMaxFileSizePerQuotaGroupInMB() == null)
                        || (requestUser.getCurrentFileSize() + file.getFileSize() <= requestUser
                                .getMaxFileSizePerQuotaGroupInMB()
                                * MB);
        if ((countOK && sizeOK) == false)
        {
            final double currentFileSizeInMB = ((double) requestUser.getCurrentFileSize()) / MB;
            final QuotaExceededException excetion =
                    new QuotaExceededException(requestUser.getMaxFileCountPerQuotaGroup(),
                            requestUser.getMaxFileSizePerQuotaGroupInMB(), requestUser
                                    .getCurrentFileCount(), currentFileSizeInMB);
            operationLog.error("[" + sessionID + "]: " + excetion.getMessage());
            throw excetion;
        }
    }

    @Override
    public FileInfoDTO tryGetUploadResumeCandidate(String sessionID,
            FilePreregistrationDTO fileSpecs)
    {
        assert sessionID != null;
        assert fileSpecs != null;
        final Session session = sessionManager.getSession(sessionID);
        final String fileName = FilenameUtils.getName(fileSpecs.getFilePathOnClient());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Try to get upload resume candidate for file=" + fileName
                    + " with complete size=" + fileSpecs.getFileSize());
        }
        final UserDTO user = session.getUser();
        return BeanUtils.createBean(FileInfoDTO.class, fileManager.tryGetUploadResumeCandidate(user
                .getID(), fileName, fileSpecs.getFileSize()));
    }

    @Override
    public void shareFiles(String sessionID, List<Long> fileIDs, String recipients)
    {
        assert sessionID != null;

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
                fileManager.shareFilesWith(url, user, recipientList, files, comment, userActionLog);
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
            throws AuthorizationFailureException
    {
        for (FileDTO file : files)
        {
            if (fileManager.isControlling(user, file) == false)
            {
                operationLog
                        .error("[" + sessionID + "]: shareFiles() failed because user "
                                + user.getUserCode() + " is not allowed to control file="
                                + file.toString());
                userActionLog.logShareFilesAuthorizationFailure(files, recipientList);
                throw new AuthorizationFailureException("Insufficient privileges for user "
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

    @Override
    public void deleteFile(String sessionID, long fileId) throws InvalidSessionException
    {
        assert sessionID != null;

        logInvocation(sessionID, "Delete file id=" + fileId);
        final FileInformation fileInfo = fileManager.getFileInformationFilestoreUnimportant(fileId);
        if (fileInfo.isFileAvailable() == false)
        {
            throw UserFailureException.fromTemplate(
                    "File with id %d does not exist on the server.", fileId);
        }
        final FileDTO file = fileInfo.getFileDTO();
        boolean success = false;
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            final UserDTO user = session.getUser();
            if (fileManager.isControlling(user, file) == false)
            {
                operationLog
                        .error("[" + sessionID + "]: deleteFile() failed because user "
                                + user.getUserCode() + " is not allowed to control file="
                                + file.toString());
                throw new AuthorizationFailureException("Insufficient privileges for user "
                        + user.getUserCode() + ".");
            }
            fileManager.deleteFile(file);
            success = true;
        } finally
        {
            userActionLog.logDeleteFile(file, success);
        }
    }

    @Override
    public InputStream download(final String sessionID, long fileID, long startPosition)
            throws InvalidSessionException, IOExceptionUnchecked
    {
        logInvocation(sessionID, "Download file id=" + fileID);
        FileDTO file = new FileDTO();
        file.setName("id:" + fileID);
        final Session session = sessionManager.getSession(sessionID);
        final FileInformation fileInfo = fileManager.getFileInformation(fileID);
        if (fileInfo.isFileAvailable() == false)
        {
            userActionLog.logDownloadFileFailedNotFound(fileInfo.getFileDTO());
            throw new IOExceptionUnchecked(new IOException(fileInfo.getErrorMessage()));
        }
        file = fileInfo.getFileDTO();
        if (fileManager.isAllowedAccess(session.getUser(), file) == false)
        {
            userActionLog.logDownloadFileFailedNotAuthorized(fileInfo.getFileDTO());
            // Note: we send back the exact same error message as for a file that cannot be
            // found.
            // We do not want to give information out on whether the file exists or not.
            throw new IOExceptionUnchecked(new IOException(Constants
                    .getErrorMessageForFileNotFound(fileID)));
        }
        userActionLog.logDownloadFileStart(file, startPosition);
        try
        {
            final FileDTO finalFile = file;
            final ResumingAndChecksummingInputStream fileContent =
                    new ResumingAndChecksummingInputStream(fileInfo.getFile(), Long.MAX_VALUE,
                            new ISimpleChecksummingProgressListener()
                                {
                                    @Override
                                    public void update(long bytesRead, int crc32Value)
                                    {
                                        userActionLog.logDownloadFileFinished(finalFile, true);
                                    }

                                    @Override
                                    public void exceptionThrown(IOException e)
                                    {
                                        operationLog.error("[" + sessionID
                                                + "]: download() failed.", e);
                                        userActionLog.logDownloadFileFinished(finalFile, false);
                                    }
                                }, startPosition, ChecksumHandling.DONT_COMPUTE);
            return fileContent;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void logInvocation(String sessionID, String message)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("[" + sessionID + "]: " + message);
        }
    }

    @Override
    public void setSessionUser(String sessionID, String userCode)
    {
        boolean success = false;
        String oldUserCode = "UNKNOWN";
        try
        {
            final Session session = sessionManager.getSession(sessionID);
            oldUserCode = session.getUser().getUserCode();
            if (session.getUser().isAdmin() == false)
            {
                throw new AuthorizationFailureException("Changing session user not allowed.");
            }
            final Set<String> allowedIpsForSetSessionUser =
                    domainModel.getBusinessContext().getAllowedIPsForSetSessionUser();
            if (allowedIpsForSetSessionUser.contains(remoteHostProvider.getRemoteHost()) == false)
            {
                throw new AuthorizationFailureException("Changing session user not allowed.");
            }
            final UserDTO newUserOrNull = userManager.tryFindUserByCode(userCode);
            if (newUserOrNull == null)
            {
                throw new IllegalArgumentException("User '" + userCode + "' is unknown.");
            }
            session.setUser(newUserOrNull);
            success = true;
        } finally
        {
            userActionLog.logSetSessionUser(oldUserCode, userCode, success);
        }
    }

}
