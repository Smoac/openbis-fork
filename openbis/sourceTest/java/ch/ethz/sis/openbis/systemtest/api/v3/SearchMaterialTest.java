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

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriterion;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class SearchMaterialTest extends AbstractTest
{

    @Test
    public void testSearchWithIdSetToPermId()
    {
        MaterialPermId permId = new MaterialPermId("VIRUS1", "VIRUS");
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withId().thatEquals(permId);
        testSearch(TEST_USER, criterion, permId);
    }

    @Test
    public void testSearchWithPermId()
    {
        final MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withPermId().thatEquals("NOT SUPPORTED YET");
        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testSearch(TEST_USER, criterion);
                }
            }, "Please use criterion.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
    }

    @Test
    public void testSearchWithCode()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withCode().thatStartsWith("VIRUS");
        testSearch(TEST_USER, criterion, new MaterialPermId("VIRUS1", "VIRUS"), new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withType().withId().thatEquals(new EntityTypePermId("BACTERIUM"));
        testSearch(TEST_USER, criterion, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withType().withCode().thatEquals("BACTERIUM");
        testSearch(TEST_USER, criterion, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withType().withPermId().thatEquals("BACTERIUM");
        testSearch(TEST_USER, criterion, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("adenovirus");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("adenoviru");
        testSearch(TEST_USER, criterion, 0);

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("denoviru");
        testSearch(TEST_USER, criterion, 0);

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("denovirus");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("adenovirus");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("adenoviru");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("denoviru");
        testSearch(TEST_USER, criterion, 0);

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("denovirus");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("adenovirus");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("adenoviru");
        testSearch(TEST_USER, criterion, 0);

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("denoviru");
        testSearch(TEST_USER, criterion, 0);

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("denovirus");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatContains("adenovirus");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatContains("denoviru");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithProperty()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withProperty("DESCRIPTION");
        testSearch(TEST_USER, criterion, 40);
    }

    @Test
    public void testSearchWithDateProperty()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2007-07-17");
        testSearch(TEST_USER, criterion, new MaterialPermId("NEUTRAL", "CONTROL"), new MaterialPermId("C-NO-SEC", "CONTROL"), new MaterialPermId(
                "INHIBITOR", "CONTROL"));
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withAnyProperty().thatEquals("HUHU");
        testSearch(TEST_USER, criterion, new MaterialPermId("MYGENE1", "GENE"));

        criterion = new MaterialSearchCriterion();
        criterion.withAnyProperty().thatEquals("HUH");
        testSearch(TEST_USER, criterion, 0);
    }

    public void testSearchWithAnyField()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withAnyField().thatEquals("MYGENE2");
        testSearch(TEST_USER, criterion, new MaterialPermId("MYGENE2", "GENE"));
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criterion, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withRegistrationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criterion, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithModificationDate()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withModificationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criterion, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithAndOperator()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withAndOperator();
        criterion.withCode().thatContains("SRM");
        criterion.withCode().thatContains("1A");
        testSearch(TEST_USER, criterion, new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithOrOperator()
    {
        MaterialSearchCriterion criterion = new MaterialSearchCriterion();
        criterion.withOrOperator();
        criterion.withCode().thatEquals("SRM_1");
        criterion.withCode().thatEquals("SRM_1A");
        testSearch(TEST_USER, criterion, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    private void testSearch(String user, MaterialSearchCriterion criterion, MaterialPermId... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Material> materials =
                v3api.searchMaterials(sessionToken, criterion, new MaterialFetchOptions());

        assertMaterialPermIds(materials, expectedPermIds);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, MaterialSearchCriterion criterion, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Material> materials =
                v3api.searchMaterials(sessionToken, criterion, new MaterialFetchOptions());

        assertEquals(materials.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}
