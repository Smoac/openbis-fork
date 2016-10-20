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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;

/**
 * @author pkupczyk
 */
public class SearchSpaceTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_SPACE_USER, new SpaceSearchCriteria(), "TEST-SPACE");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withId().thatEquals(new SpacePermId("CISD"));
        testSearch(TEST_USER, criteria, "CISD");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withId().thatEquals(new SpacePermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithPermIdThatContains()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatContains("ST-SPA");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithPermIdThatStartsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatStartsWith("CI");
        testSearch(TEST_USER, criteria, "CISD");
    }

    @Test
    public void testSearchWithPermIdThatEndsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatEndsWith("ACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatEquals("test-SPACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatContains("ST-sPa");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatStartsWith("ci");
        testSearch(TEST_USER, criteria, "CISD");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatEndsWith("ace");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("SPACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("CISD");
        criteria.withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "CISD", "TEST-SPACE");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatEquals("CISD");
        testSearch(TEST_USER, criteria, "CISD");
        testSearch(TEST_SPACE_USER, criteria);
    }

    private void testSearch(String user, SpaceSearchCriteria criteria, String... expectedCodes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Space> searchResult =
                v3api.searchSpaces(sessionToken, criteria, new SpaceFetchOptions());
        List<Space> spaces = searchResult.getObjects();

        assertSpaceCodes(spaces, expectedCodes);
        v3api.logout(sessionToken);
    }

}
