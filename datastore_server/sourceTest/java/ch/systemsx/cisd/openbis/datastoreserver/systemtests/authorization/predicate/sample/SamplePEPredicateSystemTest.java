/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonSamplePredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class SamplePEPredicateSystemTest extends CommonSamplePredicateSystemTest<SamplePE>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ_WRITE;
    }

    @Override
    protected SamplePE createNonexistentObject(Object param)
    {
        SpacePE space = new SpacePE();
        space.setCode("IDONTEXIST");

        ProjectPE project = new ProjectPE();
        project.setCode("IDONTEXIST");
        project.setSpace(space);

        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("IDONTEXIST");
        experiment.setProject(project);

        SamplePE sample = new SamplePE();
        sample.setCode("IDONTEXIST");

        SamplePE container = new SamplePE();
        container.setCode("IDONTEXIST");

        switch ((SampleKind) param)
        {
            case SHARED_READ:
            case SHARED_READ_WRITE:
                return sample;
            case SPACE:
                sample.setSpace(space);
                return sample;
            case SPACE_CONTAINED:
                sample.setSpace(space);
                sample.setContainer(container);
                return sample;
            case PROJECT:
                sample.setSpace(space);
                sample.setProject(project);
                return sample;
            case EXPERIMENT:
                sample.setSpace(space);
                sample.setExperiment(experiment);
                return sample;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected SamplePE createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return getSample(spacePE, projectPE, (SampleKind) param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<SamplePE> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testSamplePEPredicate(sessionProvider, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Override
    protected void assertWithNullForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
