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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ExperimentTypeCode;

/**
 * Test cases for {@link ExperimentDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "experiment" })
public class ExperimentDAOTest extends AbstractDAOTest
{
    //
    // Experiments existing in the test database
    //
    private static final String CISD_CISD_DEFAULT_EXP_REUSE = "CISD:/CISD/DEFAULT/EXP-REUSE";

    private static final String CISD_CISD_DEFAULT_EXP_X = "CISD:/CISD/DEFAULT/EXP-X";

    private static final String CISD_CISD_NEMO_EXP1 = "CISD:/CISD/NEMO/EXP1";

    private static final String CISD_CISD_NEMO_EXP10 = "CISD:/CISD/NEMO/EXP10";

    private static final String CISD_CISD_NEMO_EXP11 = "CISD:/CISD/NEMO/EXP11";

    @Test
    public void testListExperiments() throws Exception
    {
        final List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEquals(5, experiments.size());
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, experiments.get(0).getIdentifier());
        assertEquals(CISD_CISD_DEFAULT_EXP_X, experiments.get(1).getIdentifier());
        assertEquals(CISD_CISD_NEMO_EXP1, experiments.get(2).getIdentifier());
        assertEquals(CISD_CISD_NEMO_EXP10, experiments.get(3).getIdentifier());
        assertEquals(CISD_CISD_NEMO_EXP11, experiments.get(4).getIdentifier());
    }

    @Test
    public void testListExperimentsFromProject() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEquals(5, experiments.size());
        final ExperimentPE expInNemo = experiments.get(4);
        assertEquals(CISD_CISD_NEMO_EXP11, expInNemo.getIdentifier());

        final ProjectPE projectNemo = expInNemo.getProject();
        assertEquals(ProjectDAOTest.NEMO, projectNemo.getCode());

        final ExperimentTypePE expType = expInNemo.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments = daoFactory.getExperimentDAO().listExperiments(expType, projectNemo);
        Collections.sort(experiments);
        assertEquals(3, experiments.size());
        ExperimentPE exp1 = experiments.get(0);
        assertEquals(CISD_CISD_NEMO_EXP1, exp1.getIdentifier());
        List<ProcedurePE> procedures = exp1.getProcedures();
        assertEquals(2, procedures.size());
        assertEquals(1, procedures.get(0).getData().size());
        assertEquals(1, procedures.get(1).getData().size());
        assertEquals(CISD_CISD_NEMO_EXP10, experiments.get(1).getIdentifier());
        assertEquals(CISD_CISD_NEMO_EXP11, experiments.get(2).getIdentifier());
    }

    @Test
    public void testListExperimentsFromAnotherProject() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEquals(5, experiments.size());
        final ExperimentPE expInDefault = experiments.get(0);
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, expInDefault.getIdentifier());

        final ProjectPE projectDefault = expInDefault.getProject();
        assertEquals(ProjectDAOTest.DEFAULT, projectDefault.getCode());

        final ExperimentTypePE expType = expInDefault.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments = daoFactory.getExperimentDAO().listExperiments(expType, projectDefault);
        Collections.sort(experiments);
        assertEquals(2, experiments.size());
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, experiments.get(0).getIdentifier());
        assertEquals(CISD_CISD_DEFAULT_EXP_X, experiments.get(1).getIdentifier());
    }

    @Test
    public void testListExperimentsOfAnotherType() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEquals(5, experiments.size());
        final ExperimentPE expInDefault = experiments.get(0);
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, expInDefault.getIdentifier());

        final ProjectPE projectDefault = expInDefault.getProject();
        assertEquals(ProjectDAOTest.DEFAULT, projectDefault.getCode());

        final List<EntityTypePE> types =
                daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT).listEntityTypes();
        Collections.sort(types);
        assertEquals(2, types.size());
        final ExperimentTypePE expType = (ExperimentTypePE) types.get(0);
        assertEquals(ExperimentTypeCode.COMPOUND_HCS.getCode(), expType.getCode());

        experiments = daoFactory.getExperimentDAO().listExperiments(expType, projectDefault);
        Collections.sort(experiments);
        assertEquals(0, experiments.size());
    }

    @Test
    public void testTryFindByCodeAndProject()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEquals(5, experiments.size());
        final ExperimentPE templateExp = experiments.get(2);
        assertEquals(CISD_CISD_NEMO_EXP1, templateExp.getIdentifier());

        ExperimentPE experiment = daoFactory.getExperimentDAO().tryFindByCodeAndProject(
                templateExp.getProject(), templateExp.getCode());
        
        assertEquals(CISD_CISD_NEMO_EXP1, experiment.getIdentifier());
        List<ProcedurePE> procedures = experiment.getProcedures();
        assertEquals(2, procedures.size());
        assertEquals(1, procedures.get(0).getData().size());
        assertEquals(1, procedures.get(1).getData().size());
    }

    @Test
    public void testTryFindByCodeAndProjectNonexistent()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEquals(5, experiments.size());
        final ExperimentPE templateExp = experiments.get(4);
        assertEquals(CISD_CISD_NEMO_EXP11, templateExp.getIdentifier());

        AssertJUnit.assertNull(daoFactory.getExperimentDAO().tryFindByCodeAndProject(
                templateExp.getProject(), "nonexistent"));
    }

    @Test
    public void testCreateExperiment() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(5, experimentsBefore.size());

        ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", "EXP12", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment);

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(6, experimentsAfter.size());
        Collections.sort(experimentsAfter);
        assertExperimentsEqual(experiment, experimentsAfter.get(5));
    }

    @Test
    public void testCreateExperimentsOfDifferentTypes() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(5, experimentsBefore.size());

        ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", "EXP13", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment);

        ExperimentPE experiment2 =
                createExperiment("CISD", "CISD", "NEMO", "EXP12", "COMPOUND_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment2);

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experimentsAfter);
        assertEquals(7, experimentsAfter.size());
        assertExperimentsEqual(experiment, experimentsAfter.get(6));
        assertEquals(experiment.getExperimentType().getCode(), experimentsAfter.get(6)
                .getExperimentType().getCode());
        assertExperimentsEqual(experiment2, experimentsAfter.get(5));
        assertEquals(experiment2.getExperimentType().getCode(), experimentsAfter.get(5)
                .getExperimentType().getCode());
    }

    @Test
    public void testTryCreateExperimentWithExistingIdentifier() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experimentsBefore);
        assertEquals(5, experimentsBefore.size());
        assertEquals(CISD_CISD_NEMO_EXP11, experimentsBefore.get(4).getIdentifier());

        ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", "EXP11", "SIRNA_HCS");
        boolean exceptionThrown = false;
        try
        {
            daoFactory.getExperimentDAO().createExperiment(experiment);
        } catch (DataIntegrityViolationException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test(dataProvider = "illegalCodesProvider")
    public final void testCreateExperimentWithIllegalCode(String code)
    {
        final ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", code, "SIRNA_HCS");
        boolean exceptionThrown = false;
        try
        {
            daoFactory.getExperimentDAO().createExperiment(experiment);
        } catch (final DataIntegrityViolationException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private void assertExperimentsEqual(ExperimentPE e1, ExperimentPE e2)
    {
        assertEquals(e1.getCode(), e2.getCode());
        assertEquals(e1.getExperimentType(), e2.getExperimentType());
        assertEquals(e1.getProject(), e2.getProject());
        assertEquals(e1.getRegistrator(), e2.getRegistrator());
        assertEquals(e1.getRegistrationDate(), e2.getRegistrationDate());
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] illegalCodesProvider()
    {
        return new Object[][]
            {
                { EXCEED_40_CHARACTERS },
                { "" },
                { null },
                { "@XPERIMENT" }, };
    }

}
