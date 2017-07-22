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

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author pkupczyk
 */
public class SampleUpdatesPredicateWithExperimentIdentifierSystemTest extends SampleUpdatesPredicateSystemTest
{

    @Override
    protected SampleUpdatesDTO createNonexistentObject(Object param)
    {
        ExperimentIdentifier identifier = new ExperimentIdentifier("IDONTEXIST", "IDONTEXIST", "IDONTEXIST");
        return new SampleUpdatesDTO(null, null, identifier, null, null, 0, null, null, null);
    }

    @Override
    protected SampleUpdatesDTO createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExperimentPE experimentPE = getExperiment(spacePE, projectPE);
        ExperimentIdentifier identifier = new ExperimentIdentifier(spacePE.getCode(), projectPE.getCode(), experimentPE.getCode());
        return new SampleUpdatesDTO(null, null, identifier, null, null, 0, null, null, null);
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
