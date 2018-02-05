/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class DeleteProjectTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteProjectWithAdminUserInAnotherSpace()
    {
        final ProjectPermId permId = new ProjectPermId("20120814110011738-105");

        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    ProjectDeletionOptions options = new ProjectDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteProjects(sessionToken, Collections.singletonList(permId), options);
                }
            });
    }
    
    @Test
    public void testDeleteProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId = createCisdProject();

        ProjectDeletionOptions options = new ProjectDeletionOptions();
        options.setReason("It is just a test");

        assertProjectExists(permId);

        v3api.deleteProjects(sessionToken, Collections.singletonList(permId), options);

        assertProjectDoesNotExist(permId);
    }

    @Test
    public void testDeleteProjectWithExperiment()
    {
        final ProjectPermId projectPermId = createCisdProject();
        final ExperimentPermId experimentPermId = createCisdExperiment(projectPermId);

        final ProjectDeletionOptions options = new ProjectDeletionOptions();
        options.setReason("It is just a test");

        assertProjectExists(projectPermId);
        assertExperimentExists(experimentPermId);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);
                    v3api.deleteProjects(sessionToken, Collections.singletonList(projectPermId), options);
                }
            }, "Project 'PROJECT_TO_DELETE' can not be deleted because the following experiments still exist: [EXPERIMENT_TO_DELETE]");
    }

    @Test
    public void testDeleteProjectUnauthorized()
    {
        final ProjectPermId permId = createCisdProject();

        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

                    ProjectDeletionOptions options = new ProjectDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteProjects(sessionToken, Collections.singletonList(permId), options);
                }
            });
    }

}
