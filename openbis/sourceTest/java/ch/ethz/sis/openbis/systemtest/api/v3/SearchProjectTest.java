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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriterion;

/**
 * @author pkupczyk
 */
public class SearchProjectTest extends AbstractTest
{

    @Test
    public void testSearchWithIdSetToPermId()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withId().thatEquals(new ProjectPermId("20120814110011738-105"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withId().thatEquals(new ProjectPermId("IDONTEXIST"));
        testSearch(TEST_USER, criterion);
    }

    @Test
    public void testSearchWithIdSetToNonexistentIdentifier()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withId().thatEquals(new ProjectIdentifier("/IDONT/EXIST"));
        testSearch(TEST_USER, criterion);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withPermId().thatEquals("20120814110011738-105");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withCode().thatEquals("test-PROJECT");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withCode().thatContains("pRoJ");
        testSearch(TEST_USER, criterion, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withCode().thatStartsWith("n");
        testSearch(TEST_USER, criterion, "/CISD/NEMO", "/CISD/NOE", "/TEST-SPACE/NOE");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withCode().thatEndsWith("t");
        testSearch(TEST_USER, criterion, "/CISD/DEFAULT", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithSpaceWithIdThatEquals()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withSpace().withId().thatEquals(new SpacePermId("CISD"));
        testSearch(TEST_USER, criterion, "/CISD/DEFAULT", "/CISD/NEMO", "/CISD/NOE");
    }

    @Test
    public void testSearchWithSpaceWithCodeThatStartsWith()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withSpace().withCode().thatStartsWith("TEST");
        testSearch(TEST_USER, criterion, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/NOE", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withAndOperator();
        criterion.withCode().thatContains("TEST");
        criterion.withCode().thatContains("PROJECT");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withOrOperator();
        criterion.withPermId().thatEquals("20120814110011738-101");
        criterion.withPermId().thatEquals("20120814110011738-105");
        testSearch(TEST_USER, criterion, "/CISD/DEFAULT", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithProjectUnauthorized()
    {
        ProjectSearchCriterion criterion = new ProjectSearchCriterion();
        criterion.withId().thatEquals(new ProjectIdentifier("/CISD/DEFAULT"));
        testSearch(TEST_USER, criterion, "/CISD/DEFAULT");
        testSearch(TEST_SPACE_USER, criterion);
    }

    private void testSearch(String user, ProjectSearchCriterion criterion, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Project> projects = v3api.searchProjects(sessionToken, criterion, new ProjectFetchOptions());

        assertProjectIdentifiers(projects, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

}
