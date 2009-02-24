/*
* Copyright 2007 ETH Zuerich, CISD
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;

/**
 * Test cases for corresponding {@link ProcedureBO} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class ProcedureBOTest extends AbstractBOTest
{

    private static final String EXPERIMENT_CODE = "EXP";

    @Test
    public void testDefine()
    {
        final ExperimentPE experimentPE = createExperiment();
        final String procedureTypeCode = "p1";
        final ProcedureTypePE procedureType = new ProcedureTypePE();
        procedureType.setCode(procedureTypeCode);
        context.checking(new Expectations()
            {
                {
                    one(procedureTypeDAO).tryFindProcedureTypeByCode(procedureTypeCode);
                    will(returnValue(procedureType));
                }
            });

        final IProcedureBO procedureBO = createProcedureBO();
        procedureBO.define(experimentPE, procedureTypeCode);
        final ProcedurePE procedure = procedureBO.getProcedure();
        assertEquals(EXPERIMENT_CODE, procedure.getExperiment().getCode());
        assertEquals(procedureTypeCode, procedure.getProcedureType().getCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineButProcedureTypeCodeUnknown()
    {
        final ExperimentPE experiment = createExperiment();
        final String procedureTypeCode = "p1";
        prepareForDefineTest(procedureTypeCode, null);
        try
        {
            createProcedureBO().define(experiment, procedureTypeCode);
            AssertJUnit.fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Unknown procedure type " + procedureTypeCode + ".", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testSaveButNothingToSave()
    {
        createProcedureBO().save();

        context.assertIsSatisfied();
    }

    @Test
    public void testSave()
    {
        final ExperimentPE experiment = createExperiment();
        final String procedureTypeCode = "p1";
        final ProcedureTypePE procedureTypeDTO = new ProcedureTypePE();
        procedureTypeDTO.setCode(procedureTypeCode);
        prepareForDefineTest(procedureTypeCode, procedureTypeDTO);
        final IProcedureBO procedureBO = createProcedureBO();
        procedureBO.define(experiment, procedureTypeCode);
        final ProcedurePE procedure = procedureBO.getProcedure();
        context.checking(new Expectations()
            {
                {
                    one(procedureDAO).createProcedure(procedure);
                }
            });
        procedureBO.save();
        context.assertIsSatisfied();
    }

    @Test
    public void testTySaveWhenDataAccessExceptionThrown()
    {
        final ExperimentPE experiment = createExperiment();
        final String procedureTypeCode = "p1";
        final ProcedureTypePE procedureTypeDTO = new ProcedureTypePE();
        procedureTypeDTO.setCode(procedureTypeCode);
        prepareForDefineTest(procedureTypeCode, procedureTypeDTO);
        final IProcedureBO procedureBO = createProcedureBO();
        procedureBO.define(experiment, procedureTypeCode);
        final ProcedurePE procedure = procedureBO.getProcedure();
        context.checking(new Expectations()
            {
                {
                    one(procedureDAO).createProcedure(procedure);
                    will(throwException(new DataIntegrityViolationException("data access problem")));
                }
            });
        try
        {
            procedureBO.save();
            fail("UserFailureException thrown.");
        } catch (final UserFailureException ex)
        {
        }
        context.assertIsSatisfied();
    }

    private static ExperimentPE createExperiment()
    {
        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(EXPERIMENT_CODE);
        return experiment;
    }

    private void prepareForDefineTest(final String procedureTypeCode,
            final ProcedureTypePE procedureTypeDTO)
    {
        context.checking(new Expectations()
            {
                {
                    one(procedureTypeDAO).tryFindProcedureTypeByCode(procedureTypeCode);
                    will(returnValue(procedureTypeDTO));
                }
            });
    }

    private IProcedureBO createProcedureBO()
    {
        return new ProcedureBO(daoFactory, EXAMPLE_SESSION);
    }
}
