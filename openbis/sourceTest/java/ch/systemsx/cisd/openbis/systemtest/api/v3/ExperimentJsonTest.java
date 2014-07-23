/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.api.v3;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author pkupczyk
 */
@Test(enabled = false)
public class ExperimentJsonTest extends AbstractJsonTest
{

    @Test(enabled = false)
    public void testListExperiments() throws Exception
    {
        String sessionToken = login();
        String fetchOptions = getFileContent("fetchOptions.json");

        JsonNode response = sendRequest("listExperiments.json", sessionToken, "200811050951882-1028", fetchOptions);

        assertResultCount(response, 1);

        JsonNode experimentNode = response.get("result").get(0);
        assertEquals("EXP1", experimentNode.get("code").asText());
        assertEquals("/CISD/NEMO/EXP1", experimentNode.get("identifier").get("identifier").asText());
        assertEquals("200811050951882-1028", experimentNode.get("permId").get("permId").asText());
        assertEquals("SIRNA_HCS", experimentNode.get("type").get("code").asText());
        assertEquals("NEMO", experimentNode.get("project").get("code").asText());
        assertEquals("A simple experiment", experimentNode.get("properties").get("DESCRIPTION").asText());
        assertEquals("test", experimentNode.get("registrator").get("userId").asText());
        JsonNode attachments = experimentNode.get("attachments");
        assertChildrenCount(attachments, 1);
        assertEquals("exampleExperiments.txt", attachments.get(0).get("fileName").asText());
        assertFalse(experimentNode.get("registrationDate").isNull());
        assertFalse(experimentNode.get("modificationDate").isNull());
    }

    @Test(enabled = false)
    public void testCreateExperiments() throws Exception
    {
        String sessionToken = login();

        JsonNode createResponse = sendRequest("createExperiments.json", sessionToken, "NEW_JSON_EXPERIMENT");

        assertResultCount(createResponse, 1);

        String experimentPermId = createResponse.get("result").get(0).get("permId").asText();
        String fetchOptions = getFileContent("fetchOptions.json");

        JsonNode listResponse = sendRequest("listExperiments.json", sessionToken, experimentPermId, fetchOptions);

        assertResultCount(listResponse, 1);

        JsonNode experimentNode = listResponse.get("result").get(0);
        assertEquals(experimentPermId, experimentNode.get("permId").get("permId").asText());
        assertEquals("NEW_JSON_EXPERIMENT", experimentNode.get("code").asText());
        assertEquals("/CISD/NEMO/NEW_JSON_EXPERIMENT", experimentNode.get("identifier").get("identifier").asText());
        assertEquals("COMPOUND_HCS", experimentNode.get("type").get("code").asText());
        assertEquals("NEMO", experimentNode.get("project").get("code").asText());
        assertEquals("hello", experimentNode.get("properties").get("DESCRIPTION").asText());
        JsonNode tagsNode = experimentNode.get("tags");
        assertChildrenCount(tagsNode, 1);
        assertEquals("NEW_JSON_TAG", tagsNode.get(0).get("name").asText());
        assertEquals("test", experimentNode.get("registrator").get("userId").asText());
        assertFalse(experimentNode.get("modifier").isNull());
        assertFalse(experimentNode.get("registrationDate").isNull());
        assertFalse(experimentNode.get("modificationDate").isNull());
    }

    @Test(enabled = false)
    public void testUpdateExperiments() throws Exception
    {
        // updateExperiments method is not yet implemented in the new API
    }

    @Test(enabled = false)
    public void testSearchExperiments() throws Exception
    {
        String sessionToken = login();
        String fetchOptions = getFileContent("fetchOptions.json");

        JsonNode response = sendRequest("searchExperiments.json", sessionToken, "EXP1", fetchOptions);

        assertResultCount(response, 1);

        JsonNode experimentNode = response.get("result").get(0);
        assertEquals("EXP1", experimentNode.get("code").asText());
        assertEquals("/CISD/NEMO/EXP1", experimentNode.get("identifier").get("identifier").asText());
        assertEquals("200811050951882-1028", experimentNode.get("permId").get("permId").asText());
        assertEquals("SIRNA_HCS", experimentNode.get("type").get("code").asText());
        assertEquals("NEMO", experimentNode.get("project").get("code").asText());
        assertEquals("A simple experiment", experimentNode.get("properties").get("DESCRIPTION").asText());
        assertEquals("test_role", experimentNode.get("modifier").get("userId").asText());
    }

}
