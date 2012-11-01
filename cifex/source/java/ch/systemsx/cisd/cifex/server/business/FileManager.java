/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business;

import static ch.systemsx.cisd.cifex.server.AbstractFileUploadDownloadServlet.MAX_FILENAME_LENGTH;
import static ch.systemsx.cisd.cifex.server.util.ExpirationUtilities.fixExpiration;
import static ch.systemsx.cisd.common.time.DateTimeUtils.extendUntilEndOfDay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.CRCCheckumMismatchException;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.io.CopyUtils;
import ch.systemsx.cisd.cifex.rpc.io.ISimpleChecksummingProgressListener;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingOutputStream;
import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dto.BasicFileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.cifex.server.util.ExpirationUtilities;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMapNonUniqueKey;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.reflection.BeanUtils;
import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.common.string.StringUtilities.IUniquenessChecker;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * The only <code>IFileManager</code> implementation.
 * 
 * @author Christian Ribeaud
 */
final class FileManager extends AbstractManager implements IFileManager
{
    private static final String FILE_CHECKSUM_TEMPLATE = "File %s has crc32 checksum %x.";

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final int PROGRESS_UPDATE_CHUNK_SIZE = 128 * 1024; // 128 kB

    private static final int PROGRESS_UPDATE_MIN_INTERVAL_MILLIS = 2 * 1000; // 2 seconds

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileManager.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            FileManager.class);

    private final ITriggerManager triggerManager;

    private final IUserManager userManager;

    FileManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IUserManager userManager, final IBusinessContext businessContext,
            ITriggerManager triggerManager)
    {
        this(daoFactory, boFactory, userManager, businessContext, triggerManager,
                SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    FileManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IUserManager userManager, final IBusinessContext businessContext,
            ITriggerManager triggerManager, ITimeProvider timeProvider)
    {
        super(daoFactory, boFactory, businessContext, timeProvider);
        this.userManager = userManager;
        this.triggerManager = triggerManager;
    }

    /**
     * Whether given <var>userDTO</var> could be found in list of sharing users.
     */
    private final static boolean containsUser(final UserDTO userDTO,
            final List<UserDTO> sharingUsers)
    {
        for (final UserDTO user : sharingUsers)
        {
            if (user.getID().equals(userDTO.getID()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes file with given path from the file system.
     * 
     * @returns <code>true</code> if the file has been successfully deleted.
     */
    private final boolean deleteFromFileSystem(final String path)
    {
        final File file = new File(businessContext.getFileStore(), path);
        return deleteFromFileSystem(file);
    }

    /**
     * Deletes file with given path from the file system.
     * 
     * @returns <code>true</code> if the file has been successfully deleted.
     */
    private final boolean deleteFromFileSystem(final File file)
    {
        if (file.exists())
        {
            if (file.delete())
            {
                return true;
            } else
            {
                notificationLog.error("File [" + file.getAbsolutePath() + "] can not be deleted.");
                return false;
            }
        } else
        {
            operationLog.warn("File [" + file.getAbsolutePath()
                    + "] requested to be deleted, but doesn't exist.");
            return true;
        }
    }

    //
    // IFileManager
    //

    @Override
    public final List<FileDTO> listDownloadFiles(final long userId)
    {
        return daoFactory.getFileDAO().listDownloadFiles(userId);
    }

    @Override
    @Transactional
    public final List<FileDTO> listOwnedFiles(final long userId)
    {
        final List<FileDTO> list =
                daoFactory.getFileDAO().listDirectlyAndIndirectlyOwnedFiles(userId);
        final Map<Long, String> idToCodeMap = new HashMap<Long, String>();
        for (FileDTO file : list)
        {
            for (UserDTO user : file.getSharingUsers())
            {
                user.setUserCode(getUserCodeForId(idToCodeMap, user.getID()));
            }
        }
        return list;
    }

    @Override
    @Transactional
    public final void deleteExpiredFiles(final IUserActionLog logOrNull)
    {
        final List<FileDTO> expiredFiles = daoFactory.getFileDAO().getExpiredFiles();
        if (operationLog.isInfoEnabled() && expiredFiles.size() > 0)
        {
            operationLog.info("Found " + expiredFiles.size() + " expired files.");
        }
        RuntimeException firstExceptionOrNull = null;
        for (final FileDTO file : expiredFiles)
        {
            try
            {
                boolean success = daoFactory.getFileDAO().deleteFile(file.getID());
                if (success)
                {
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info("Expired file '" + file.getPath()
                                + "' removed from database.");
                    }
                } else
                {
                    operationLog.warn("Expired file '" + file.getPath()
                            + "' could not be found in the database.");
                }
                success &= deleteFromFileSystem(file.getPath());
                if (logOrNull != null)
                {
                    logOrNull.logExpireFile(file, success);
                }
            } catch (final RuntimeException ex)
            {
                if (logOrNull != null)
                {
                    logOrNull.logExpireFile(file, false);
                }
                operationLog.error("Error deleting file '" + file.getPath() + "'.", ex);
                if (firstExceptionOrNull == null)
                {
                    firstExceptionOrNull = ex;
                }
            }
        }
        // Rethrow exception, if any
        if (firstExceptionOrNull != null)
        {
            throw firstExceptionOrNull;
        }
    }

    @Override
    public final FileInformation getFileInformation(final long fileId)
    {
        return getFileInformation(fileId, true);
    }

    @Override
    public final FileInformation getFileInformationFilestoreUnimportant(final long fileId)
    {
        return getFileInformation(fileId, false);
    }

    private final FileInformation getFileInformation(final long fileId,
            final boolean fileStoreImportant)
    {
        final FileDTO fileDTOOrNull = daoFactory.getFileDAO().tryGetFile(fileId);
        File realFile = null;
        if (fileDTOOrNull == null)
        {
            return new FileInformation(fileId, Constants.getErrorMessageForFileNotFound(fileId));
        } else if (fileStoreImportant)
        {
            realFile = new java.io.File(businessContext.getFileStore(), fileDTOOrNull.getPath());
            if (realFile.exists() == false)
            {
                if (businessContext.getFileStore().canRead() == false)
                {
                    notificationLog.error("CIFEX file store is not readable any more ("
                            + businessContext.getFileStore().getAbsolutePath() + ")");
                    return new FileInformation(
                            fileId,
                            String.format(
                                    "Unexpected: File '%s' [id=%d] can not be read (CIFEX file store unreadable).",
                                    realFile.getPath(), fileId));
                } else
                {
                    return new FileInformation(fileId, String.format(
                            "Unexpected: File '%s' [id=%d] is missing in CIFEX file store.",
                            realFile.getPath(), fileId));
                }
            }
        }
        return new FileInformation(fileId, fileDTOOrNull, realFile);
    }

    @Override
    public final FileContent getFileContent(final FileDTO fileDTO)
    {
        final File realFile = getRealFile(fileDTO);
        try
        {
            final FileContent content =
                    new FileContent(BeanUtils.createBean(BasicFileDTO.class, fileDTO),
                            new FileInputStream(realFile));
            return content;
        } catch (final FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public File getRealFile(final FileDTO fileDTO)
    {
        final File realFile = new File(businessContext.getFileStore(), fileDTO.getPath());
        if (realFile.exists() == false)
        {
            throw new IllegalStateException(String.format(
                    "File '%s' does not exist on the file system.", realFile.getAbsolutePath()));
        }
        return realFile;
    }

    @Override
    public final boolean isAllowedAccess(final UserDTO userDTO, final FileDTO fileDTO)
    {
        if (isControlling(userDTO, fileDTO))
        {
            return true;
        }
        final List<UserDTO> sharingUsers = fileDTO.getSharingUsers();
        return containsUser(userDTO, sharingUsers);
    }

    @Override
    public boolean isControlling(final UserDTO userDTO, final FileDTO fileDTO)
    {
        // Admins are in control of all files.
        if (userDTO.isAdmin())
        {
            return true;
        }
        // The owner of a file is in control of it.
        if (userDTO.getID().equals(fileDTO.getOwnerId()))
        {
            return true;
        }
        // The registrator of the owner of a file is in control of it, too.
        final UserDTO owner = fileDTO.getOwner();
        if (owner != null && owner.getRegistrator() != null
                && userDTO.getID().equals(owner.getRegistrator().getID()))
        {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public final FileDTO saveFile(final UserDTO user, final String fileName, final String comment,
            final String contentTypeOrNull, final InputStream inputStream)
    {
        assert user != null : "Unspecified user.";
        assert user.getEmail() != null : "Unspecified email of user " + user;
        assert StringUtils.isNotBlank(fileName) : "Unspecified file name.";
        assert inputStream != null : "Unspecified input stream.";

        final String contentType =
                (contentTypeOrNull != null) ? contentTypeOrNull : DEFAULT_CONTENT_TYPE;
        final File file = createFile(user, fileName);
        try
        {
            ResumingAndChecksummingOutputStream outputStream = null;
            try
            {
                outputStream = new ResumingAndChecksummingOutputStream(file, 1024, null);
                IOUtils.copyLarge(inputStream, outputStream);
                outputStream.close();
                final long byteCount = outputStream.getByteCount();
                final int crc32Value = outputStream.getCrc32Value();
                if (byteCount > 0)
                {
                    final FileDTO fileDTO =
                            registerFile(user, fileName, comment, contentType, file, byteCount,
                                    crc32Value);
                    return fileDTO;
                } else
                {
                    deleteFromFileSystem(file);
                    throwExceptionOnFileDoesNotExist(fileName);
                    return null; // never reached
                }
            } catch (final IOException ex)
            {
                operationLog.error("Error saving file " + fileName + ": " + ex.getMessage());
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Error saving file '%s' (Is it a file?).", fileName);
            } finally
            {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        } catch (final RuntimeException e)
        {
            deleteFromFileSystem(file);
            throw e;
        }
    }

    @Override
    public final FileDTO saveFile(final UserDTO user, final String fileName, final String comment,
            final String contentTypeOrNull, final long fileSize, final InputStream inputStream)
    {
        assert user != null : "Unspecified user.";
        assert user.getEmail() != null : "Unspecified email of user " + user;
        assert StringUtils.isNotBlank(fileName) : "Unspecified file name.";
        assert inputStream != null : "Unspecified input stream.";

        final PreCreatedFileDTO fileContainer = createFile(user, fileName, fileSize, comment);
        final File file = fileContainer.getFile();
        final FileDTO fileDTO = fileContainer.getFileDTO();
        ResumingAndChecksummingOutputStream outputStream = null;
        try
        {
            outputStream =
                    new ResumingAndChecksummingOutputStream(file, PROGRESS_UPDATE_CHUNK_SIZE,
                            new ISimpleChecksummingProgressListener()
                                {
                                    long lastUpdated = System.currentTimeMillis();

                                    @Override
                                    public void update(long bytesWritten, int crc32Value)
                                    {
                                        final long now = System.currentTimeMillis();
                                        if (now - lastUpdated > PROGRESS_UPDATE_MIN_INTERVAL_MILLIS
                                                && bytesWritten != fileSize)
                                        {
                                            fileDTO.setSize(bytesWritten);
                                            fileDTO.setCrc32Value(crc32Value);
                                            updateUploadProgress(fileDTO, user);
                                            lastUpdated = now;
                                        }
                                    }

                                    @Override
                                    public void exceptionThrown(IOException e)
                                    {
                                    }
                                });
            final int remoteCrc32Value =
                    CopyUtils.copyAndReturnChecksum(inputStream, outputStream, fileSize, 0L);
            outputStream.close();
            final long byteCount = outputStream.getByteCount();
            fileDTO.setSize(byteCount);
            final int crc32Value = outputStream.getCrc32Value();
            fileDTO.setCrc32Value(crc32Value);
            if (remoteCrc32Value != crc32Value)
            {
                deleteFromFileSystem(file);
                daoFactory.getFileDAO().deleteFile(fileDTO.getID());
                throw new CRCCheckumMismatchException(fileName, crc32Value, remoteCrc32Value);
            }
            if (byteCount != fileSize)
            {
                deleteFromFileSystem(file);
                daoFactory.getFileDAO().deleteFile(fileDTO.getID());
                throw EnvironmentFailureException.fromTemplate(
                        "Wrong file size of file %s [expected: %d, found: %d]", fileName, fileSize,
                        byteCount);
            }
            operationLog.info(String.format(FILE_CHECKSUM_TEMPLATE, fileName, crc32Value));
            // Set file to 'complete' in database.
            updateUploadProgress(fileDTO, user);
            return fileDTO;
        } catch (final IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Error saving file '%s'.", fileName);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public final void resumeSaveFile(final UserDTO user, final FileDTO fileDTO, final File file,
            final String comment, final long startPos, final InputStream inputStream)
            throws IllegalArgumentException
    {
        assert user != null : "Unspecified user.";
        assert user.getEmail() != null : "Unspecified email of user " + user;
        assert fileDTO != null : "Unspecified file link.";
        assert file != null : "Unspecified file.";
        assert inputStream != null : "Unspecified input stream.";

        final String fileName = fileDTO.getName();
        final long fileSize = fileDTO.getCompleteSize();
        if (startPos > fileDTO.getSize())
        {
            throw new IllegalArgumentException(String.format(
                    "File id=%d: requested start position %d for resume is larger than "
                            + "transferred size %d.", fileDTO.getID(), startPos, fileDTO.getSize()));
        }
        // Update comment if it has changed.
        if (StringUtils.isNotBlank(comment)
                && ObjectUtils.equals(comment, fileDTO.getComment()) == false)
            daoFactory.getFileDAO().updateFileUserEdit(fileDTO.getID(), fileName, comment,
                    calculateNewExpirationDate(fileDTO, user));
        ResumingAndChecksummingOutputStream outputStream = null;
        try
        {
            final int partialCrc32Value =
                    (fileDTO.getCrc32Value() != null) ? fileDTO.getCrc32Value() : 0;
            outputStream =
                    new ResumingAndChecksummingOutputStream(file, PROGRESS_UPDATE_CHUNK_SIZE,
                            new ISimpleChecksummingProgressListener()
                                {
                                    long lastUpdated = System.currentTimeMillis();

                                    @Override
                                    public void update(long bytesWritten, int crc32Value)
                                    {
                                        final long now = System.currentTimeMillis();
                                        if (now - lastUpdated > PROGRESS_UPDATE_MIN_INTERVAL_MILLIS
                                                && bytesWritten != fileSize)
                                        {
                                            fileDTO.setSize(bytesWritten);
                                            fileDTO.setCrc32Value(crc32Value);
                                            updateUploadProgress(fileDTO, user);
                                            lastUpdated = now;
                                        }
                                    }

                                    @Override
                                    public void exceptionThrown(IOException e)
                                    {
                                    }
                                }, startPos, partialCrc32Value);
            final int remoteCrc32Value =
                    CopyUtils.copyAndReturnChecksum(inputStream, outputStream, fileSize, startPos);
            outputStream.close();
            final long byteCount = outputStream.getByteCount();
            fileDTO.setSize(byteCount);
            final int crc32Value = outputStream.getCrc32Value();
            fileDTO.setCrc32Value(crc32Value);
            if (remoteCrc32Value != crc32Value)
            {
                deleteFromFileSystem(file);
                daoFactory.getFileDAO().deleteFile(fileDTO.getID());
                throw new CRCCheckumMismatchException(fileName, crc32Value, remoteCrc32Value);
            }
            if (byteCount != fileSize)
            {
                deleteFromFileSystem(file);
                daoFactory.getFileDAO().deleteFile(fileDTO.getID());
                throw EnvironmentFailureException.fromTemplate(
                        "Wrong file size of file %s [expected: %d, found: %d]", fileName, fileSize,
                        byteCount);
            }
            operationLog.info(String.format(FILE_CHECKSUM_TEMPLATE, fileName, crc32Value));
            // Set file to 'complete' in database.
            updateUploadProgress(fileDTO, user);
        } catch (final IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Error saving file '%s'.", fileName);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    @Transactional
    public List<String> registerFileLinkAndInformRecipients(UserDTO user, String fileName,
            String comment, String contentTypeOrNull, File file, int crc32Value,
            String[] recipients, String url, final IUserActionLog logOrNull)
    {
        final String contentType =
                (contentTypeOrNull != null) ? contentTypeOrNull : DEFAULT_CONTENT_TYPE;
        final FileDTO fileDTO =
                registerFile(user, fileName, comment, contentType, file, file.length(), crc32Value);
        return shareFilesWith(url, user, Arrays.asList(recipients), Collections.singleton(fileDTO),
                comment, logOrNull);
    }

    private FileDTO registerFile(final UserDTO user, final String fileName, final String comment,
            final String contentType, final File file, final long byteCount, final int crc32Value)
    {
        operationLog.info(String.format(FILE_CHECKSUM_TEMPLATE, fileName, crc32Value));
        final FileDTO fileDTO = new FileDTO(user);
        fileDTO.setName(FilenameUtilities.ensureMaximumSize(fileName, MAX_FILENAME_LENGTH));
        fileDTO.setContentType(contentType);
        fileDTO.setPath(FileUtilities.getRelativeFilePath(businessContext.getFileStore(), file));
        fileDTO.setComment(comment);
        fileDTO.setExpirationDate(calculateNewExpirationDate(fileDTO, user));
        fileDTO.setSize(byteCount);
        fileDTO.setCompleteSize(byteCount);
        fileDTO.setCrc32Value(crc32Value);
        daoFactory.getFileDAO().createFile(fileDTO);
        return fileDTO;
    }

    @Override
    public PreCreatedFileDTO createFile(final UserDTO user,
            final FilePreregistrationDTO fileInfoDTO, final String comment)
    {
        final String fileName = FilenameUtils.getName(fileInfoDTO.getFilePathOnClient());
        return createFile(user, fileName, fileInfoDTO.getFileSize(), comment);
    }

    private PreCreatedFileDTO createFile(final UserDTO user, final String fileName,
            final long fileSize, final String comment)
    {
        final String contentType = FilenameUtilities.getMimeType(fileName);
        final File fileInStore = createFile(user, fileName);
        final FileDTO fileInDB =
                preRegisterFileLink(user, fileName, fileSize, comment, contentType, fileInStore);
        return new PreCreatedFileDTO(fileInStore, fileInDB);
    }

    @Override
    public File createFile(final UserDTO user, final String fileName)
    {
        final File folder = createFolderFor(user);
        final File fileInStore =
                FileUtilities.createNextNumberedFile(new File(folder, fileName), null);
        return fileInStore;
    }

    private File createFolderFor(final UserDTO user)
    {
        final File folder = new File(businessContext.getFileStore(), user.getUserCode());
        if (folder.exists())
        {
            if (folder.isDirectory() == false)
            {
                throw new EnvironmentFailureException("Folder '" + folder.getAbsolutePath()
                        + "' exists but is not a directory.");
            }
        } else
        {
            final boolean successful = folder.mkdirs();
            if (successful == false)
            {
                if (businessContext.getFileStore().canWrite() == false)
                {
                    notificationLog.error("CIFEX file store is not writable any more ("
                            + businessContext.getFileStore().getAbsolutePath() + ")");
                    throw new EnvironmentFailureException("Folder '" + folder.getAbsolutePath()
                            + "' can not be created as CIFEX file store is not writable.");
                } else
                {
                    throw new EnvironmentFailureException("Folder '" + folder.getAbsolutePath()
                            + "' can not be created for some unknown reason.");
                }
            }
        }
        return folder;
    }

    private FileDTO preRegisterFileLink(UserDTO user, String fileName, long fileSize,
            String comment, String contentType, File file)
    {
        final FileDTO fileDTO = new FileDTO(user);
        fileDTO.setName(FilenameUtilities.ensureMaximumSize(fileName, MAX_FILENAME_LENGTH));
        fileDTO.setContentType(contentType);
        fileDTO.setPath(FileUtilities.getRelativeFilePath(businessContext.getFileStore(), file));
        fileDTO.setComment(comment);
        fileDTO.setExpirationDate(calculateNewExpirationDate(fileDTO, user));
        fileDTO.setSize(0L);
        fileDTO.setCompleteSize(fileSize);
        daoFactory.getFileDAO().createFile(fileDTO);
        return fileDTO;
    }

    @Override
    public void throwExceptionOnFileDoesNotExist(final String fileName)
    {
        final String msg =
                String.format("File '%s' does not seem to exist. It has not been saved.", fileName);
        operationLog.warn(msg);
        throw new UserFailureException(msg);
    }

    @Override
    public void updateUploadProgress(FileDTO fileDTO, UserDTO requestUser)
    {
        daoFactory.getFileDAO().updateFileUploadProgress(fileDTO.getID(), fileDTO.getSize(),
                fileDTO.getCrc32Value(), calculateNewExpirationDate(fileDTO, requestUser));
    }

    @Override
    public FileDTO tryGetUploadResumeCandidate(long userId, String fileName, long completeSize)
    {
        return daoFactory.getFileDAO().tryGetResumeCandidate(userId, fileName, completeSize);
    }

    @Override
    @Transactional
    public final List<String> shareFilesWith(final String url, final UserDTO requestUser,
            final Collection<String> userIdentifiers, final Collection<FileDTO> files,
            final String comment, final IUserActionLog logOrNull) throws UserFailureException
    {
        if (userIdentifiers.isEmpty())
        {
            return Collections.emptyList();
        }
        final Set<UserDTO> users = new LinkedHashSet<UserDTO>();
        final List<String> invalidIdentifiers = new ArrayList<String>();
        boolean success = false;
        try
        {
            final List<String> userCodes = new ArrayList<String>();
            final List<String> emailAddresses = new ArrayList<String>();
            extractUserCodesAndEmailAddresses(userIdentifiers, userCodes, emailAddresses,
                    invalidIdentifiers);
            // This call creates unknown users which the external authentication service knows
            // about.
            final Collection<UserDTO> relevantUsers =
                    userManager.getUsers(userCodes, emailAddresses, logOrNull);
            final TableMapNonUniqueKey<String, UserDTO> existingUsers =
                    UserUtils.createTableMapOfExistingUsersWithEmailAsKey(relevantUsers);
            final TableMap<String, UserDTO> existingUniqueUsers =
                    UserUtils.createTableMapOfExistingUsersWithUserCodeAsKey(relevantUsers);
            for (final String userCode : userCodes)
            {
                handleUserCode(userCode, requestUser, existingUsers, existingUniqueUsers,
                        invalidIdentifiers, users);
            }
            for (final String emailAddress : emailAddresses)
            {
                handleEmailAddress(emailAddress, requestUser, existingUsers, existingUniqueUsers,
                        invalidIdentifiers, users);
            }
            final RuntimeException exception =
                    createLinksAndCallTriggersAndSendEmails(users, files, url, comment, requestUser);
            success = (exception == null);
            if (exception != null)
            {
                throw exception;
            }
            return invalidIdentifiers;
        } finally
        {
            if (logOrNull != null)
            {
                logOrNull.logShareFiles(files, users, userIdentifiers, invalidIdentifiers, success);
            }
        }
    }

    private void extractUserCodesAndEmailAddresses(final Collection<String> identifiers,
            final List<String> userCodes, final List<String> validEmailAddresses,
            final List<String> invalidIdentifiers)
    {
        for (final String identifier : identifiers)
        {
            final String trimmedIdentifier = identifier.trim();
            if (UserUtils.isUserCodeWithIdPrefix(trimmedIdentifier))
            {
                userCodes.add(UserUtils.extractUserCode(trimmedIdentifier));
            } else if (UserUtils.isEmail(trimmedIdentifier))
            {
                validEmailAddresses.add(trimmedIdentifier.toLowerCase());
            } else
            {
                invalidIdentifiers.add(trimmedIdentifier);
            }
        }
    }

    private void handleUserCode(final String userCode, final UserDTO requestUser,
            final TableMapNonUniqueKey<String, UserDTO> existingUsers,
            final TableMap<String, UserDTO> existingUniqueUsers,
            final List<String> invalidIdentifiers, final Set<UserDTO> users)
    {
        final UserDTO userOrNull = existingUniqueUsers.tryGet(userCode);
        if (userOrNull != null && StringUtils.isNotBlank(userOrNull.getEmail()))
        {
            users.add(userOrNull);
        } else
        {
            invalidIdentifiers.add(userCode);
        }
    }

    private void handleEmailAddress(final String emailAddress, final UserDTO requestUser,
            final TableMapNonUniqueKey<String, UserDTO> existingUsers,
            final TableMap<String, UserDTO> existingUniqueUsers,
            final List<String> invalidIdentifiers, final Set<UserDTO> users)
    {
        final String lowerCaseIdentifier = emailAddress.toLowerCase();
        final Set<UserDTO> existingUsersOrNull =
                removeUnsuitableUsersForSharing(requestUser,
                        existingUsers.tryGet(lowerCaseIdentifier));
        if (existingUsersOrNull == null)
        {
            final String password =
                    businessContext.getPasswordGenerator().generatePassword(
                            UserManager.PASSWORD_LENGTH);
            final UserDTO user = tryCreateUser(requestUser, lowerCaseIdentifier, password);
            if (user != null)
            {
                existingUsers.add(user);
                users.add(user);
            } else
            {
                // Email address is invalid because user does not exist and requestUser
                // is not allowed to create new users.
                invalidIdentifiers.add(lowerCaseIdentifier);
            }
        } else
        {
            users.addAll(existingUsersOrNull);
        }
    }

    /**
     * Remove all users from <var>usersByEmail</var> that are <li>not permanent users and are <li>
     * not owner by the <var>requestUser</var>. If no users are left after this procedure, return
     * <code>null</code>, otherwise return <var>usersByEmail</var>.
     * <p>
     * This way, a new user will be created if all users with a given email are temporary users
     * created by some other permanent user. The rationale is to avoid leakage of file shares with
     * other regular users that by chance exchange files with the same user.
     * <p>
     * This class encodes the same logic as the one in
     * {@link ch.systemsx.cisd.cifex.client.application.UserUtils#removeUnsuitableUsersForSharing(UserInfoDTO, List)}
     * , but for {@link UserDTO} instead of
     * {@link ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO}.
     */
    private static Set<UserDTO> removeUnsuitableUsersForSharing(UserDTO requestUser,
            Set<UserDTO> usersByEmailOrNull)
    {
        if (usersByEmailOrNull == null)
        {
            return null;
        }
        // For a permanent user, the accepted owner of users to share the file with is the request
        // user itself, for a temporary user it is the registrator of the request user.
        // Note that acceptedOwner can be a minimal UserDTO which has only the id set. That's fine
        // as UserDTO.equals() is based solely on the id.
        final UserDTO acceptedOwner =
                (requestUser.isPermanent() ? requestUser : requestUser.getRegistrator());
        final Iterator<UserDTO> it = usersByEmailOrNull.iterator();
        while (it.hasNext())
        {
            final UserDTO user = it.next();
            if (user.getExpirationDate() != null
                    && acceptedOwner.equals(user.getRegistrator()) == false)
            {
                it.remove();
            }
        }
        return usersByEmailOrNull.isEmpty() ? null : usersByEmailOrNull;
    }

    private Date getMaxExpirationTime(Collection<FileDTO> files)
    {
        Date maxExpirationTime = new Date(timeProvider.getTimeInMilliseconds());
        for (FileDTO file : files)
        {
            if (file.getExpirationDate() == null)
            {
                continue;
            }
            if (maxExpirationTime.compareTo(file.getExpirationDate()) < 0)
            {
                maxExpirationTime = file.getExpirationDate();
            }
        }
        return maxExpirationTime;
    }

    private RuntimeException createLinksAndCallTriggersAndSendEmails(Set<UserDTO> users,
            final Collection<FileDTO> files, final String url, final String comment,
            final UserDTO requestUser)
    {
        final Date maxFileExirationTime = getMaxExpirationTime(files);
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final IMailClient mailClient = businessContext.getMailClient();
        // Implementation note: we do the sharing link creation and the email sending in
        // two loops in order to ensure that all database links are created before any
        // email is sent (note that this method is @Transactional).
        final Map<String, UserDTO> emailToUserMap = new HashMap<String, UserDTO>();
        for (final UserDTO user : users)
        {
            if (triggerManager.isTriggerUser(user) == false)
            {
                emailToUserMap.put(user.getEmail(), user);
            }
            // Check whether we need to update the expiration time of the userso that he has
            // enough time to download the file.
            final Date newUserExpirationDateOrNull =
                    ExpirationUtilities.tryExtendExpiration(
                            new Date(timeProvider.getTimeInMilliseconds()),
                            user.getExpirationDate(), user.getRegistrationDate(),
                            maxFileExirationTime, businessContext.getMaxUserRetention());
            if (newUserExpirationDateOrNull != null)
            {
                user.setExpirationDate(newUserExpirationDateOrNull);
                userManager.updateUser(user, user, null, requestUser, null);
            }
            for (final FileDTO file : files)
            {
                try
                {
                    fileDAO.createSharingLink(file.getID(), user.getID());
                } catch (final DataIntegrityViolationException ex)
                {
                    operationLog.warn(String.format(
                            "Trying to share file %s with user %s for the second time, skipped.",
                            file.getPath(), user.getUserCode()), ex);
                }
            }
        }

        final Set<FileDTO> filesLeft = new HashSet<FileDTO>(files);
        RuntimeException firstExceptionOrNull = null;
        for (final FileDTO fileDTO : files)
        {
            boolean dismiss = false;
            for (final UserDTO userDTO : users)
            {
                if (triggerManager.isTriggerUser(userDTO) == false)
                {
                    continue;
                }
                try
                {
                    dismiss |= triggerManager.handle(userDTO, fileDTO, this);
                } catch (final RuntimeException ex)
                {
                    final String msg =
                            "Error calling trigger for file '" + fileDTO.getPath()
                                    + "' with trigger user '" + userDTO.getUserCode() + "'.";
                    operationLog.error(msg, ex);
                    if (firstExceptionOrNull == null)
                    {
                        firstExceptionOrNull =
                                new RuntimeException(msg + " [" + ex.getClass().getSimpleName()
                                        + ":" + ex.getMessage() + "]", ex);
                    }
                }
            }
            if (dismiss)
            {
                filesLeft.remove(fileDTO);
            }
        }
        if (filesLeft.size() == 0)
        {
            return null;
        }

        boolean notified = false;
        for (final Entry<String, UserDTO> userEntry : emailToUserMap.entrySet())
        {
            final String email = userEntry.getKey();
            final UserDTO user = userEntry.getValue();
            final EMailBuilderForUploadedFiles builder =
                    new EMailBuilderForUploadedFiles(mailClient, requestUser, email);
            builder.setURL(url);
            final String passwordOrNull =
                    (user.getPassword() == null) ? null : user.getPassword().tryGetPlain();
            builder.setPassword(passwordOrNull);
            builder.setUserCode(user.getUserCode());
            builder.setFullName(user.getUserFullName());
            for (final FileDTO fileDTO : filesLeft)
            {
                builder.addFile(fileDTO);
            }
            if (StringUtils.isNotBlank(comment))
            {
                builder.setComment(comment);
            }
            try
            {
                builder.sendEMail();
                notified = false;
            } catch (final EnvironmentFailureException ex)
            {
                if (notified == false && ex.getMessage().indexOf("email addresses are invalid") < 0)
                {
                    // As we are sure that we have correct email addresses, this
                    // exception can only be related to the configuration and/or
                    // environment. So inform the administrator about the problem.
                    notificationLog.error("A problem has occurred while sending email.", ex);
                    notified = true;
                }
                // Inform request user about failure
                mailClient.sendEmailMessage("CIFEX was unable to send an email to your recipient",
                        ex.getMessage(), null, null, new EMailAddress(requestUser.getEmail()));
            }
        }
        return firstExceptionOrNull;
    }

    private UserDTO tryCreateUser(final UserDTO requestUser, final String email,
            final String password)
    {
        // Only permanent users are allowed to create new user accounts.
        if (requestUser.isPermanent())
        {
            final UserDTO user = new UserDTO();
            // Ensure we use a unique user code, based on the email address.
            user.setUserCode(StringUtilities.createUniqueString(email, new IUniquenessChecker()
                {
                    @Override
                    public boolean isUnique(String code)
                    {
                        return daoFactory.getUserDAO().hasUserCode(code) == false;
                    }
                }));
            user.setEmail(email);
            user.setPassword(new Password(password));
            user.setRegistrator(requestUser);
            final IUserBO userBO = boFactory.createUserBO();
            userBO.defineForCreate(user, requestUser, true);
            userBO.save();
            return user;
        } else
        {
            return null;
        }
    }

    @Override
    @Transactional
    public final List<FileDTO> listFiles() throws UserFailureException
    {
        final List<FileDTO> list = daoFactory.getFileDAO().listFiles();
        final Map<Long, String> idToCodeMap = new HashMap<Long, String>();
        for (FileDTO file : list)
        {
            for (UserDTO user : file.getSharingUsers())
            {
                user.setUserCode(getUserCodeForId(idToCodeMap, user.getID()));
            }
        }
        return list;
    }

    private String getUserCodeForId(Map<Long, String> idToCodeMap, long id)
    {
        String code = idToCodeMap.get(id);
        if (code == null)
        {
            code = daoFactory.getUserDAO().tryFindUserCodeById(id);
            idToCodeMap.put(id, code);
        }
        return code;
    }

    @Override
    @Transactional
    public void deleteFile(final FileDTO fileDTO)
    {
        assert fileDTO != null : "Given file can not be null";

        daoFactory.getFileDAO().deleteFile(fileDTO.getID());
        deleteFromFileSystem(fileDTO.getPath());
    }

    @Override
    public Date updateFileUserData(final long fileId, final String name,
            final String commentOrNull, final Date expirationDate, final UserDTO requestUser)
    {
        final Date newExpirationDate = fixFileExpiration(fileId, expirationDate, requestUser);
        daoFactory.getFileDAO().updateFileUserEdit(fileId, name, commentOrNull, newExpirationDate);
        return newExpirationDate;
    }

    @Override
    public FileDTO getFile(final long fileId) throws IllegalArgumentException
    {
        final FileDTO fileOrNull = daoFactory.getFileDAO().tryGetFile(fileId);
        if (fileOrNull == null)
        {
            final String msg = "No file found for fileId " + fileId + ".";
            operationLog.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return fileOrNull;
    }

    private Date calculateNewExpirationDate(final FileDTO file, final UserDTO requestUser)
    {
        final Integer maxUserRetentionDaysOrNull = tryGetMaxFileRetentionDays(requestUser);
        final int fileRetentionDays = businessContext.getFileRetention();
        final Date now = new Date(timeProvider.getTimeInMilliseconds());
        final Date newExpirationDate = DateUtils.addDays(now, fileRetentionDays);
        if (maxUserRetentionDaysOrNull != null)
        {
            final Date registrationDate =
                    (file.getRegistrationDate() == null) ? now : file.getRegistrationDate();
            final Date maxExpirationDate =
                    DateUtils.addDays(registrationDate, maxUserRetentionDaysOrNull);
            if (newExpirationDate.getTime() > maxExpirationDate.getTime())
            {
                return extendUntilEndOfDay(maxExpirationDate);
            }
        }
        return extendUntilEndOfDay(newExpirationDate);
    }

    @Override
    public void updateFile(final FileDTO file, UserDTO requestUser)
    {
        checkAndFixFileExpiration(file, requestUser);
        daoFactory.getFileDAO().updateFile(file);
    }

    /** Checks that the new expiration date is in the valid range, otherwise set it to the limit. */
    private void checkAndFixFileExpiration(final FileDTO file, UserDTO requestUser)
    {
        file.setExpirationDate(fixFileExpiration(file.getID(), file.getExpirationDate(),
                requestUser));
    }

    /** Checks that the new expiration date is in the valid range, otherwise set it to the limit. */
    private Date fixFileExpiration(final long fileId, final Date proposedExpirationDate,
            final UserDTO requestUser)
    {
        final Integer maxRetentionDaysOrNull = tryGetMaxFileRetentionDays(requestUser);
        // Note: If no limit for the retention time applies then the registration date is not
        // required to compute the expiration date.
        final Date registrationDateOrNull =
                (maxRetentionDaysOrNull == null) ? null : daoFactory.getFileDAO()
                        .getFileRegistrationDate(fileId);
        return fixExpiration(new Date(timeProvider.getTimeInMilliseconds()),
                proposedExpirationDate, registrationDateOrNull, maxRetentionDaysOrNull,
                businessContext.getFileRetention());
    }

    private Integer tryGetMaxFileRetentionDays(final UserDTO requestUserOrNull)
    {
        if (requestUserOrNull == null)
        {
            return businessContext.getMaxFileRetention();
        } else
        {
            if (requestUserOrNull.isAdmin())
            {
                return null;
            } else
            {
                return (requestUserOrNull.getMaxFileRetention() == null) ? businessContext
                        .getMaxFileRetention() : requestUserOrNull.getMaxFileRetention();
            }
        }
    }

    @Override
    public void deleteSharingLink(final long fileId, final String userCode)
    {
        daoFactory.getFileDAO().deleteSharingLink(fileId, userCode);
    }

}
