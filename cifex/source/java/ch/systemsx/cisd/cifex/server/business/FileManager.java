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
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dto.BasicFileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * The only <code>IFileManager</code> implementation.
 * 
 * @author Christian Ribeaud
 */
final class FileManager extends AbstractManager implements IFileManager
{
    private final File fileStore;

    private final int fileRetentionInMinutes;

    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION, FileManager.class);

    FileManager(final IDAOFactory daoFactory, final File fileStore, final int fileRetentionInMinutes)
    {
        super(daoFactory);
        assert fileStore.exists() : "File store does not exist.";
        assert fileStore.isDirectory() : "File store is not a directory";

        this.fileStore = fileStore;
        this.fileRetentionInMinutes = fileRetentionInMinutes;
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
        final File file = new File(fileStore, path);
        if (file.exists())
        {
            file.delete();
            if (logger.isInfoEnabled())
            {
                logger.info("Expired file [" + path + "] deleted.");
            }
        } else
        {
            logger.warn("Expired file [" + path + "] not deleted: doesn't exist.");
        }
    }

    //
    // IFileManager
    //

    @Transactional
    public final void deleteExpiredFiles()
    {
        final List<FileDTO> expiredFiles = daoFactory.getFileDAO().getExpiredFiles();
        for (final FileDTO file : expiredFiles)
        {
            daoFactory.getFileDAO().deleteFile(file.getID());
            deleteFromFileSystem(file.getPath());
        }
    }

    @Transactional
    public final FileOutput getFile(final UserDTO userDTO, final long fileId)
    {
        assert userDTO != null : "Given user can not be null.";

        final FileDTO file = getFile(fileId);
        final java.io.File realFile = new java.io.File(fileStore, file.getPath());
        if (realFile.exists() == false)
        {
            throw new UserFailureException(String.format("File '%s' no longer available.", realFile.getAbsolutePath()));
        }
        final List<UserDTO> sharingUsers = file.getSharingUsers();
        if (containsUser(userDTO, sharingUsers))
        {
            throw UserFailureException.fromTemplate("Current user '%s' does not have access to file '%s'.", userDTO
                    .getUserName(), file.getPath());
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
    public final void saveFile(final UserDTO user, final String fileName, final String contentType,
            final InputStream input)
    {
        final File folder = new File(fileStore, user.getEmail());
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
        final File file = new File(folder, fileName);
        OutputStream outputStream = null;
        CountingInputStream inputStream = null;
        try
        {
            outputStream = new FileOutputStream(file);
            inputStream = new CountingInputStream(input);
            IOUtils.copy(inputStream, outputStream);
            final FileDTO fileDTO = new FileDTO(user.getID());
            fileDTO.setName(fileName);
            fileDTO.setContentType(contentType);
            fileDTO.setPath(FileUtilities.getRelativeFile(fileStore, file));
            fileDTO.setExpirationDate(DateUtils.addMinutes(new Date(), fileRetentionInMinutes));
            fileDTO.setSize(inputStream.getByteCount());
            daoFactory.getFileDAO().createFile(fileDTO);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
}
