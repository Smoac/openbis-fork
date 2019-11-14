/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

/**
 * @author Franz-Josef Elmer
 */
public class GetRightsTest extends AbstractTest
{
    @Test
    public void testGetSampleRights()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");
        IObjectId s2 = new SampleIdentifier("/CISD/CP-TEST-1");
        IObjectId s3 = new SampleIdentifier("/CISD/NEW");
        IObjectId s4 = new SampleIdentifier("/CISD/NEMO/NEW");
        IObjectId s5 = new SampleIdentifier("/TEST-SPACE/NOE/NEW");
        IObjectId s6 = new CreationId("123-45");

        // When
        Map<IObjectId, Rights> map = v3api.getRights(sessionToken, Arrays.asList(s1, s2, s3, s4, s5, s6), new RightsFetchOptions());

        // Then
        assertEquals(map.get(s1).getRights().toString(), "[]");
        assertEquals(map.get(s2).getRights().toString(), "[UPDATE]");
        assertEquals(map.get(s3).getRights().toString(), "[CREATE]");
        assertEquals(map.get(s4).getRights().toString(), "[CREATE]");
        assertEquals(map.get(s5).getRights().toString(), "[]");
        assertEquals(map.size(), 5);
    }

    @Test
    public void testGetSampleCreationRightForUnknownSamplePermId()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new SamplePermId("123-45");

        // When
        assertUserFailureException(Void -> v3api.getRights(sessionToken, Arrays.asList(s1), new RightsFetchOptions()),
                // Then
                "Unknown sample with perm id 123-45.");
    }

    @Test
    public void testGetSampleCreationRightInMissingSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new SampleIdentifier("/NO-SPACE/NEW");

        // When
        assertUserFailureException(Void -> v3api.getRights(sessionToken, Arrays.asList(s1), new RightsFetchOptions()),
                // Then
                "Unknown space in sample identifier '/NO-SPACE/NEW'.");
    }

    @Test
    public void testGetSampleCreationRightInMissingProject()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new SampleIdentifier("/TEST-SPACE/NO-PROJECT/NEW");

        // When
        assertUserFailureException(Void -> v3api.getRights(sessionToken, Arrays.asList(s1), new RightsFetchOptions()),
                // Then
                "Unknown project in sample identifier '/TEST-SPACE/NO-PROJECT/NEW'.");
    }

    @Test
    public void testGetExperimentRights()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        IObjectId s2 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        IObjectId s3 = new ExperimentIdentifier("/CISD/NEMO/NEW");
        IObjectId s4 = new ExperimentIdentifier("/TEST-SPACE/NOE/NEW");

        // When
        Map<IObjectId, Rights> map = v3api.getRights(sessionToken, Arrays.asList(s1, s2, s3, s4), new RightsFetchOptions());

        // Then
        assertEquals(map.get(s1).getRights().toString(), "[]");
        assertEquals(map.get(s2).getRights().toString(), "[UPDATE]");
        assertEquals(map.get(s3).getRights().toString(), "[CREATE]");
        assertEquals(map.get(s4).getRights().toString(), "[]");
    }

    @Test
    public void testGetExperimentCreationRightForUnknownExperimentPermId()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new ExperimentPermId("123-45");

        // When
        assertUserFailureException(Void -> v3api.getRights(sessionToken, Arrays.asList(s1), new RightsFetchOptions()),
                // Then
                "Unknown experiment with perm id 123-45.");
    }

    @Test
    public void testGetExperimentCreationRightInMissingProject()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new ExperimentIdentifier("/TEST-SPACE/NO-PROJECT/NEW");

        // When
        assertUserFailureException(Void -> v3api.getRights(sessionToken, Arrays.asList(s1), new RightsFetchOptions()),
                // Then
                "Unknown project in experiment identifier '/TEST-SPACE/NO-PROJECT/NEW'.");
    }

    @Test
    public void testGetDataSetRights()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new DataSetPermId("20120619092259000-22");
        IObjectId s2 = new DataSetPermId("20081105092259000-21");

        // When
        Map<IObjectId, Rights> map = v3api.getRights(sessionToken, Arrays.asList(s1, s2), new RightsFetchOptions());

        // Then
        assertEquals(map.get(s1).getRights().toString(), "[]");
        assertEquals(map.get(s2).getRights().toString(), "[UPDATE]");
    }
}
