/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class DeleteSampleTest extends AbstractSampleTest
{

    @Test
    public void testDeleteSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId permId = createSampleToDelete();

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(permId), options);
        Assert.assertNotNull(deletionId);

        List<Sample> samples = v3api.listSamples(sessionToken, Collections.singletonList(permId), new SampleFetchOptions());
        Assert.assertEquals(0, samples.size());
    }

    @Test
    public void testDeleteSampleWithUnauthorizedSample()
    {
        final SamplePermId permId = createSampleToDelete();

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

                    SampleDeletionOptions options = new SampleDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteSamples(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    private SamplePermId createSampleToDelete()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_TO_DELETE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        List<Sample> samples = v3api.listSamples(sessionToken, permIds, new SampleFetchOptions());

        Assert.assertEquals(1, samples.size());
        Assert.assertEquals("SAMPLE_TO_DELETE", samples.get(0).getCode());

        return permIds.get(0);
    }

}
