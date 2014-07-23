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
import static junit.framework.Assert.assertTrue;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author pkupczyk
 */
@Test(enabled = false)
public class SampleJsonTest extends AbstractJsonTest
{

    @Test(enabled = false)
    public void testListSamples() throws Exception
    {
        String sessionToken = login();
        String fetchOptions = getFileContent("fetchOptions.json");

        JsonNode response = sendRequest("listSamples.json", sessionToken, "200811050919915-8", fetchOptions);

        assertResultCount(response, 1);

        JsonNode sampleNode = response.get("result").get(0);
        assertEquals("CL1", sampleNode.get("code").asText());
        assertEquals("CONTROL_LAYOUT", sampleNode.get("sampleType").get("code").asText());
        assertEquals("CISD", sampleNode.get("space").get("code").asText());
        assertTrue(sampleNode.get("experiment").isNull());
        assertTrue(sampleNode.get("container").isNull());
        assertEquals("384_WELLS_16X24", sampleNode.get("properties").get("$PLATE_GEOMETRY").asText());
        assertEquals("test control layout", sampleNode.get("properties").get("DESCRIPTION").asText());
    }

    @Test(enabled = false)
    public void testCreateSamples() throws Exception
    {
        String sessionToken = login();

        JsonNode createResponse = sendRequest("createSamples.json", sessionToken, "NEW_JSON_SAMPLE");

        assertResultCount(createResponse, 1);

        String samplePermId = createResponse.get("result").get(0).get("permId").asText();
        String fetchOptions = getFileContent("fetchOptions.json");

        JsonNode listResponse = sendRequest("listSamples.json", sessionToken, samplePermId, fetchOptions);

        assertResultCount(listResponse, 1);

        JsonNode sampleNode = listResponse.get("result").get(0);
        assertEquals(samplePermId, sampleNode.get("permId").get("permId").asText());
        assertEquals("NEW_JSON_SAMPLE", sampleNode.get("code").asText());
        assertEquals("CELL_PLATE", sampleNode.get("sampleType").get("code").asText());
        assertEquals("CISD", sampleNode.get("space").get("code").asText());
        assertEquals("EXP10", sampleNode.get("experiment").get("code").asText());
        assertEquals("hello", sampleNode.get("properties").get("COMMENT").asText());
    }

    @Test(enabled = false)
    public void testUpdateSamples() throws Exception
    {
        String sessionToken = login();

        JsonNode createResponse;

        createResponse = sendRequest("createSamples.json", sessionToken, "JSON_SAMPLE_TO_UPDATE_CONTAINED");
        assertResultCount(createResponse, 1);

        createResponse = sendRequest("createSamples.json", sessionToken, "JSON_SAMPLE_TO_UPDATE_CONTAINER");
        assertResultCount(createResponse, 1);

        createResponse = sendRequest("createSamples.json", sessionToken, "JSON_SAMPLE_TO_UPDATE");
        assertResultCount(createResponse, 1);

        String samplePermId = createResponse.get("result").get(0).get("permId").asText();
        String fetchOptions = getFileContent("fetchOptions.json");

        sendRequest("updateSamples.json", sessionToken, "/CISD/JSON_SAMPLE_TO_UPDATE");

        JsonNode listResponse = sendRequest("listSamples.json", sessionToken, samplePermId, fetchOptions);

        assertResultCount(listResponse, 1);

        JsonNode sampleNode = listResponse.get("result").get(0);
        assertEquals("JSON_SAMPLE_TO_UPDATE", sampleNode.get("code").asText());
        assertEquals("CELL_PLATE", sampleNode.get("sampleType").get("code").asText());
        assertEquals("CISD", sampleNode.get("space").get("code").asText());
        assertEquals("EXP11", sampleNode.get("experiment").get("code").asText());
        assertEquals("JSON_SAMPLE_TO_UPDATE_CONTAINER", sampleNode.get("container").get("code").asText());

        JsonNode containedNode = sampleNode.get("contained");
        assertTrue(containedNode.has(0));
        assertFalse(containedNode.has(1));
        assertEquals("JSON_SAMPLE_TO_UPDATE_CONTAINED", containedNode.get(0).get("code").asText());
        assertEquals("hello 2", sampleNode.get("properties").get("COMMENT").asText());
    }

    @Test(enabled = false)
    public void testSearchSamples() throws Exception
    {
        String sessionToken = login();
        String fetchOptions = getFileContent("fetchOptions.json");

        JsonNode response = sendRequest("searchSamples.json", sessionToken, "PLATE_WELLSEARCH", fetchOptions);

        assertResultCount(response, 1);

        JsonNode sampleNode = response.get("result").get(0);
        assertEquals("PLATE_WELLSEARCH", sampleNode.get("code").asText());
        assertEquals("CELL_PLATE", sampleNode.get("sampleType").get("code").asText());
        assertEquals("CISD", sampleNode.get("space").get("code").asText());
        assertEquals("EXP-WELLS", sampleNode.get("experiment").get("code").asText());
        assertTrue(sampleNode.get("container").isNull());
        assertEquals("{}", sampleNode.get("properties").toString());
    }

}
