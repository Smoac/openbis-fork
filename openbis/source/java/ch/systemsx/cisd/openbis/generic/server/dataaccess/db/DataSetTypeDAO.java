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

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Data access object for {@link DataSetTypePE}.
 * 
 * @author     Franz-Josef Elmer
 */
public class DataSetTypeDAO extends AbstractTypeDAO<DataSetTypePE> implements IDataSetTypeDAO
{
    public DataSetTypeDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, DataSetTypePE.class);
    }

    public DataSetTypePE tryToFindDataSetTypeByCode(String code)
    {
        return tryFindTypeByCode(code);
    }

}
