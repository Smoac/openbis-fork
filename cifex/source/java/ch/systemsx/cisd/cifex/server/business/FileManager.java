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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.h2.util.IOUtils;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * The only <code>IFileManager</code> implementation.
 * 
 * @author Christian Ribeaud
 */
final class FileManager extends AbstractManager implements IFileManager
{
    private final File fileStore;

    FileManager(final IDAOFactory daoFactory, final File fileStore)
    {
        super(daoFactory);
        assert fileStore.exists() : "File store does not exist.";
        assert fileStore.isDirectory() : "File store is not a directory";
        
        this.fileStore = fileStore;
    }

    //
    // IFileManager
    //

    public final FileDTO tryGetFile(final long fileId)
    {
        // TODO 2008-01-24, Christian Ribeaud: replace by code using data access layer
        return null;
    }

    public void saveFile(UserDTO user, String fileName, InputStream inputStream)
    {
        File folder = new File(fileStore, user.getEmail());
        if (folder.exists())
        {
            if (folder.isDirectory() == false)
            {
                throw new EnvironmentFailureException("Folder '" + folder.getAbsolutePath()
                        + "' exists but is not a directory.");
            }
        } else
        {
            boolean successful = folder.mkdirs();
            if (successful == false)
            {
                throw new EnvironmentFailureException("Folder '" + folder.getAbsolutePath()
                        + "' can not be created for some unknown reason.");
            }
        }
        File file = new File(folder, "dummy");
        FileOutputStream fileOutputStream  = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeSilently(inputStream);
            IOUtils.closeSilently(fileOutputStream);
        }
    }
}
