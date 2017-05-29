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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.experiment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.CommonCollectionPredicateSystemTest;

/**
 * @author pkupczyk
 */
public class ExperimentListPredicateSystemTest extends CommonCollectionPredicateSystemTest<Experiment>
{

    @Autowired
    private ExperimentPredicateTestService service;

    @Override
    protected Experiment createNonexistentObject()
    {
        ExperimentInitializer initializer = new Experiment.ExperimentInitializer();
        initializer.setId(-1L);
        initializer.setPermId("IDONTEXIST");
        initializer.setCode("IDONTEXIST");
        initializer.setExperimentTypeCode("IDONTEXIST");
        initializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        initializer.setIdentifier("/IDONTEXIST/IDONTEXIST/" + initializer.getCode());
        return new Experiment(initializer);
    }

    @Override
    protected Experiment createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        ExperimentInitializer initializer = new Experiment.ExperimentInitializer();
        initializer.setId(ExperimentDB.getId(spacePE, projectPE));
        initializer.setPermId(ExperimentDB.getPermId(spacePE, projectPE));
        initializer.setCode(ExperimentDB.getCode(spacePE, projectPE));
        initializer.setExperimentTypeCode(ExperimentDB.getTypeCode(spacePE, projectPE));
        initializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        initializer.setIdentifier(ExperimentDB.getIdentifier(spacePE, projectPE));
        return new Experiment(initializer);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<Experiment> objects)
    {
        service.testExperimentListPredicate(sessionProvider, objects);
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t)
    {
        assertException(t, UserFailureException.class, "No experiment specified.");
    }

}