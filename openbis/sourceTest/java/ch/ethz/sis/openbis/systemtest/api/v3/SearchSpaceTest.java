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

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriterion;

/**
 * @author pkupczyk
 */
public class SearchSpaceTest extends AbstractTest
{

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withId().thatEquals(new SpacePermId("CISD"));
        testSearch(TEST_USER, criterion, "CISD");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withId().thatEquals(new SpacePermId("IDONTEXIST"));
        testSearch(TEST_USER, criterion);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithPermIdThatContains()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withPermId().thatContains("ST-SPA");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithPermIdThatStartsWith()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withPermId().thatStartsWith("C");
        testSearch(TEST_USER, criterion, "CISD");
    }

    @Test
    public void testSearchWithPermIdThatEndsWith()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withPermId().thatEndsWith("E");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withCode().thatEquals("test-SPACE");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withCode().thatContains("ST-sPa");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withCode().thatStartsWith("c");
        testSearch(TEST_USER, criterion, "CISD");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withCode().thatEndsWith("e");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withAndOperator();
        criterion.withCode().thatContains("TEST");
        criterion.withCode().thatContains("SPACE");
        testSearch(TEST_USER, criterion, "TEST-SPACE");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withOrOperator();
        criterion.withPermId().thatEquals("CISD");
        criterion.withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, "CISD", "TEST-SPACE");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SpaceSearchCriterion criterion = new SpaceSearchCriterion();
        criterion.withPermId().thatEquals("CISD");
        testSearch(TEST_USER, criterion, "CISD");
        testSearch(TEST_SPACE_USER, criterion);
    }

    private void testSearch(String user, SpaceSearchCriterion criterion, String... expectedCodes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Space> spaces =
                v3api.searchSpaces(sessionToken, criterion, new SpaceFetchOptions());

        assertSpaceCodes(spaces, expectedCodes);
        v3api.logout(sessionToken);
    }

}
