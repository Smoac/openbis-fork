/*
 * Copyright 2007 ETH Zuerich, CISD
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
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;

/**
 * Data access object for {@link ProcedureTypePE}.
 * 
 * @author Christian Ribeaud
 */
final class ProcedureTypeDAO extends AbstractTypeDAO<ProcedureTypePE> implements IProcedureTypeDAO
{

    ProcedureTypeDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // IProcedureTypeDAO
    //

    public ProcedureTypePE tryFindProcedureTypeByCode(final String code) throws DataAccessException
    {
        return tryFindTypeByCode(code);
    }

    public final List<ProcedureTypePE> listProcedureTypes() throws DataAccessException
    {
        return listTypes();
    }

    @Override
    Class<ProcedureTypePE> getEntityClass()
    {
        return ProcedureTypePE.class;
    }

}
