/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author pkupczyk
 */
public class UpdateSamplesImportTest extends ObjectsImportTest
{

    @Test(dataProvider = FALSE_TRUE_PROVIDER)
    public void testUpdate(boolean async) throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String sampleCode = "TEST-IMPORT-" + UUID.randomUUID().toString();
        SampleIdentifier sampleIdentifier = new SampleIdentifier("/TEST-SPACE/" + sampleCode);

        SampleCreation creation = new SampleCreation();
        creation.setCode(sampleCode);
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setProperty("COMMENT", "initial comment");

        Sample sample = getObject(sessionToken, sampleIdentifier);
        assertNull(sample);

        as.createSamples(sessionToken, Arrays.asList(creation));

        sample = getObject(sessionToken, sampleIdentifier);
        assertEquals("initial comment", sample.getProperty("COMMENT"));

        ImportFile file = new ImportFile("identifier", "COMMENT");
        file.addLine(sampleIdentifier.getIdentifier(), "imported comment");
        uploadFiles(sessionToken, TEST_UPLOAD_KEY, file.toString());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(PARAM_UPLOAD_KEY, TEST_UPLOAD_KEY);
        parameters.put(PARAM_TYPE_CODE, "CELL_PLATE");
        parameters.put(PARAM_UPDATE_EXISTING, false);
        parameters.put(PARAM_ASYNC, async);

        if (async)
        {
            parameters.put(PARAM_USER_EMAIL, TEST_EMAIL);
        }

        long timestamp = System.currentTimeMillis();
        String message = executeImport(sessionToken, "updateSamples", parameters);

        sample = getObject(sessionToken, sampleIdentifier, timestamp, DEFAULT_TIMEOUT);
        assertEquals("imported comment", sample.getProperty("COMMENT"));

        if (async)
        {
            assertEquals("When the import is complete the confirmation or failure report will be sent by email.", message);
            assertEmail(timestamp, TEST_EMAIL, "Sample Batch Update successfully performed");
        } else
        {
            assertEquals("Update of 1 sample(s) is complete.", message);
            assertNoEmails(timestamp);
        }
    }

}
