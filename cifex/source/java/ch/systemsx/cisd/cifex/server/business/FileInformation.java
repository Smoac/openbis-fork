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

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;

/**
 * A class that holds information about a file in the database. Either it has the {@link FileDTO} or
 * it has an error message describing what is wrong.
 * 
 * @author Bernd Rinn
 */
public class FileInformation
{

    private final FileDTO fileDTOOrNull;
    
    private final File fileOrNull;

    private final long fileId;

    private final String errorMessageOrNull;

    public FileInformation(final long fileId, final FileDTO fileDTO, final File file)
    {
        assert fileId > 0;
        this.fileId = fileId;
        this.fileDTOOrNull = fileDTO;
        this.fileOrNull = file;
        this.errorMessageOrNull = null;
    }

    public FileInformation(final long fileId, final String errorMessage)
    {
        assert fileId > 0;
        this.fileId = fileId;
        this.errorMessageOrNull = errorMessage;
        this.fileDTOOrNull = null;
        this.fileOrNull = null;
    }

    /**
     * Returns <code>true</code> if the file is available.
     */
    public boolean isFileAvailable()
    {
        return (fileDTOOrNull != null);
    }

    /**
     * Returns the id of the file in the database.
     */
    public long getFileId()
    {
        return fileId;
    }

    /**
     * Returns the DTO of the file.
     * 
     * @throws IllegalStateException If the file is not available (see {@link #isFileAvailable()})
     */
    public FileDTO getFileDTO() throws IllegalStateException
    {
        if (isFileAvailable() == false)
        {
            throw new IllegalStateException(errorMessageOrNull);
        }
        return fileDTOOrNull;
    }

    /**
     * Returns the file object, pointing to the file store.
     * 
     * @throws IllegalStateException If the file is not available (see {@link #isFileAvailable()})
     */
    public File getFile() throws IllegalStateException
    {
        if (isFileAvailable() == false)
        {
            throw new IllegalStateException(errorMessageOrNull);
        }
        return fileOrNull;
    }

    /**
     * Returns the error message describing why the file is not available.
     * 
     * @throws IllegalStateException If the file is available (see {@link #isFileAvailable()})
     */
    public String getErrorMessage()
    {
        if (isFileAvailable())
        {
            throw new IllegalStateException("File [id=" + fileId + "] is available.");
        }
        return errorMessageOrNull;
    }

}
