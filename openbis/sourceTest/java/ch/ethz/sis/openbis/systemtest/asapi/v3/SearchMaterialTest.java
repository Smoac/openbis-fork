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

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class SearchMaterialTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new MaterialSearchCriteria(), 3734);
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        MaterialPermId permId = new MaterialPermId("VIRUS1", "VIRUS");
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withId().thatEquals(permId);
        testSearch(TEST_USER, criteria, permId);
    }

    @Test
    public void testSearchWithPermId()
    {
        final MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withPermId().thatEquals("NOT SUPPORTED YET");
        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testSearch(TEST_USER, criteria);
                }
            }, "Please use criteria.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
    }

    @Test
    public void testSearchWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withCode().thatStartsWith("VIRUS");
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"), new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("BACTERIUM"));
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withCode().thatEquals("BACTERIUM");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withPermId().thatEquals("BACTERIUM");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("denovirus");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("adenoviru");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("denovirus");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("adenoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("denovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("denoviru");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION");
        testSearch(TEST_USER, criteria, 40);
    }

    @Test
    public void testSearchWithDateProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2007-07-17");
        testSearch(TEST_USER, criteria, new MaterialPermId("NEUTRAL", "CONTROL"), new MaterialPermId("C-NO-SEC", "CONTROL"), new MaterialPermId(
                "INHIBITOR", "CONTROL"));
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAnyProperty().thatEquals("HUHU");
        testSearch(TEST_USER, criteria, new MaterialPermId("MYGENE1", "GENE"));

        criteria = new MaterialSearchCriteria();
        criteria.withAnyProperty().thatEquals("HUH");
        testSearch(TEST_USER, criteria, 0);
    }

    public void testSearchWithAnyField()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAnyField().thatEquals("MYGENE2");
        testSearch(TEST_USER, criteria, new MaterialPermId("MYGENE2", "GENE"));
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithModificationDate()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModificationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithSortingByPropertyWithFloatValues()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("GFP", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("SCRAM", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("XXXXX-ALL", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("X-NO-DESC", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("X-NO-SIZE", "CONTROL"));

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withProperties();

        fo.sortBy().property("VOLUME").asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();

        assertEquals(materials1.get(0).getProperty("VOLUME"), "2.2");
        assertEquals(materials1.get(1).getProperty("VOLUME"), "3.0");
        assertEquals(materials1.get(2).getProperty("VOLUME"), "22.22");
        assertEquals(materials1.get(3).getProperty("VOLUME"), "99.99");
        assertEquals(materials1.get(4).getProperty("VOLUME"), "123");

        fo.sortBy().property("VOLUME").desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();

        assertEquals(materials2.get(0).getProperty("VOLUME"), "123");
        assertEquals(materials2.get(1).getProperty("VOLUME"), "99.99");
        assertEquals(materials2.get(2).getProperty("VOLUME"), "22.22");
        assertEquals(materials2.get(3).getProperty("VOLUME"), "3.0");
        assertEquals(materials2.get(4).getProperty("VOLUME"), "2.2");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("SRM");
        criteria.withCode().thatContains("1A");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithOrOperator()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatEquals("SRM_1");
        criteria.withCode().thatEquals("SRM_1A");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("FLU", "VIRUS"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE1", "GENE"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE2", "GENE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fo = new MaterialFetchOptions();

        fo.sortBy().code().asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials1, new MaterialPermId("FLU", "VIRUS"), new MaterialPermId("MYGENE1", "GENE"),
                new MaterialPermId("MYGENE2", "GENE"));

        fo.sortBy().code().desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials2, new MaterialPermId("MYGENE2", "GENE"), new MaterialPermId("MYGENE1", "GENE"),
                new MaterialPermId("FLU", "VIRUS"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("FLU", "VIRUS"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE1", "GENE"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE2", "GENE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials1, new MaterialPermId("MYGENE1", "GENE"), new MaterialPermId("MYGENE2", "GENE"),
                new MaterialPermId("FLU", "VIRUS"));

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials2, new MaterialPermId("FLU", "VIRUS"), new MaterialPermId("MYGENE2", "GENE"),
                new MaterialPermId("MYGENE1", "GENE"));

        v3api.logout(sessionToken);
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, MaterialPermId... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Material> searchResult =
                v3api.searchMaterials(sessionToken, criteria, new MaterialFetchOptions());
        List<Material> materials = searchResult.getObjects();

        assertMaterialPermIds(materials, expectedPermIds);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Material> searchResult =
                v3api.searchMaterials(sessionToken, criteria, new MaterialFetchOptions());
        List<Material> materials = searchResult.getObjects();

        assertEquals(materials.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}
