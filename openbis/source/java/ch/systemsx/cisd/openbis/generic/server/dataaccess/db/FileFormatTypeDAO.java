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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * Data access object for {@link FileFormatTypePE}.
 * 
 * @author Franz-Josef Elmer
 */
public class FileFormatTypeDAO extends AbstractTypeDAO<FileFormatTypePE> implements
        IFileFormatTypeDAO
{
    public FileFormatTypeDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, FileFormatTypePE.class);
    }

    public FileFormatTypePE tryToFindFileFormatTypeByCode(String code)
    {
        return tryFindTypeByCode(code);
    }

    public List<FileFormatTypePE> listFileFormatTypes()
    {
        return listTypes();
    }

    public void createOrUpdate(FileFormatTypePE fileFormatType)
    {
        assert fileFormatType != null : "File Format Type is null";
        if (fileFormatType.getDatabaseInstance() == null)
        {
            fileFormatType.setDatabaseInstance(getDatabaseInstance());
        }
        createOrUpdateType(fileFormatType);
    }

}
