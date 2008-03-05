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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.BasicFileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMapNonUniqueKey;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * The only <code>IFileManager</code> implementation.
 * 
 * @author Christian Ribeaud
 */
final class FileManager extends AbstractManager implements IFileManager
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileManager.class);

    private static final Logger trackingLog = LogFactory.getLogger(LogCategory.TRACKING, FileManager.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, FileManager.class);

    FileManager(IDAOFactory daoFactory, IBusinessObjectFactory boFactory, IBusinessContext businessContext)
    {
        super(daoFactory, boFactory, businessContext);
    }

    /**
     * Whether given <var>userDTO</var> could be found in list of sharing users.
     */
    private final static boolean containsUser(final UserDTO userDTO, final List<UserDTO> sharingUsers)
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

    /** Deletes file with given path from the file system. */
    private final void deleteFromFileSystem(final String path)
    {
        final File file = new File(businessContext.getFileStore(), path);
        deleteFromFileSystem(file);
    }

    /** Deletes file with given path from the file system. */
    private final void deleteFromFileSystem(final File file)
    {
        if (file.exists())
        {
            if (file.delete())
            {
                if (trackingLog.isInfoEnabled())
                {
                    trackingLog.info("File [" + file.getAbsolutePath() + "] deleted.");
                }
            } else
            {
                notificationLog.error("File [" + file.getAbsolutePath() + "] can not be deleted.");
            }
        } else
        {
            operationLog.warn("File [" + file.getAbsolutePath() + "] not deleted: doesn't exist.");
        }
    }

    //
    // IFileManager
    //

    @Transactional
    public final List<FileDTO> listDownloadFiles(final long userId) throws UserFailureException
    {
        return daoFactory.getFileDAO().listDownloadFiles(userId);
    }

    @Transactional
    public final List<FileDTO> listUploadedFiles(final long userId) throws UserFailureException
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
                        operationLog.info("Expired file '" + file.getPath() + "' removed from database.");
                    }
                } else
                {
                    operationLog.warn("Expired file '" + file.getPath() + "' could not be found in the database.");
                }
                deleteFromFileSystem(file.getPath());
            } catch (RuntimeException ex)
            {
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
        final FileDTO fileDTOOrNull = daoFactory.getFileDAO().tryGetFile(fileId);
        if (fileDTOOrNull == null)
        {
            return new FileInformation(fileId, "File [id=" + fileId + "] not found in the database.");
        } else
        {
            final File realFile = new java.io.File(businessContext.getFileStore(), fileDTOOrNull.getPath());
            if (realFile.exists() == false)
            {
                return new FileInformation(fileId, String.format("File '%s' [id=%d] not found in the file store.",
                        realFile.getPath(), fileId));
            }
        }
        return new FileInformation(fileId, fileDTOOrNull);
    }

    @Transactional
    public final FileContent getFileContent(final FileDTO fileDTO)
    {
        final File realFile = new File(businessContext.getFileStore(), fileDTO.getPath());
        if (realFile.exists() == false)
        {
            throw new IllegalStateException(String.format("File '%s' does not exist on the file system.", realFile
                    .getAbsolutePath()));
        }
        try
        {
            return new FileContent(BeanUtils.createBean(BasicFileDTO.class, fileDTO), new FileInputStream(realFile));
        } catch (final FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
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
    public final FileDTO saveFile(final UserDTO user, final String fileName, final String contentType,
            final InputStream input)
    {
        assert user != null : "Unspecified user.";
        assert user.getEmail() != null : "Unspecified email of user " + user;
        assert StringUtils.isNotBlank(fileName) : "Unspecified file name.";
        assert StringUtils.isNotBlank(contentType) : "Unspecified content type.";
        assert input != null : "Unspecified input stream.";

        final File folder = createFolderFor(user);
        final File file = FileUtilities.createNextNumberedFile(new File(folder, fileName), null);
        try
        {
            OutputStream outputStream = null;
            InputStream inputStream = null;
            try
            {
                outputStream = new FileOutputStream(file);
                final CountingInputStream countingInputStream = new CountingInputStream(input);
                inputStream = countingInputStream;
                // Uncomment the following line if you want a more perceptible effect in the file upload feedback.
                // inputStream = new SlowInputStream(countingInputStream, 100 * FileUtils.ONE_KB);
                IOUtils.copy(inputStream, outputStream);
                final long byteCount = countingInputStream.getByteCount();
                if (byteCount > 0)
                {
                    final FileDTO fileDTO = new FileDTO(user.getID());
                    fileDTO.setName(fileName);
                    fileDTO.setContentType(contentType);
                    fileDTO.setPath(FileUtilities.getRelativeFile(businessContext.getFileStore(), file));
                    fileDTO.setExpirationDate(DateUtils.addMinutes(new Date(), businessContext.getFileRetention()));
                    fileDTO.setSize(byteCount);
                    daoFactory.getFileDAO().createFile(fileDTO);
                    return fileDTO;
                } else
                {
                    deleteFromFileSystem(file);
                    throwExceptionOnFileDoesNotExist(fileName);
                    return null; // never reached
                }
            } catch (IOException ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex, "Error saving file '%s' (Is it a file?).", fileName);
            } finally
            {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        } catch (RuntimeException e)
        {
            deleteFromFileSystem(file);
            throw e;
        }
    }

    public void throwExceptionOnFileDoesNotExist(final String fileName)
    {
        final String msg = String.format("File '%s' does not seem to exist. It has not been saved.", fileName);
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

    @Transactional
    public List<String> shareFilesWith(String url, UserDTO requestUser, Collection<String> emailsOfUsers,
            Collection<FileDTO> files, String comment)
    {
        final TableMapNonUniqueKey<String, UserDTO> existingUsers = createTableMapOfExistingUsers();
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final List<String> invalidEmailAdresses = new ArrayList<String>();
        final PasswordGenerator passwordGenerator = businessContext.getPasswordGenerator();
        final IMailClient mailClient = businessContext.getMailClient();
        boolean notified = false;
        for (final String email : emailsOfUsers)
        {
            Set<UserDTO> users = existingUsers.tryGet(email);
            String password = null;
            if (users == null) // Try to create user.
            {
                password = passwordGenerator.generatePassword(10);
                final UserDTO user = tryCreateUser(requestUser, email, password);
                if (user != null)
                {
                    existingUsers.add(user);
                    users = Collections.singleton(user);
                } else
                {
                    // Email address is invalid because user does not exist and requestUser is not allowed to create new
                    // users.
                    invalidEmailAdresses.add(email);
                }
            }
            if (users != null)
            {
                // Implementation note: we do the sharing link creation and the email sending in two loops in order to
                // ensure that all database links are created before any email is sent (note that this method is
                // @Transactional).
                for (final UserDTO user : users)
                {
                    for (FileDTO file : files)
                    {
                        fileDAO.createSharingLink(file.getID(), user.getID());
                    }
                }
                for (final UserDTO user : users)
                {
                    final EMailBuilderForUploadedFiles builder =
                            new EMailBuilderForUploadedFiles(mailClient, requestUser, email);
                    builder.setURL(url);
                    builder.setPassword(password);
                    builder.setUserCode(user.getUserCode());
                    for (final FileDTO fileDTO : files)
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
                            // As we are sure that we get correct email addresses, this exception can only be related to
                            // the configuration and/or environment. So inform the administrator about the problem.
                            notificationLog.error("A problem has occurred while sending email.", ex);
                            notified = true;
                        }
                    }
                }
            }
        }
        return invalidEmailAdresses;
    }

    private TableMapNonUniqueKey<String, UserDTO> createTableMapOfExistingUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        return new TableMapNonUniqueKey<String, UserDTO>(userDAO.listUsers(), new IKeyExtractor<String, UserDTO>()
            {
                public String getKey(UserDTO user)
                {
                    return user.getEmail();
                }
            });
    }

    private UserDTO tryCreateUser(UserDTO requestUser, String email, String password)
    {
        if (requestUser.isPermanent()) // Only permanent users are allowed to create new user accounts.
        {
            final UserDTO user = new UserDTO();
            user.setUserCode(email);
            user.setEmail(email);
            user.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
            user.setRegistrator(requestUser);
            IUserBO userBO = boFactory.createUserBO();
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
        return daoFactory.getFileDAO().listFiles();
    }

    @Transactional
    public void deleteFile(final FileDTO fileDTO)
    {
        assert fileDTO != null : "Given file can not be null";

        daoFactory.getFileDAO().deleteFile(fileDTO.getID());
        deleteFromFileSystem(fileDTO.getPath());
    }

    @Transactional
    public void updateFileExpiration(final long fileId, final Date newExpirationDate)
    {
        final FileDTO file = daoFactory.getFileDAO().tryGetFile(fileId);
        if (newExpirationDate == null)
        {
            file.setExpirationDate(DateUtils.addMinutes(new Date(), businessContext.getFileRetention()));
        } else
        {
            file.setExpirationDate(newExpirationDate);
        }
        daoFactory.getFileDAO().updateFile(file);
    }

}
