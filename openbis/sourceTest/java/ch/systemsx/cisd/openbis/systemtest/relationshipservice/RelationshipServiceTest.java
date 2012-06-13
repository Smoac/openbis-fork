/*
 * Copyright 2009 ETH Zuerich, CISD
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

import org.hamcrest.Matcher;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author anttil
 */
@Test(groups = "system test")
public abstract class RelationshipServiceTest extends SystemTestCase
{

    @DataProvider(name = "All 2-permutations of space level roles weaker than {ADMIN, ADMIN}")
    protected static RoleCode[][] getRoleCombinations()
    {
        return new RoleCode[][]
            {
                    { RoleCode.OBSERVER, RoleCode.OBSERVER },
                    { RoleCode.OBSERVER, RoleCode.USER },
                    { RoleCode.OBSERVER, RoleCode.POWER_USER },
                    { RoleCode.OBSERVER, RoleCode.ETL_SERVER },
                    { RoleCode.OBSERVER, RoleCode.ADMIN },
                    { RoleCode.USER, RoleCode.OBSERVER },
                    { RoleCode.USER, RoleCode.USER },
                    { RoleCode.USER, RoleCode.POWER_USER },
                    { RoleCode.USER, RoleCode.ETL_SERVER },
                    { RoleCode.USER, RoleCode.ADMIN },
                    { RoleCode.POWER_USER, RoleCode.OBSERVER },
                    { RoleCode.POWER_USER, RoleCode.USER },
                    { RoleCode.POWER_USER, RoleCode.POWER_USER },
                    { RoleCode.POWER_USER, RoleCode.ETL_SERVER },
                    { RoleCode.POWER_USER, RoleCode.ADMIN },
                    { RoleCode.ETL_SERVER, RoleCode.OBSERVER },
                    { RoleCode.ETL_SERVER, RoleCode.USER },
                    { RoleCode.ETL_SERVER, RoleCode.POWER_USER },
                    { RoleCode.ETL_SERVER, RoleCode.ETL_SERVER },
                    { RoleCode.ETL_SERVER, RoleCode.ADMIN },
                    { RoleCode.ADMIN, RoleCode.OBSERVER },
                    { RoleCode.ADMIN, RoleCode.USER },
                    { RoleCode.ADMIN, RoleCode.POWER_USER },
                    { RoleCode.ADMIN, RoleCode.ETL_SERVER } };
    }

    @DataProvider(name = "Instance level roles below ADMIN")
    protected static RoleCode[][] getRolesBelowAdmin()
    {
        return new RoleCode[][]
            {
                    { RoleCode.OBSERVER },
                    { RoleCode.ETL_SERVER } };
    }

    protected static <T> T create(Builder<T> builder)
    {
        return builder.create();
    }

    protected SampleBuilder aSample()
    {
        return new SampleBuilder(commonServer, genericServer);
    }

    protected ProjectUpdateBuilder aProjectUpdate(Project project)
    {
        return new ProjectUpdateBuilder(commonServer, genericServer, project);
    }

    protected ExperimentUpdateBuilder anExperimentUpdate(Experiment experiment)
    {
        return new ExperimentUpdateBuilder(commonServer, genericServer, experiment);
    }

    protected SessionBuilder aSession()
    {
        return new SessionBuilder(commonServer, genericServer);
    }

    protected ExperimentBuilder anExperiment()
    {
        return new ExperimentBuilder(commonServer, genericServer);
    }

    protected ProjectBuilder aProject()
    {
        return new ProjectBuilder(commonServer, genericServer);
    }

    protected SpaceBuilder aSpace()
    {
        return new SpaceBuilder(commonServer, genericServer);
    }

    protected Matcher<Object> inSpace(Space space)
    {
        return new InSpaceMatcher(space);
    }

    protected Matcher<Experiment> inProject(Project project)
    {
        return new InProjectMatcher(project);
    }

    protected Experiment serverSays(Experiment experiment)
    {
        return commonServer.getExperimentInfo(systemSessionToken, new TechId(experiment.getId()));
    }

    protected Project serverSays(Project project)
    {
        return commonServer.getProjectInfo(systemSessionToken, new TechId(project.getId()));
    }

    protected Sample serverSays(Sample sample)
    {
        SampleParentWithDerived result =
                commonServer.getSampleInfo(systemSessionToken, new TechId(sample.getId()));
        return result.getParent();
    }
}
