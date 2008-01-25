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

package ch.systemsx.cisd.cifex.server.business.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;

/**
 * <i>Data Access Object</i> for files.
 * 
 * @author Izabela Adamczyk
 */
public interface IFileDAO
{
    /**
     * Inserts given <code>File</code> into the database.
     * <p>
     * As side effect the <i>unique identifier</i> returned by the database is set to given <code>File</code> object
     * using {@link FileDTO#setID(Long)}.
     * </p>
     * 
     * @param file <code>File</code> object to be inserted into the database. Can not be <code>null</code>.
     */
    public void createFile(final FileDTO file) throws DataAccessException;

    /**
     * Removes <code>File</code> with given id from database.
     * 
     * @param id Id of file which should be removed from database.
     */
    public void deleteFile(final long id) throws DataAccessException;

    /**
     * Returns a list of all files existing in database.
     */
    public List<FileDTO> listFiles() throws DataAccessException;

    /** Returns detailed information about file, including registerer data and list of users the file is shared with. */
    public FileDTO tryGetFile(final long id) throws DataAccessException;
}
