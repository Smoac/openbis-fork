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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.dataset;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonDataSetPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class DataPEPredicateSystemTest extends CommonDataSetPredicateSystemTest<DataPE>
{

    @Override
    protected DataPE createNonexistentObject(Object param)
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

        DataPE dataSet = new DataPE();
        dataSet.setCode("IDONTEXIST");

        switch ((DataSetKind) param)
        {
            case EXPERIMENT:
                dataSet.setExperiment(experiment);
                return dataSet;
            case SPACE_SAMPLE:
                sample.setSpace(space);
                dataSet.setSample(sample);
                return dataSet;
            case PROJECT_SAMPLE:
                sample.setProject(project);
                dataSet.setSample(sample);
                return dataSet;
            case EXPERIMENT_SAMPLE:
                sample.setExperiment(experiment);
                dataSet.setSample(sample);
                return dataSet;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected DataPE createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return getDataSet(spacePE, projectPE, (DataSetKind) param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<DataPE> objects, Object param)
    {
        getBean(DataSetPredicateTestService.class).testDataPEPredicate(sessionProvider, objects.get(0));
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

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
