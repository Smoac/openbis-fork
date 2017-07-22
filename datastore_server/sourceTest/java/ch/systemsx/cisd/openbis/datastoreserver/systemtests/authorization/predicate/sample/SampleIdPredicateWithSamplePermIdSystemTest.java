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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SamplePermIdUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SampleIdPredicateWithSamplePermIdSystemTest extends SampleIdPredicateSystemTest
{

    @Override
    protected ISampleId createNonexistentObject(Object param)
    {
        PermId permId = SamplePermIdUtil.createNonexistentObject(param);
        return new SamplePermIdId(permId.getId());
    }

    @Override
    protected ISampleId createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        PermId permId = SamplePermIdUtil.createObject(this, spacePE, projectPE, param);
        return new SamplePermIdId(permId.getId());
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        SamplePermIdUtil.assertWithNonexistentObjectForInstanceUser(person, t, param);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        SamplePermIdUtil.assertWithNonexistentObjectForProjectUser(person, t, param);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        SamplePermIdUtil.assertWithNonexistentObjectForSpaceUser(person, t, param);
    }

}
