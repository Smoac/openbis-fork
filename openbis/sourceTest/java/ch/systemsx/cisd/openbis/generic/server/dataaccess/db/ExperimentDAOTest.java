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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
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

    private static final String CISD_CISD_NEMO_EXP11 = "CISD:/CISD/NEMO/EXP11";

    private static final String CISD_CISD_NEMO_EXP10 = "CISD:/CISD/NEMO/EXP10";

    private static final String CISD_CISD_NEMO_EXP1 = "CISD:/CISD/NEMO/EXP1";

    private static final String CISD_CISD_DEFAULT_EXP_X = "CISD:/CISD/DEFAULT/EXP-X";

    private static final String CISD_CISD_DEFAULT_EXP_REUSE = "CISD:/CISD/DEFAULT/EXP-REUSE";

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
        assertEquals(CISD_CISD_NEMO_EXP1, experiments.get(0).getIdentifier());
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

}
