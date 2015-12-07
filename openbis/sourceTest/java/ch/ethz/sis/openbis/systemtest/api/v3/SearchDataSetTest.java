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

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.tag.id.TagPermId;

/**
 * @author pkupczyk
 */
public class SearchDataSetTest extends AbstractDataSetTest
{
    @Test
    public void testSearchWithIdSetToPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withId().thatEquals(new DataSetPermId("20081105092259000-18"));
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchTwoDataSetsWithCodeAndId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatEquals("20081105092259000-18");
        criteria.withCode().thatEquals("20081105092259000-19");
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-19");
    }

    @Test
    public void testSearchWithProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withProperty("COMMENT").thatContains("non-virt");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithRegistrationDateIsEarlierThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05 09:22:00");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithModicationDateIsLaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModificationDate().thatIsLaterThanOrEqualTo("2011-05-01");
        criteria.withContainer().withCode().thatContains("2");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "COMPONENT_2A", "20110509092359990-12");
    }

    @Test
    public void testSearchWithContainer()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer().withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithChildren()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3");
    }

    @Test
    public void testSearchWithChildrenWithPropertyEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withProperty("GENDER").thatEquals("FEMALE");
        criteria.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithParent()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents().withCode().thatEquals("20081105092159111-1");
        testSearch(TEST_USER, criteria, "20081105092259000-9");
    }

    @Test
    public void testSearchWithExperiment()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withExperiment();
        testSearch(TEST_USER, criteria, "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withoutExperiment();
        testSearch(TEST_USER, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithExperimentWithPermIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withPermId().thatEquals("200902091255058-1035");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithExperimentWithProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withProperty("GENDER");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17", "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentWithPropertyThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withProperty("GENDER").thatEquals("MALE");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentYoungerThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09 12:11:00");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withSample();
        testSearch(TEST_USER, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithoutSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withoutSample();
        testSearch(TEST_USER, criteria, "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithSampleWithPropertiesThatContains()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        SampleSearchCriteria sampleSearchCriteria = criteria.withSample().withOrOperator();
        sampleSearchCriteria.withProperty("BACTERIUM").thatContains("M-X");
        sampleSearchCriteria.withProperty("ORGANISM").thatContains("LY");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("LINK_TYPE"));
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withCode().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withPermId().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyProperty().thatEquals("non-virtual");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithAnyField()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyField().thatEquals("20110509092359990-11");
        testSearch(TEST_USER, criteria, "20110509092359990-11");
    }

    @Test
    public void testSearchWithTag()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "20120619092259000-22");
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModificationDate().thatEquals("2011-05-09");
        testSearch(TEST_USER, criteria, 14);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("VALIDATIONS");
        criteria.withCode().thatContains("PARENT");
        testSearch(TEST_USER, criteria, "VALIDATIONS_PARENT-28");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20081105092159111-1");
        criteria.withPermId().thatEquals("20081105092159222-2");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criteria, 1);

        criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    private void testSearch(String user, DataSetSearchCriteria criteria, String... expectedIdentifiers)
    {
        List<DataSet> dataSets = searchDataSets(user, criteria, new DataSetFetchOptions());

        assertIdentifiers(dataSets, expectedIdentifiers);
    }

    private void testSearch(String user, DataSetSearchCriteria criteria, int expectedCount)
    {
        List<DataSet> dataSets = searchDataSets(user, criteria, new DataSetFetchOptions());
        assertEquals(dataSets.size(), expectedCount);
    }

    private List<DataSet> searchDataSets(String user, DataSetSearchCriteria criteria, DataSetFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<DataSet> searchResult = v3api.searchDataSets(sessionToken, criteria, fetchOptions);
        List<DataSet> dataSets = searchResult.getObjects();
        v3api.logout(sessionToken);
        return dataSets;
    }

}
