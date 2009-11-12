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

package ch.systemsx.cisd.cifex.server.business;

import java.io.File;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;

/**
 * A data transfer object for a pre-created file. 
 *
 * @author Bernd Rinn
 */
public final class PreCreatedFileDTO
{
    
    private final File file;
    
    private final FileDTO fileDTO;

    public PreCreatedFileDTO(File file, FileDTO fileDTO)
    {
        super();
        this.file = file;
        this.fileDTO = fileDTO;
    }

    /**
     * Returns the file in the data store.
     */
    public final File getFile()
    {
        return file;
    }

    /**
     * Returns the link of the file stored in the database.
     */
    public final FileDTO getFileDTO()
    {
        return fileDTO;
    }

}
