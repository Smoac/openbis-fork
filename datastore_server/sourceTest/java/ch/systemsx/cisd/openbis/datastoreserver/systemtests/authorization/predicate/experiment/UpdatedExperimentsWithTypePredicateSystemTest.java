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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.experiment;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.experiment.ExperimentPredicateTestService;

/**
 * @author pkupczyk
 */
public class UpdatedExperimentsWithTypePredicateSystemTest extends CommonPredicateSystemTest<UpdatedBasicExperiment>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected UpdatedBasicExperiment createNonexistentObject(Object param)
    {
        UpdatedBasicExperiment experiment = new UpdatedBasicExperiment();
        experiment.setIdentifier("/IDONTEXIST/IDONTEXIST/IDONTEXIST");
        return experiment;
    }

    @Override
    protected UpdatedBasicExperiment createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExperimentPE experimentPE = getExperiment(spacePE, projectPE);
        UpdatedBasicExperiment experiment = new UpdatedBasicExperiment();
        experiment.setIdentifier(experimentPE.getIdentifier());
        return experiment;
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<UpdatedBasicExperiment> objects, Object param)
    {
        UpdatedExperimentsWithType experiments = new UpdatedExperimentsWithType();
        experiments.setUpdatedExperiments(objects);
        getBean(ExperimentPredicateTestService.class).testUpdatedExperimentsWithTypePredicate(sessionProvider, experiments);
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}