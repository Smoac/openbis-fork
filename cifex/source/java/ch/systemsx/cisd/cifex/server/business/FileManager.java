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
import java.util.Date;
import java.util.List;

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
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileOutput;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
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

    private final FileDTO getFile(final long fileId) throws UserFailureException
    {
        final FileDTO file = daoFactory.getFileDAO().tryGetFile(fileId);
        if (file == null)
        {
            throw UserFailureException.fromTemplate("No file could be found for id %d.", fileId);
        }
        return file;
    }

    /** Deletes file with given path from the file system. */
    private final void deleteFromFileSystem(final String path)
    {
        final File file = new File(businessContext.getFileStore(), path);
        if (file.exists())
        {
            file.delete();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("File [" + path + "] deleted.");
            }
        } else
        {
            operationLog.warn("File [" + path + "] not deleted: doesn't exist.");
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
        RuntimeException ex_all = null;
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
                if (ex_all == null)
                {
                    ex_all = ex;
                }
            }
        }
        // Rethrow exception, if any
        if (ex_all != null)
        {
            throw ex_all;
        }
    }

    @Transactional
    public final FileOutput getFile(final UserDTO userDTO, final long fileId)
    {
        assert userDTO != null : "Given user can not be null.";

        final FileDTO file = getFile(fileId);
        final java.io.File realFile = new java.io.File(businessContext.getFileStore(), file.getPath());
        if (realFile.exists() == false)
        {
            throw new UserFailureException(String.format("File '%s' no longer available.", realFile.getAbsolutePath()));
        }
        final List<UserDTO> sharingUsers = file.getSharingUsers();
        // Administrator can download the file not shared with him.
        if (userDTO.getID().equals(file.getRegistererId()) == false && containsUser(userDTO, sharingUsers) == false
                && userDTO.isAdmin() == false)
        {
            throw UserFailureException.fromTemplate("Current user '%s' does not have access to file '%s'.", userDTO
                    .getUserFullName(), file.getPath());
        }
        try
        {
            return new FileOutput(BeanUtils.createBean(BasicFileDTO.class, file), new FileInputStream(realFile));
        } catch (final FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
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
            CountingInputStream inputStream = null;
            try
            {
                outputStream = new FileOutputStream(file);
                inputStream = new CountingInputStream(input);
                IOUtils.copy(inputStream, outputStream);
                final long byteCount = inputStream.getByteCount();
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
                    file.delete();
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
            file.delete();
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
        final File folder = new File(businessContext.getFileStore(), user.getEmail());
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
            Collection<FileDTO> files)
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        // FIXME 2008-02-05, Bernd Rinn: emails are no longer guaranteed to be unique, so we need to change the data structure here!
        TableMap<String, UserDTO> existingUsers =
                new TableMap<String, UserDTO>(userDAO.listUsers(), new IKeyExtractor<String, UserDTO>()
                    {
                        public String getKey(UserDTO user)
                        {
                            return user.getEmail();
                        }
                    });
        IFileDAO fileDAO = daoFactory.getFileDAO();
        final List<String> invalidEmailAdresses = new ArrayList<String>();
        PasswordGenerator passwordGenerator = businessContext.getPasswordGenerator();
        for (String email : emailsOfUsers)
        {
            UserDTO user = existingUsers.tryToGet(email);
            String password = null;
            if (user == null)
            {
                password = passwordGenerator.generatePassword(10);
                user = tryCreateUser(requestUser, existingUsers, email, invalidEmailAdresses, password);
            }
            if (user != null)
            {
                for (FileDTO file : files)
                {
                    fileDAO.createSharingLink(file.getID(), user.getID());
                    continue;
                }
                sendEmail(url, files, email, password);
            }
        }
        return invalidEmailAdresses;
    }

    private UserDTO tryCreateUser(UserDTO requestUser, TableMap<String, UserDTO> existingUsers, String email,
            List<String> invalidEmailAdresses, String password)
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
            existingUsers.add(user);
            return user;
        } else
        {
            invalidEmailAdresses.add(email);
            return null;
        }
    }

    private void sendEmail(String url, Collection<FileDTO> files, String email, String password)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("The followings files are available for downloading:\n");
        for (final FileDTO fileDTO : files)
        {
            builder.append(fileDTO.getName()).append(" ");
            builder.append(url).append("/index.html?fileId=").append(fileDTO.getID());
            builder.append("&email=").append(email);
            builder.append('\n');
        }
        builder.append("\nClick on a link to start downloading. You have to login with your e-mail address (i.e. ");
        builder.append(email).append(")");
        if (password != null)
        {
            builder.append(" with the following password:\n\n").append(password);
        } else
        {
            builder.append(" with your password.");
        }
        IMailClient mailClient = businessContext.getMailClient();
        mailClient.sendMessage("Files for download available on the Cifex Server", builder.toString(), new String[]
            { email });
    }

    @Transactional
    public final List<FileDTO> listFiles() throws UserFailureException
    {
        return daoFactory.getFileDAO().listFiles();
    }

    @Transactional
    public void deleteFile(long fileId)
    {
        FileDTO file = daoFactory.getFileDAO().tryGetFile(fileId);
        daoFactory.getFileDAO().deleteFile(file.getID());
        deleteFromFileSystem(file.getPath());

    }

}
