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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.BasicFileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.cifex.server.util.ChecksummingInputStream;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMapNonUniqueKey;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * The only <code>IFileManager</code> implementation.
 * 
 * @author Christian Ribeaud
 */
final class FileManager extends AbstractManager implements IFileManager
{
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final Pattern USER_CODE_WITH_ID_PREFIX_PATTERN =
            Pattern.compile(Constants.USER_CODE_WITH_ID_PREFIX_REGEX);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(Constants.EMAIL_REGEX);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileManager.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, FileManager.class);

    public final static String USER_ID_PREFIX = Constants.USER_ID_PREFIX;

    private final ITimeProvider timeProvider;

    private final ITriggerManager triggerManager;

    FileManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IBusinessContext businessContext, ITriggerManager triggerManager)
    {
        this(daoFactory, boFactory, businessContext, triggerManager,
                SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    FileManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IBusinessContext businessContext, ITriggerManager triggerManager,
            ITimeProvider timeProvider)
    {
        super(daoFactory, boFactory, businessContext);
        this.timeProvider = timeProvider;
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

    @Transactional
    public final List<FileDTO> listDownloadFiles(final long userId)
    {
        return daoFactory.getFileDAO().listDownloadFiles(userId);
    }

    @Transactional
    public final List<FileDTO> listUploadedFiles(final long userId)
    {
        return daoFactory.getFileDAO().listUploadedFiles(userId);
    }

    @Transactional
    public final void deleteExpiredFiles()
    {
        final List<FileDTO> expiredFiles = daoFactory.getFileDAO().getExpiredFiles();
        if (operationLog.isInfoEnabled() && expiredFiles.size() > 0)
        {
            operationLog.info("Found " + expiredFiles.size() + " expired files.");
        }
        RuntimeException firstExecptionOrNull = null;
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
                businessContext.getUserActionLog().logExpireFile(file, success);
            } catch (final RuntimeException ex)
            {
                businessContext.getUserActionLog().logExpireFile(file, false);
                operationLog.error("Error deleting file '" + file.getPath() + "'.", ex);
                if (firstExecptionOrNull == null)
                {
                    firstExecptionOrNull = ex;
                }
            }
        }
        // Rethrow exception, if any
        if (firstExecptionOrNull != null)
        {
            throw firstExecptionOrNull;
        }
    }

    @Transactional
    public final FileInformation getFileInformation(final long fileId)
    {
        return getFileInformation(fileId, true);
    }

    @Transactional
    public final FileInformation getFileInformationFilestoreUnimportant(final long fileId)
    {
        return getFileInformation(fileId, false);
    }

    @Transactional
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
                return new FileInformation(fileId, String.format(
                        "Unexpected: File '%s' [id=%d] is missing in CIFEX file store.", realFile
                                .getPath(), fileId));
            }
        }
        return new FileInformation(fileId, fileDTOOrNull, realFile);
    }

    @Transactional
    public final FileContent getFileContent(final FileDTO fileDTO)
    {
        boolean success = false;
        try
        {
            final File realFile = getRealFile(fileDTO);
            try
            {
                final FileContent content =
                        new FileContent(BeanUtils.createBean(BasicFileDTO.class, fileDTO),
                                new FileInputStream(realFile));
                success = true;
                return content;
            } catch (final FileNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        } finally
        {
            businessContext.getUserActionLog().logDownloadFile(fileDTO, success);
        }
    }

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

    @Transactional
    public final boolean isAllowedAccess(final UserDTO userDTO, final FileDTO fileDTO)
    {
        if (isAllowedDeletion(userDTO, fileDTO))
        {
            return true;
        }
        final List<UserDTO> sharingUsers = fileDTO.getSharingUsers();
        return containsUser(userDTO, sharingUsers);
    }

    @Transactional
    public boolean isAllowedDeletion(final UserDTO userDTO, final FileDTO fileDTO)
    {
        if (userDTO.isAdmin())
        {
            return true;
        }
        if (userDTO.getID().equals(fileDTO.getRegistratorId()))
        {
            return true;
        }
        return false;
    }

    @Transactional
    public final FileDTO saveFile(final UserDTO user, final String fileName, final String comment,
            final String contentTypeOrNull, final InputStream input)
    {
        assert user != null : "Unspecified user.";
        assert user.getEmail() != null : "Unspecified email of user " + user;
        assert StringUtils.isNotBlank(fileName) : "Unspecified file name.";
        assert input != null : "Unspecified input stream.";

        final String contentType =
                (contentTypeOrNull != null) ? contentTypeOrNull : DEFAULT_CONTENT_TYPE;
        final File file = createFile(user, fileName);
        boolean success = false;
        try
        {
            OutputStream outputStream = null;
            InputStream inputStream = null;
            try
            {
                outputStream = new FileOutputStream(file);
                final ChecksummingInputStream countingAndChecksummingInputStream =
                        new ChecksummingInputStream(input);
                inputStream = countingAndChecksummingInputStream;
                // Uncomment the following line if you want a more perceptible effect in the file
                // upload feedback. inputStream = new SlowInputStream(countingInputStream, 100 *
                // FileUtils.ONE_KB);
                IOUtils.copy(inputStream, outputStream);
                outputStream.close();
                final long byteCount = countingAndChecksummingInputStream.getByteCount();
                final int crc32Value = countingAndChecksummingInputStream.getCRC32Value();
                operationLog.info(String.format("File %s has crc32 checksum %x.", fileName,
                        crc32Value));
                if (byteCount > 0)
                {
                    final FileDTO fileDTO =
                            registerFile(user, fileName, comment, contentType, file, byteCount,
                                    crc32Value);
                    success = true;
                    return fileDTO;
                } else
                {
                    deleteFromFileSystem(file);
                    throwExceptionOnFileDoesNotExist(fileName);
                    return null; // never reached
                }
            } catch (final IOException ex)
            {
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
        } finally
        {
            businessContext.getUserActionLog().logUploadFile(fileName, success);
        }
    }

    @Transactional
    public List<String> registerFileLinkAndInformRecipients(UserDTO user, String fileName,
            String comment, String contentTypeOrNull, File file, int crc32Value,
            String[] recipients, String url)
    {
        final String contentType =
                (contentTypeOrNull != null) ? contentTypeOrNull : DEFAULT_CONTENT_TYPE;
        final FileDTO fileDTO =
                registerFile(user, fileName, comment, contentType, file, file.length(), crc32Value);
        return shareFilesWith(url, user, Arrays.asList(recipients), Collections.singleton(fileDTO),
                comment);
    }

    private FileDTO registerFile(final UserDTO user, final String fileName, final String comment,
            final String contentType, final File file, final long byteCount, final int crc32Value)
    {
        operationLog.info(String.format("File %s has crc32 checksum %x.", fileName, crc32Value));
        final FileDTO fileDTO = new FileDTO(user.getID());
        fileDTO.setName(fileName);
        fileDTO.setContentType(contentType);
        fileDTO.setPath(FileUtilities.getRelativeFile(businessContext.getFileStore(), file));
        fileDTO.setComment(comment);
        fileDTO.setExpirationDate(caluclateExpirationDate(user));
        fileDTO.setSize(byteCount);
        fileDTO.setCrc32Value(crc32Value);
        daoFactory.getFileDAO().createFile(fileDTO);
        return fileDTO;
    }

    public File createFile(final UserDTO user, final String fileName)
    {
        final File folder = createFolderFor(user);
        return FileUtilities.createNextNumberedFile(new File(folder, fileName), null);
    }

    public void throwExceptionOnFileDoesNotExist(final String fileName)
    {
        final String msg =
                String.format("File '%s' does not seem to exist. It has not been saved.", fileName);
        operationLog.warn(msg);
        throw new UserFailureException(msg);
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
                throw new EnvironmentFailureException("Folder '" + folder.getAbsolutePath()
                        + "' can not be created for some unknown reason.");
            }
        }
        return folder;
    }

    public final List<String> shareFilesWith(final String url, final UserDTO requestUser,
            final Collection<String> userIdentifiers, final Collection<FileDTO> files,
            final String comment) throws UserFailureException
    {
        final Set<UserDTO> allUsers = new HashSet<UserDTO>();
        final List<String> invalidEmailAdresses = new ArrayList<String>();
        setRegisterer(requestUser, files);
        boolean success = false;
        RuntimeException firstExceptionOrNull = null;
        try
        {
            final TableMapNonUniqueKey<String, UserDTO> existingUsers =
                    createTableMapOfExistingUsersWithEmailAsKey();
            final TableMap<String, UserDTO> existingUniqueUsers =
                    createTableMapOfExistingUsersWithUserCodeAsKey();
            for (final String identifier : userIdentifiers)
            {
                final Set<UserDTO> users = new LinkedHashSet<UserDTO>();
                final String password =
                        handleIdentifer(identifier, requestUser, existingUsers,
                                existingUniqueUsers, invalidEmailAdresses, users);
                allUsers.addAll(users);
                final RuntimeException ex =
                        createLinksAndCallTriggersAndSendEmails(users, files, url, comment,
                                requestUser, password);
                if (firstExceptionOrNull == null && ex != null)
                {
                    firstExceptionOrNull = ex;
                }
            }
            success = (firstExceptionOrNull == null);
            if (firstExceptionOrNull != null)
            {
                throw firstExceptionOrNull;
            }
            return invalidEmailAdresses;
        } finally
        {
            businessContext.getUserActionLog().logShareFiles(files, allUsers, userIdentifiers,
                    invalidEmailAdresses, success);
        }
    }

    private void setRegisterer(final UserDTO requestUser, final Collection<FileDTO> files)
    {
        for (FileDTO file : files)
        {
            file.setRegisterer(requestUser);
        }
    }

    private String handleIdentifer(final String identifier, final UserDTO requestUser,
            final TableMapNonUniqueKey<String, UserDTO> existingUsers,
            final TableMap<String, UserDTO> existingUniqueUsers,
            final List<String> invalidEmailAdresses, Set<UserDTO> users)
    {
        String password = null;
        final String lowerCaseIdentifier = identifier.toLowerCase();
        // If the Identifier start with "id:", it is not a email
        if (USER_CODE_WITH_ID_PREFIX_PATTERN.matcher(lowerCaseIdentifier).matches())
        {
            final UserDTO userOrNull =
                    existingUniqueUsers.tryGet(lowerCaseIdentifier.substring(USER_ID_PREFIX
                            .length()));
            if (userOrNull != null && StringUtils.isNotBlank(userOrNull.getEmail()))
            {
                users.add(userOrNull);
            } else
            {
                invalidEmailAdresses.add(lowerCaseIdentifier);
            }
        } else if (EMAIL_PATTERN.matcher(lowerCaseIdentifier).matches())
        {
            Set<UserDTO> existingUsersOrNull = existingUsers.tryGet(lowerCaseIdentifier);
            if (existingUsersOrNull == null)
            {
                password = businessContext.getPasswordGenerator().generatePassword(10);
                final UserDTO user = tryCreateUser(requestUser, lowerCaseIdentifier, password);
                if (user != null)
                {
                    existingUsers.add(user);
                    users.add(user);
                } else
                {
                    // Email address is invalid because user does not exist and requestUser
                    // is not allowed to create new users.
                    invalidEmailAdresses.add(lowerCaseIdentifier);
                }
            } else
            {
                users.addAll(existingUsersOrNull);
            }
        } else
        {
            invalidEmailAdresses.add(lowerCaseIdentifier);
        }
        return password;
    }

    private RuntimeException createLinksAndCallTriggersAndSendEmails(Set<UserDTO> users,
            final Collection<FileDTO> files, final String url, final String comment,
            final UserDTO requestUser, String password)
    {
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final IMailClient mailClient = businessContext.getMailClient();
        // Implementation note: we do the sharing link creation and the email sending in
        // two loops in order to ensure that all database links are created before any
        // email is sent (note that this method is @Transactional).
        final List<String> alreadyExistingSharingLinks = new ArrayList<String>();
        for (final UserDTO user : users)
        {
            for (final FileDTO file : files)
            {
                try
                {
                    fileDAO.createSharingLink(file.getID(), user.getID());
                } catch (final DataIntegrityViolationException ex)
                {
                    alreadyExistingSharingLinks.add(user.getUserCode());
                    notificationLog.error(String.format(
                            "Sharing file %s with user %s for the second time.", file.getPath(),
                            user.getUserCode()), ex);
                }
            }
        }
        if (alreadyExistingSharingLinks.size() > 0)
        {
            throw new UserFailureException(("Cannot share file with the users twice ("
                    + alreadyExistingSharingLinks + "). Operation failed."));
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
        for (final UserDTO user : users)
        {
            if (triggerManager.isTriggerUser(user))
            {
                continue;
            }
            final String email = user.getEmail();
            final EMailBuilderForUploadedFiles builder =
                    new EMailBuilderForUploadedFiles(mailClient, requestUser, email);
            builder.setURL(url);
            builder.setPassword(password);
            builder.setUserCode(user.getUserCode());
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
                if (notified == false)
                {
                    // As we are sure that we get correct email addresses, this
                    // exception can only be related to the configuration and/or
                    // environment. So inform the administrator about the problem.
                    notificationLog.error("A problem has occurred while sending email.", ex);
                    notified = true;
                }
            }
        }
        return firstExceptionOrNull;
    }

    private TableMapNonUniqueKey<String, UserDTO> createTableMapOfExistingUsersWithEmailAsKey()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        return new TableMapNonUniqueKey<String, UserDTO>(userDAO.listUsers(),
                new IKeyExtractor<String, UserDTO>()
                    {
                        public String getKey(final UserDTO user)
                        {
                            return user.getEmail();
                        }
                    });
    }

    private TableMap<String, UserDTO> createTableMapOfExistingUsersWithUserCodeAsKey()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        return new TableMap<String, UserDTO>(userDAO.listUsers(),
                new IKeyExtractor<String, UserDTO>()
                    {
                        public String getKey(final UserDTO user)
                        {
                            return user.getUserCode();
                        }
                    });
    }

    private UserDTO tryCreateUser(final UserDTO requestUser, final String email,
            final String password)
    {
        // Only permanent users are allowed to create new user accounts.
        if (requestUser.isPermanent())
        {
            final UserDTO user = new UserDTO();
            user.setUserCode(email);
            user.setEmail(email);
            user.setPassword(new Password(password));
            user.setRegistrator(requestUser);
            final IUserBO userBO = boFactory.createUserBO();
            userBO.define(user);
            userBO.save();
            return user;
        } else
        {
            return null;
        }
    }

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

    @Transactional
    public void deleteFile(final FileDTO fileDTO)
    {
        assert fileDTO != null : "Given file can not be null";

        boolean success = false;
        try
        {
            daoFactory.getFileDAO().deleteFile(fileDTO.getID());
            deleteFromFileSystem(fileDTO.getPath());
            success = true;
        } finally
        {
            businessContext.getUserActionLog().logDeleteFile(fileDTO, success);
        }
    }

    @Transactional
    public void updateFileExpiration(final long fileId) throws IllegalArgumentException
    {
        final FileDTO file = getFile(fileId);
        boolean success = false;
        try
        {
            file.setExpirationDate(caluclateExpirationDate(file.getRegisterer()));
            daoFactory.getFileDAO().updateFile(file);
            success = true;
        } finally
        {
            businessContext.getUserActionLog().logRenewFile(file, success);
        }
    }

    private FileDTO getFile(final long fileId) throws IllegalArgumentException
    {
        final FileDTO file = daoFactory.getFileDAO().tryGetFile(fileId);
        if (file == null)
        {
            final String msg = "No file found for fileId " + fileId + ".";
            operationLog.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return file;
    }

    private Date caluclateExpirationDate(UserDTO user)
    {
        Integer usersFileRetention = user.getFileRetention();
        int fileRetention =
                usersFileRetention == null ? businessContext.getFileRetention()
                        : usersFileRetention.intValue();
        return DateUtils.addMinutes(new Date(timeProvider.getTimeInMilliseconds()), fileRetention);
    }

    public void updateFile(final FileDTO file)
    {
        daoFactory.getFileDAO().updateFile(file);
    }

    @Transactional
    public void deleteSharingLink(final long fileId, final String userCode)
    {

        boolean success = false;
        try
        {
            daoFactory.getFileDAO().deleteSharingLink(fileId, userCode);
            success = true;
        } finally
        {
            businessContext.getUserActionLog().logDeleteSharingLink(fileId, userCode, success);
        }

    }
}
