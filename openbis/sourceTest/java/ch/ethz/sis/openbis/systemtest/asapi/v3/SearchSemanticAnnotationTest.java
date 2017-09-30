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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;

/**
 * @author pkupczyk
 */
public class SearchSemanticAnnotationTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new SemanticAnnotationSearchCriteria(), "20170918092158673-1", "20170918092158673-2", "20170918092158673-3",
                "20170918092158673-4", "20170918092158673-5", "20170918092158673-6");
    }

    @Test
    public void testSearchWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withId().thatEquals(new SemanticAnnotationPermId("20170918092158673-2"));
        testSearch(TEST_USER, criteria, "20170918092158673-2");
    }

    @Test
    public void testSearchWithIdNonexistent()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withId().thatEquals(new SemanticAnnotationPermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPermId().thatEquals("20170918092158673-2");
        testSearch(TEST_USER, criteria, "20170918092158673-2");
    }

    @Test
    public void testSearchWithEntityTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withId().thatEquals(new EntityTypePermId("DILUTION_PLATE", EntityKind.SAMPLE));
        testSearch(TEST_USER, criteria, "20170918092158673-2");
    }

    @Test
    public void testSearchWithEntityTypeWithCodeThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withCode().thatEquals("MASTER_PLATE");
        testSearch(TEST_USER, criteria, "20170918092158673-1");
    }

    @Test
    public void testSearchWithEntityTypeWithKindThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withKind().thatEquals(EntityKind.SAMPLE);
        testSearch(TEST_USER, criteria, "20170918092158673-1", "20170918092158673-2");
        criteria.withEntityType().withKind().thatEquals(EntityKind.EXPERIMENT);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithEntityTypeWithCodeThatEqualsAndWithKindThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withCode().thatEquals("MASTER_PLATE");
        criteria.withEntityType().withKind().thatEquals(EntityKind.SAMPLE);
        testSearch(TEST_USER, criteria, "20170918092158673-1");
    }

    @Test
    public void testSearchWithPropertyTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyType().withId().thatEquals(new PropertyTypePermId("GENE_SYMBOL"));
        testSearch(TEST_USER, criteria, "20170918092158673-6");
    }

    @Test
    public void testSearchWithPropertyTypeWithCodeThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyType().withCode().thatEquals("DESCRIPTION");
        testSearch(TEST_USER, criteria, "20170918092158673-5");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withId()
                .thatEquals(new PropertyAssignmentPermId(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE),
                        new PropertyTypePermId("$PLATE_GEOMETRY")));
        testSearch(TEST_USER, criteria, "20170918092158673-4");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithEntityTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withEntityType().withId().thatEquals(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE));
        testSearch(TEST_USER, criteria, "20170918092158673-4");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithPropertyTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withPropertyType().withId().thatEquals(new PropertyTypePermId("$PLATE_GEOMETRY"));
        testSearch(TEST_USER, criteria, "20170918092158673-3", "20170918092158673-4");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithEntityTypeWithIdThatEqualsAndWithPropertyTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withEntityType().withId().thatEquals(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE));
        criteria.withPropertyAssignment().withPropertyType().withId().thatEquals(new PropertyTypePermId("$PLATE_GEOMETRY"));
        testSearch(TEST_USER, criteria, "20170918092158673-4");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withAndOperator();
        criteria.withPermId().thatContains("20170918092158673-");
        criteria.withPermId().thatContains("-2");
        testSearch(TEST_USER, criteria, "20170918092158673-2");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatContains("20170918092158673-");
        criteria.withPermId().thatContains("-2");
        testSearch(TEST_USER, criteria, "20170918092158673-1", "20170918092158673-2", "20170918092158673-3",
                "20170918092158673-4", "20170918092158673-5", "20170918092158673-6");
    }

    private void testSearch(String user, SemanticAnnotationSearchCriteria criteria, String... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<SemanticAnnotation> searchResult = v3api.searchSemanticAnnotations(sessionToken, criteria, new SemanticAnnotationFetchOptions());
        List<SemanticAnnotation> annotations = searchResult.getObjects();

        Set<String> actualSet = new HashSet<String>();
        for (SemanticAnnotation annotation : annotations)
        {
            actualSet.add(annotation.getPermId().getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedPermIds);

        v3api.logout(sessionToken);
    }

}
