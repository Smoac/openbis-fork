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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * The only productive implementation of {@link IProcedureBO}. We are using an interface here to
 * keep the system testable.
 * 
 * @author Franz-Josef Elmer
 */
public final class ProcedureBO extends AbstractBusinessObject implements IProcedureBO
{
    private ProcedurePE procedure;

    private boolean shouldBeSaved;

    public ProcedureBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // IProcedureBO
    //

    public final ProcedurePE getProcedure()
    {
        return procedure;
    }

    public final void define(final ExperimentPE experiment, final String procedureTypeCode)
    {
        final ProcedureTypePE procedureType = findProcedureType(procedureTypeCode);
        procedure = new ProcedurePE();
        procedure.setExperiment(experiment);
        procedure.setProcedureType(procedureType);
        shouldBeSaved = true;
    }

    private ProcedureTypePE findProcedureType(final String procedureTypeCode)
    {
        final ProcedureTypePE procedureType =
                getProcedureTypeDAO().tryFindProcedureTypeByCode(procedureTypeCode);
        if (procedureType == null)
        {
            throw new UserFailureException("Unknown procedure type " + procedureTypeCode + ".");
        }
        return procedureType;
    }

    public final void save()
    {
        if (shouldBeSaved)
        {
            assert procedure != null : "Can not save an undefined procedure.";
            try
            {
                getProcedureDAO().createProcedure(procedure);
            } catch (final DataAccessException ex)
            {
                throwException(ex, String.format("Procedure of type '%s'", procedure
                        .getProcedureType().getCode()));
            }
        }
    }

}
