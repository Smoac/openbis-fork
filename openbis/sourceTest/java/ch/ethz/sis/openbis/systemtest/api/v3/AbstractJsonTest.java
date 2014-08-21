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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static junit.framework.Assert.assertEquals;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;
import ch.systemsx.cisd.openbis.systemtest.api.common.json.JsonApiClient;

/**
 * @author pkupczyk
 */
public class AbstractJsonTest extends RemoteApiTestCase
{

    private static final String API_URL = TestInstanceHostUtils.getOpenBISUrl() + "/openbis/" + IApplicationServerApi.JSON_SERVICE_URL;

    private TestResources resources = new TestResources(getClass());

    private JsonApiClient api = new JsonApiClient(API_URL);

    public String login() throws Exception
    {
        JsonNode node = sendRequest("login.json");
        return node.get("result").asText();
    }

    protected JsonNode sendRequest(String fileName, Object... params) throws Exception
    {
        return api.sendRequest(getFileContent(fileName), params);
    }

    protected String getFileContent(String fileName)
    {
        return FileUtilities.loadToString(resources.getResourceFile(fileName));
    }

    protected static void assertResultCount(JsonNode response, int expectedCount)
    {
        assertChildrenCount(response.get("result"), expectedCount);
    }

    protected static void assertChildrenCount(JsonNode node, int expectedCount)
    {
        int actualCount = 0;

        Iterator<JsonNode> iterator = node.iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            actualCount++;
        }

        assertEquals(expectedCount, actualCount);
    }

}
