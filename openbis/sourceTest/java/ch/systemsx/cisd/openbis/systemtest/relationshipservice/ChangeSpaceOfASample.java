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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

public class ChangeSpaceOfASample extends RelationshipServiceTest
{
    @Test
    public void assignSpaceSampleToAnotherSpace()
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Sample sample = create(aSample().inSpace(sourceSpace));

        SampleUpdatesDTO updates =
                create(aSampleUpdate(sample).inSpace(destinationSpace));

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));

    }

    @Test
    public void updateSpaceSampletoInstanceSample()
    {
        Space space = create(aSpace());
        Sample sample = create(aSample().inSpace(space));

        SampleUpdatesDTO updates = create(aSampleUpdate(sample).withoutSpace());

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getSpace(), is(nullValue()));
    }

    @Test
    public void updateInstanceSampleToSpaceSample()
    {
        Sample sample = create(aSample());
        Space space = create(aSpace());
        SampleUpdatesDTO updates = create(aSampleUpdate(sample).inSpace(space));

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(space)));

    }
}
