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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author Franz-Josef Elmer
 */
public class ETLServiceAuthorizationTest extends BaseTest
{
    private Space space;

    private Space anotherSpace;

    private Project project;

    private Project anotherProject;

    private Experiment experiment;

    @BeforeClass
    public void createSomeEntities()
    {
        space = create(aSpace());
        anotherSpace = create(aSpace());
        project = create(aProject().inSpace(space));
        anotherProject = create(aProject().inSpace(anotherSpace));
        experiment = create(anExperiment().inProject(project));
        create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment));
    }

    @Test
    public void testListSamplesForInstanceAdmin()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        List<Sample> samples =
                etlService.listSamples(sessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(experiment.getId())));

        assertEquals(2, samples.size());
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void testListSamplesForObserverForAnotherSpace()
    {
        String sessionToken =
                create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, anotherSpace));

        etlService.listSamples(sessionToken,
                ListSampleCriteria.createForExperiment(new TechId(experiment.getId())));
    }

    @Test
    public void testListProjectsForInstanceAdmin()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        List<Project> projects = etlService.listProjects(sessionToken);

        assertEquals(2, projects.size());
    }

    @Test
    public void testListProjectsForObserverForSpace()
    {
        String sessionToken =
                create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, space));

        List<Project> projects = etlService.listProjects(sessionToken);

        assertEquals(1, projects.size());
    }
}
