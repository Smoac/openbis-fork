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

import static org.testng.AssertJUnit.assertEquals;

import java.util.Date;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * Test cases for corresponding {@link ProcedureDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "procedure" })
public final class ProcedureDAOTest extends AbstractDAOTest
{

    static final long ID_OF_EXPERIMENT1 = 10L;

    static final String PROECDURE_TYPE_CODE_UNKNOWN = "UNKNOWN";

    public final void testCreateProcedure()
    {
        final ProcedureTypePE type =
                daoFactory.getProcedureTypeDAO().tryFindProcedureTypeByCode(
                        ProcedureTypeCode.DATA_ACQUISITION.getCode());
        ExperimentPE experiment =
                createExperiment("CISD", "CISD", "DEFAULT", "EXP_NO_PROC", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment);
        ProcedurePE procedure = createProcedure(type, experiment);
        assertEquals(0, daoFactory.getProcedureDAO().listProcedures(experiment).size());
        daoFactory.getProcedureDAO().createProcedure(procedure);
        assertEquals(1, daoFactory.getProcedureDAO().listProcedures(experiment).size());
    }

    public final void testCreateProcedureWithTypeUnknown()
    {
        ExperimentPE experiment =
                createExperiment("CISD", "CISD", "DEFAULT", "EXP_NO_PROC", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment);

        final ProcedureTypePE typeDataAcquisition =
                daoFactory.getProcedureTypeDAO().tryFindProcedureTypeByCode(
                        ProcedureTypeCode.UNKNOWN.getCode());
        ProcedurePE procedureDA = createProcedure(typeDataAcquisition, experiment);
        assertEquals(0, daoFactory.getProcedureDAO().listProcedures(experiment).size());
        daoFactory.getProcedureDAO().createProcedure(procedureDA);
        assertEquals(1, daoFactory.getProcedureDAO().listProcedures(experiment).size());

        final ProcedureTypePE typeUnknown =
                daoFactory.getProcedureTypeDAO().tryFindProcedureTypeByCode(
                        ProcedureTypeCode.UNKNOWN.getCode());
        ProcedurePE procedureUnknown = createProcedure(typeUnknown, experiment);
        daoFactory.getProcedureDAO().createProcedure(procedureUnknown);
        assertEquals(2, daoFactory.getProcedureDAO().listProcedures(experiment).size());
    }

    private ProcedurePE createProcedure(final ProcedureTypePE type, ExperimentPE experiment)
    {
        ProcedurePE procedure = new ProcedurePE();
        procedure.setProcedureType(type);
        procedure.setRegistrationDate(new Date());
        procedure.setExperiment(experiment);
        return procedure;
    }

}