/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * System tests for
 * {@link IETLLIMSService#performEntityOperations(String, AtomicEntityOperationDetails)}
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class EntityOperationTest extends SystemTestCase
{
    private static final String PREFIX = "EO_";

    private static final String SPACE_ETL_SERVER_FOR_A = PREFIX + "S_ETL_A";

    private static final SpaceIdentifier SPACE_A = new SpaceIdentifier("CISD", "CISD");

    private static final SpaceIdentifier SPACE_B = new SpaceIdentifier("CISD", "TESTGROUP");

    private static final class EntityOperationBuilder
    {
        private final List<NewSpace> spaces = new ArrayList<NewSpace>();

        private final List<NewProject> projects = new ArrayList<NewProject>();

        private final List<NewExperiment> experiments = new ArrayList<NewExperiment>();

        private final List<NewSample> samples = new ArrayList<NewSample>();

        private final List<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        private final List<? extends NewExternalData> dataSets = new ArrayList<NewExternalData>();

        private final List<DataSetUpdatesDTO> dataSetUpdates = new ArrayList<DataSetUpdatesDTO>();

        private final Map<String, List<NewMaterial>> materials =
                new HashMap<String, List<NewMaterial>>();

        private TechId registrationID;

        private String userID;

        EntityOperationBuilder(long registrationID)
        {
            this.registrationID = new TechId(registrationID);
        }

        EntityOperationBuilder user(String userID)
        {
            this.userID = userID;
            return this;
        }

        EntityOperationBuilder space(String code)
        {
            return space(new NewSpace(code, null, null));
        }

        EntityOperationBuilder space(NewSpace space)
        {
            spaces.add(space);
            return this;
        }

        EntityOperationBuilder project(SpaceIdentifier spaceIdentifier, String projectCode)
        {
            String projectIdentifier =
                    new ProjectIdentifier(spaceIdentifier, projectCode).toString();
            return project(new NewProject(projectIdentifier, null));
        }

        EntityOperationBuilder project(NewProject project)
        {
            projects.add(project);
            return this;
        }

        AtomicEntityOperationDetails create()
        {
            return new AtomicEntityOperationDetails(registrationID, userID, spaces, projects,
                    experiments, sampleUpdates, samples, materials, dataSets, dataSetUpdates);
        }

    }

    @BeforeClass
    public void createTestUsers()
    {
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_A), RoleCode.ETL_SERVER, SPACE_A);
    }

    @Test
    public void testCreateProjectAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder(1).project(SPACE_A, "P1").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);

        assertEquals("[/" + SPACE_A.getSpaceCode() + "/P1]", result.getProjectsCreated().toString());
    }

    @Test
    public void testCreateProjectAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder(1).project(SPACE_B, "P1").create();

        try
        {
            etlService.performEntityOperations(sessionToken, eo);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: ERROR: \"User '" + SPACE_ETL_SERVER_FOR_A
                    + "' does not have enough privileges.\".", ex.getMessage());
        }
    }
}
