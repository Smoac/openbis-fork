/*
 * Copyright 2016 ETH Zuerich, CISD
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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;

/**
 * @author Jakub Straszewski
 */
public class GlobalSearchTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        GlobalSearchCriteria searchCriteria = new GlobalSearchCriteria();
        searchCriteria.withText().thatContains("CISD");
        // v3api.searchGlobally(sessionToken, searchCriteria, new GlobalSearchObjectFetchOptions());

        v3api.logout(sessionToken);

    }

}
