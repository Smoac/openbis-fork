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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;

public class ChangeSpaceOfAProjectTest extends RelationshipServiceTest
{
    @Test
    public void spaceAdminOfBothSpacesIsAllowedToUpdateProjectSpaceRelationship()
    {
        Space sourceSpace = aSpace().create();
        Space destinationSpace = aSpace().create();
        Project project = aProject().inSpace(sourceSpace).create();

        Experiment experiment1 = anExperiment().inProject(project).create();
        Experiment experiment2 = anExperiment().inProject(project).create();

        Sample sample1 = aSample().inExperiment(experiment1).create();
        Sample sample2 = aSample().inExperiment(experiment2).create();

        ProjectUpdatesDTO updates = aProjectUpdate(project).withSpace(destinationSpace).create();

        String session =
                aSession().withSpaceRole(RoleCode.ADMIN, sourceSpace).withSpaceRole(RoleCode.ADMIN,
                        destinationSpace).create();

        commonServer.updateProject(session, updates);

        assertThat(serverSays(project), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample1), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample2), is(inSpace(destinationSpace)));
    }

}
