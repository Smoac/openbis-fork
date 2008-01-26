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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
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

    FileManager(final IDAOFactory daoFactory, final File fileStore, final int fileRetentionInMinutes)
    {
        super(daoFactory);
        assert fileStore.exists() : "File store does not exist.";
        assert fileStore.isDirectory() : "File store is not a directory";

        this.fileStore = fileStore;
        this.fileRetentionInMinutes = fileRetentionInMinutes;
    }

    //
    // IFileManager
    //

    @Transactional
    public final FileOutput getFile(final UserDTO userDTO, final long fileId)
    {
        assert userDTO != null : "Given user can not be null.";

        final FileDTO file = daoFactory.getFileDAO().tryGetFile(fileId);
        final java.io.File realFile = new java.io.File(fileStore, file.getPath());
        if (realFile.exists() == false)
        {
            throw new UserFailureException(String.format("File '%s' no longer available."));
        }
        return null;
    }

    @Transactional
    public final void saveFile(final UserDTO user, final String fileName, final String contentType,
            final InputStream inputStream)
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
        // TODO 2008-01-26, Christian Ribeaud: monitor available space to avoid surprises (FileSystemUtils may help?)
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, fileOutputStream);
            final FileDTO fileDTO = new FileDTO(user.getID());
            fileDTO.setName(fileName);
            fileDTO.setContentType(contentType);
            fileDTO.setPath(FileUtilities.getRelativeFile(fileStore, file));
            fileDTO.setExpirationDate(DateUtils.addMinutes(new Date(), fileRetentionInMinutes));
            daoFactory.getFileDAO().createFile(fileDTO);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }
}
