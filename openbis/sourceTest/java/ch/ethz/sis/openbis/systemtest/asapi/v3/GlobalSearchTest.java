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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import static org.testng.Assert.*;

/**
 * @author pkupczyk
 */
public class GlobalSearchTest extends AbstractTest
{

    @Test
    public void testSearchWithAuthorized()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("200902091219327-1025");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getObjects().size(), 1);

        GlobalSearchObject object = result.getObjects().get(0);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");
    }

    @Test
    public void testSearchWithUnauthorized()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("200902091219327-1025");

        SearchResult<GlobalSearchObject> result = search(TEST_SPACE_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithOneContainsOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertStuff(result);
    }

    @Test
    public void testSearchWithOneContainsMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple");
        criteria.withText().thatContains("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");
        criteria.withText().thatContains("stuff simple");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithOneContainsExactlyOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertStuff(result);
    }

    @Test
    public void testSearchWithOneContainsExactlyMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsExactlyOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple");
        criteria.withText().thatContainsExactly("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsExactlyMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");
        criteria.withText().thatContainsExactly("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleStuff(result);
    }

    @Test
    public void testSearchWithObjectKindsSpecified()
    {
        GlobalSearchObject object;

        // experiment
        object = searchAndAssertOneOrNone(TEST_USER, "200811050951882-1028", GlobalSearchObjectKind.EXPERIMENT);
        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028");

        object = searchAndAssertOneOrNone(TEST_USER, "200811050951882-1028", GlobalSearchObjectKind.SAMPLE);
        assertNull(object);

        // sample
        object = searchAndAssertOneOrNone(TEST_USER, "200902091219327-1025", GlobalSearchObjectKind.SAMPLE);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");

        object = searchAndAssertOneOrNone(TEST_USER, "200902091219327-1025", GlobalSearchObjectKind.DATA_SET);
        assertNull(object);

        // data set
        object = searchAndAssertOneOrNone(TEST_USER, "20081105092159111-1", GlobalSearchObjectKind.DATA_SET);
        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", DataSetKind.PHYSICAL);

        object = searchAndAssertOneOrNone(TEST_USER, "20081105092159111-1", GlobalSearchObjectKind.MATERIAL);
        assertNull(object);

        // material
        object = searchAndAssertOneOrNone(TEST_USER, "HSV1", GlobalSearchObjectKind.MATERIAL);
        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");

        object = searchAndAssertOneOrNone(TEST_USER, "HSV1", GlobalSearchObjectKind.EXPERIMENT);
        assertNull(object);
    }

    @Test
    public void testSearchWithObjectKindsNotSpecified()
    {
        GlobalSearchObject object = null;

        // experiment
        object = searchAndAssertOneOrNone(TEST_USER, "200811050951882-1028");
        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028");

        // sample
        object = searchAndAssertOneOrNone(TEST_USER, "200902091219327-1025");
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");

        // data set
        object = searchAndAssertOneOrNone(TEST_USER, "20081105092159111-1");
        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", DataSetKind.PHYSICAL);

        // material
        object = searchAndAssertOneOrNone(TEST_USER, "HSV1");
        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
    }

    @Test
    public void testSearchWithContainsAndWildCardsDisabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuf*");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithContainsExactlyAndWildCardsDisabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuf*");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithSortingByScoreAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().score();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getScore, true);
    }

    @Test
    public void testSearchWithSortingByScoreDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().score().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getScore, false);
    }

    @Test
    public void testSearchWithSortingByObjectKindAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectKind();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getObjectKind, true);
    }

    @Test
    public void testSearchWithSortingByObjectKindDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectKind().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getObjectKind, false);
    }

    @Test
    public void testSearchWithSortingByObjectPermIdAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectPermId().toString(), true);
    }

    @Test
    public void testSearchWithSortingByObjectPermIdDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectPermId().toString(), false);
    }

    @Test
    public void testSearchWithSortingByObjectIdentifierAsc()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectIdentifier().toString(), true);
    }

    @Test
    public void testSearchWithSortingByObjectIdentifierDesc()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().desc();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectIdentifier().toString(), false);
    }

    @Test
    public void testSearchWithSortingByMultipleFields()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        final GlobalSearchObjectSortOptions sortOptions = fo.sortBy();
        sortOptions.score().asc();
        sortOptions.objectKind().desc();
        sortOptions.objectPermId().asc();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        for (int index = 1, size = objects.size(); index < size; index++)
        {
            final GlobalSearchObject o1 = objects.get(index - 1);
            final GlobalSearchObject o2 = objects.get(index);
            final double value1 = o1.getScore();
            final double value2 = o2.getScore();
            final boolean scoresEqual = Math.abs(value1 - value2) < 0.0000001;
            if (scoresEqual)
            {
                final GlobalSearchObjectKind objectKind1 = o1.getObjectKind();
                final GlobalSearchObjectKind objectKind2 = o2.getObjectKind();

                if (objectKind1.equals(objectKind2))
                {
                    final IObjectId permId1 = o1.getObjectPermId();
                    final IObjectId permId2 = o2.getObjectPermId();

                    assertFalse(permId1.toString().compareTo(permId2.toString()) > 0,
                            "Subsubordering is incorrect. [index=" + index + "permId1=" + permId1 +
                                    ", permId2=" + permId2 + "]");
                } else
                {
                    assertFalse(objectKind1.compareTo(objectKind2) < 0,
                            "Subordering is incorrect. [index=" + index + "objectKind1=" + objectKind1 +
                                    ", objectKind2=" + objectKind2 + "]");
                }
            } else
            {
                assertFalse(value1 > value2,
                        "Ordering is incorrect. [index=" + index + "value1=" + value1 + ", value2=" + value2 + "]");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertSorted(final List<GlobalSearchObject> globalSearchObjects,
            final Function<GlobalSearchObject, Comparable<?>> valueRetriever, final boolean ascending)
    {
        for (int index = 1, size = globalSearchObjects.size(); index < size; index++)
        {
            final Comparable<Object> value1 = (Comparable<Object>) valueRetriever.apply(globalSearchObjects.get(index - 1));
            final Object value2 = valueRetriever.apply(globalSearchObjects.get(index));
            final int comparison = value1.compareTo(value2);
            assertFalse(ascending && comparison > 0 || !ascending && comparison < 0,
                    "Ordering is incorrect. [index=" + index + "value1=" + value1 + ", value2=" + value2 + "]");
        }
    }

    @Test
    public void testSearchWithPagingSameProperty()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().asc();
        fo.from(3).count(2);
        fo.withMatch();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(result.getTotalCount(), 7);
        assertEquals(objects.size(), 2);

        assertExperiment(objects.get(0), "201108050937246-1031", "/CISD/DEFAULT/EXP-Y", "Property 'Description': A simple experiment");
        assertExperiment(objects.get(1), "200811050951882-1028", "/CISD/NEMO/EXP1", "Property 'Description': A simple experiment");
    }

    @Test
    public void testSearchWithPagingDifferentProperties()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple male");

        final GlobalSearchObjectFetchOptions fo1 = new GlobalSearchObjectFetchOptions();
        fo1.from(1).count(2);
        fo1.withExperiment();
        fo1.withMatch();

        final SearchResult<GlobalSearchObject> searchResult = search(TEST_USER, criteria, fo1);
        final List<GlobalSearchObject> results = searchResult.getObjects();
        assertEquals(searchResult.getTotalCount(), 5);
        assertEquals(results.size(), 2);

        for (int i = 0; i < 2; i++)
        {
            final GlobalSearchObject globalSearchObject = results.get(i);
            assertEquals(globalSearchObject.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
            assertTrue(globalSearchObject.getMatch().contains("Property 'Description': A simple experiment"));
            assertTrue(globalSearchObject.getMatch().contains("Property 'Gender': MALE"));
            assertTrue(globalSearchObject.getScore() > 0);
        }
    }

    @Test
    public void testSearchWithExperimentPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200811050951882-1028", fo);

        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithExperimentPermIdAndExperimentFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200811050951882-1028", fo);

        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028");
        assertEquals(object.getExperiment().getCode(), "EXP1");
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithExperimentPermIdAndNonExperimentFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withSample();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200811050951882-1028", fo);

        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028");
        assertExperimentNotFetched(object);
        assertNull(object.getSample());
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndSampleFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withSample();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");
        assertEquals(object.getSample().getCode(), "CP-TEST-1");
        assertExperimentNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndNonSampleFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndNonSampleFetchedWithNoMatches()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "");
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithDataSetPermIdAndLocation()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("20110509092359990-11");
        criteria.withText().thatContainsExactly("LINK");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getTotalCount(), 5);

        final List<GlobalSearchObject> resultObjects = result.getObjects();
        final GlobalSearchObject object1 = resultObjects.stream().filter(
                globalSearchObject -> globalSearchObject.getDataSet().getCode().equals("20110509092359990-11"))
                .findAny().orElse(null);
        final GlobalSearchObject object2 = resultObjects.stream().filter(
                globalSearchObject -> globalSearchObject.getDataSet().getCode().equals("20120628092259000-23"))
                .findAny().orElse(null);

        assertDataSet(object1, "20110509092359990-11", object1.getMatch(), DataSetKind.PHYSICAL);
        AssertionUtil.assertContains("Perm ID: 20110509092359990-11", object1.getMatch());
        assertEquals(object1.getDataSet().getCode(), "20110509092359990-11");
        assertExperimentNotFetched(object1);
        assertSampleNotFetched(object1);
        assertMaterialNotFetched(object1);

        assertDataSet(object2, "20120628092259000-23", object2.getMatch(), DataSetKind.LINK);
        AssertionUtil.assertContains("DataSet kind: LINK", object2.getMatch());
        assertEquals(object2.getDataSet().getCode(), "20120628092259000-23");
        assertExperimentNotFetched(object2);
        assertSampleNotFetched(object2);
        assertMaterialNotFetched(object2);
    }

    @Test
    public void testSearchWithDataSetPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("20081105092159111-1", fo);

        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", null);
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithDataSetPermIdAndDataSetFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("20081105092159111-1", fo);

        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", DataSetKind.PHYSICAL);
        assertEquals(object.getDataSet().getCode(), "20081105092159111-1");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchDataSetWithKindLink()
    {
        // given
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        // when
        GlobalSearchObject object = searchAndAssertOne("20120628092259000-23", fo);

        // then
        assertDataSet(object, "20120628092259000-23", "Perm ID: 20120628092259000-23", DataSetKind.LINK);
    }

    @Test
    public void testSearchWithDataSetPermIdAndNonDataSetFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("20081105092159111-1", fo);

        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", null);
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1 (VIRUS)", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialPermIdAndMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMaterial();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1 (VIRUS)", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
        assertEquals(object.getMaterial().getPermId().toString(), "HSV1 (VIRUS)");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialPermIdAndNonMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1 (VIRUS)", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialCodeAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialCodeAndMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMaterial();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
        assertEquals(object.getMaterial().getPermId().toString(), "HSV1 (VIRUS)");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialCodeAndNonMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)");
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("/CISD/DEFAULT/EXP-REUSE");
        criteria.withText().thatContainsExactly("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        GlobalSearchObjectFetchOptions fetchOptions = new GlobalSearchObjectFetchOptions();

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        search(user.getUserId(), criteria, fetchOptions);
                    }
                });
        } else
        {
            SearchResult<GlobalSearchObject> result = search(user.getUserId(), criteria, fetchOptions);

            if (user.isInstanceUser())
            {
                assertEquals(result.getObjects().size(), 2);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEquals(result.getObjects().size(), 1);
                assertEquals(result.getObjects().get(0).getObjectIdentifier().toString(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
            } else
            {
                assertEquals(result.getObjects().size(), 0);
            }
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        GlobalSearchCriteria c = new GlobalSearchCriteria();
        c.withText().thatContainsExactly("200902091219327-1025");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withSample();

        v3api.searchGlobally(sessionToken, c, fo);

        assertAccessLog(
                "search-globally  SEARCH_CRITERIA:\n'GLOBAL_SEARCH\n    any field contains exactly '200902091219327-1025'\n'\nFETCH_OPTIONS:\n'GlobalSearchObject\n    with Sample\n    with DataSet\n'");
    }

    @Test
    public void testTextThatContainsPermIdsAndCode()
    {
        final GlobalSearchCriteria c = new GlobalSearchCriteria();
        c.withText().thatContains("200902091239077-1033 20110509092359990-11 200811050919915-8 VIRUS1");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withExperiment();
        fo.withSample();
        fo.withMaterial();
        fo.withMatch();

        final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
        assertEquals(results.size(), 4);

        results.forEach(result ->
        {
            switch (result.getObjectKind())
            {
                case DATA_SET:
                {
                    assertEquals(result.getObjectPermId().toString(), "20110509092359990-11");
                    assertEquals(result.getObjectIdentifier().toString(), "20110509092359990-11");
                    assertTrue(result.getMatch().contains("Perm ID: 20110509092359990-11"));
                    assertTrue(result.getScore() > 0);
                    assertNull(result.getExperiment());
                    assertNull(result.getSample());
                    assertEquals(result.getDataSet().getCode(), "20110509092359990-11");
                    assertNull(result.getMaterial());
                    break;
                }
                case EXPERIMENT:
                {
                    assertEquals(result.getObjectPermId().toString(), "200902091239077-1033");
                    assertEquals(result.getObjectIdentifier().toString(), "/CISD/NEMO/EXP-TEST-1");
                    assertEquals(result.getMatch(), "Perm ID: 200902091239077-1033");
                    assertTrue(result.getScore() > 0);
                    assertEquals(result.getExperiment().getCode(), "EXP-TEST-1");
                    assertNull(result.getSample());
                    assertNull(result.getDataSet());
                    assertNull(result.getMaterial());
                    break;
                }
                case SAMPLE:
                {
                    assertEquals(result.getObjectPermId().toString(), "200811050919915-8");
                    assertEquals(result.getObjectIdentifier().toString(), "/CISD/CL1");
                    assertEquals(result.getMatch(), "Perm ID: 200811050919915-8");
                    assertTrue(result.getScore() > 0);
                    assertNull(result.getExperiment());
                    assertEquals(result.getSample().getCode(), "CL1");
                    assertNull(result.getDataSet());
                    assertNull(result.getMaterial());
                    break;
                }
                case MATERIAL:
                {
                    final MaterialPermId materialPermId = (MaterialPermId) result.getObjectPermId();
                    final MaterialPermId materialObjectIdentifier = (MaterialPermId) result.getObjectIdentifier();
                    assertEquals(materialPermId.toString(), "VIRUS1 (VIRUS)");
                    assertEquals(materialPermId.getCode(), "VIRUS1");
                    assertEquals(materialPermId.getTypeCode(), "VIRUS");
                    assertEquals(materialObjectIdentifier.toString(), "VIRUS1 (VIRUS)");
                    assertEquals(materialObjectIdentifier.getCode(), "VIRUS1");
                    assertEquals(materialObjectIdentifier.getTypeCode(), "VIRUS");
                    assertEquals(result.getMatch(), "Identifier: VIRUS1 (VIRUS)");
                    assertTrue(result.getScore() > 0);
                    assertNull(result.getExperiment());
                    assertNull(result.getSample());
                    assertNull(result.getDataSet());
                    assertEquals(result.getMaterial().getCode(), "VIRUS1");
                    break;
                }
            }
        });
    }

    @Test
    public void testRanking()
    {
        final GlobalSearchCriteria c = new GlobalSearchCriteria();
        c.withText().thatContains("simple male");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
        assertEquals(results.size(), 5);

        assertTrue(results.get(0).getScore() > results.get(4).getScore());

        for (int i = 0; i < 3; i++)
        {
            final GlobalSearchObject globalSearchObject = results.get(i);
            assertEquals(globalSearchObject.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
            assertTrue(globalSearchObject.getMatch().contains("Property 'Description': A simple experiment"));
            assertTrue(globalSearchObject.getMatch().contains("Property 'Gender': MALE"));
            assertTrue(globalSearchObject.getScore() > 0);
        }

        for (int i = 3; i < 5; i++)
        {
            final GlobalSearchObject globalSearchObject = results.get(i);
            assertTrue(globalSearchObject.getObjectKind() == GlobalSearchObjectKind.EXPERIMENT ||
                    globalSearchObject.getObjectKind() == GlobalSearchObjectKind.SAMPLE);
            assertTrue(globalSearchObject.getMatch().contains("Property 'Description': A simple experiment") ||
                    globalSearchObject.getMatch().contains("Property 'Comment': extremely simple stuff"));
            assertTrue(globalSearchObject.getScore() > 0);
        }
    }

    @Test
    public void testCharacterCases()
    {
        final GlobalSearchCriteria c1 = new GlobalSearchCriteria();
        c1.withText().thatContains("simple male");

        final GlobalSearchCriteria c2 = new GlobalSearchCriteria();
        c2.withText().thatContains("SIMPLE MALE");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        final List<GlobalSearchObject> results1 = search(TEST_USER, c1, fo).getObjects();
        final List<GlobalSearchObject> results2 = search(TEST_USER, c2, fo).getObjects();
        assertEquals(results1.size(), results2.size());

        for (int i = 0; i < results1.size(); i++)
        {
            final GlobalSearchObject result1 = results1.get(i);
            final GlobalSearchObject result2 = results2.get(i);

            assertEquals(result1.getObjectKind(), result2.getObjectKind());
            assertEquals(result1.getObjectPermId(), result2.getObjectPermId());
            assertEquals(result1.getObjectIdentifier(), result2.getObjectIdentifier());
            assertEquals(result1.getMatch(), result2.getMatch());
            assertEquals(result1.getScore(), result2.getScore());

            assertEquals((result1.getExperiment() != null) ? result1.getExperiment().getPermId() : null,
                    (result2.getExperiment() != null) ? result2.getExperiment().getPermId() : null);
        }
    }

    @Test
    public void testComplexScoreSortingForSamples() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        String term = "RAT";
        List<SampleCreation> creations = getSampleCreationsForTest(term);
        List<SamplePermId> identifiers = v3api.createSamples(sessionToken, creations);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatContains(term);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withSample();
            fo.sortBy().score().desc();

            final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
            assertTrue(results.size() >= 4);
            assertEquals(results.get(0).getSample().getCode(), term);
            assertEquals(results.get(1).getSample().getCode(), term + "2");
            assertTrue((results.get(2).getSample() != null &&
                    results.get(2).getSample().getCode().equals(term + "3")) ||
                    (results.get(3).getSample() != null && results.get(3).getSample().getCode().equals(term + "3")));
        } finally
        {
            /* Cleanup */
            cleanupSamplesForTest(v3api, sessionToken, identifiers);
        }
    }

    @Test
    public void testComplexScoreSortingForExperiments() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        String term = "RAT";
        List<ExperimentCreation> creations = getExperimentCreationsForTest(term);
        List<ExperimentPermId> identifiers = v3api.createExperiments(sessionToken, creations);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatContains(term);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withExperiment();
            fo.sortBy().score().desc();

            final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
            assertTrue(results.size() >= 4);
            assertEquals(results.get(0).getExperiment().getCode(), term);
            assertEquals(results.get(1).getExperiment().getCode(), term + "2");
            assertTrue((results.get(2).getExperiment() != null &&
                    results.get(2).getExperiment().getCode().equals(term + "3")) ||
                    (results.get(3).getExperiment() != null &&
                    results.get(3).getExperiment().getCode().equals(term + "3")));
        } finally
        {
            /* Cleanup */
            cleanupExperinmentsForTest(v3api, sessionToken, identifiers);
        }
    }

    @Test
    public void testComplexScoreSortingForDataSets() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        String term = "RAT";
        List<DataSetCreation> creations = getDataSetsCreationsForTest(term);
        List<DataSetPermId> identifiers = v3api.createDataSets(sessionToken, creations);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatContains(term);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withDataSet();
            fo.sortBy().score().desc();

            final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
            assertTrue(results.size() >= 4);
            assertEquals(results.get(0).getDataSet().getCode(), term);
            assertEquals(results.get(1).getDataSet().getCode(), term + "2");
            assertTrue((results.get(2).getDataSet() != null &&
                    results.get(2).getDataSet().getCode().equals(term + "3")) ||
                    (results.get(3).getDataSet() != null && results.get(3).getDataSet().getCode().equals(term + "3")));
        } finally
        {
            /* Cleanup */
            cleanupDataSetsForTest(v3api, sessionToken, identifiers);
        }
    }

    @Test
    public void testComplexScoreSortingForAll() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        String term = "RAT";
        List<SampleCreation> creationsS = getSampleCreationsForTest(term);
        List<SamplePermId> identifiersS = v3api.createSamples(sessionToken, creationsS);
        List<ExperimentCreation> creationsE = getExperimentCreationsForTest(term);
        List<ExperimentPermId> identifiersE = v3api.createExperiments(sessionToken, creationsE);
        List<DataSetCreation> creationsD = getDataSetsCreationsForTest(term);
        List<DataSetPermId> identifiersD = v3api.createDataSets(sessionToken, creationsD);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatContains(term);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withSample();
            fo.withExperiment();
            fo.withDataSet();
            fo.sortBy().score().desc();
            fo.sortBy().objectKind().desc();

            final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
            assertTrue(results.size() >= 12);

            /*
             * When the same Score is given, DataSets come before Samples that come before Experiments
             */

            assertEquals(results.get(0).getDataSet().getCode(), term);
            assertEquals(results.get(1).getSample().getCode(), term);
            assertEquals(results.get(2).getExperiment().getCode(), term);

            assertEquals(results.get(3).getDataSet().getCode(), term + "2");
            assertEquals(results.get(4).getSample().getCode(), term + "2");
            assertEquals(results.get(5).getExperiment().getCode(), term + "2");

            assertEquals(results.get(6).getDataSet().getCode(), term + "3");
            assertEquals(results.get(7).getSample().getCode(), term + "3");
            assertEquals(results.get(9).getExperiment().getCode(), term + "3");
        } finally
        {
            /* Cleanup */
            cleanupSamplesForTest(v3api, sessionToken, identifiersS);
            cleanupExperinmentsForTest(v3api, sessionToken, identifiersE);
            cleanupDataSetsForTest(v3api, sessionToken, identifiersD);
        }
    }

    private static DataSetCreation getDataSetCreationForTest(String code, String description, String organism) {
        DataSetCreation creation = new DataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER"));
        creation.setComponentIds(Arrays.asList(new DataSetPermId("DATASET-TO-DELETE")));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setDataStoreId(new DataStorePermId("STANDARD"));

        if (description != null) {
            creation.setProperty("DESCRIPTION", description);
        }
        if (organism != null) {
            creation.setProperty("ORGANISM", organism);
        }
        return creation;
    }

    private static List<DataSetCreation> getDataSetsCreationsForTest(String term) {
        List<DataSetCreation> creations = new ArrayList<>();

        // Score 1000 + 100 + 1 = 1101
        creations.add(getDataSetCreationForTest(term, term, term));
        // Score 100 + 1 = 101
        creations.add(getDataSetCreationForTest(term + "2", term, term));
        // Score 100 = 100
        creations.add(getDataSetCreationForTest(term + "3", null, term));
        // Score 1 = 1
        creations.add(getDataSetCreationForTest(term + "4", term, null));

        return creations;
    }

    private static void cleanupDataSetsForTest(IApplicationServerInternalApi v3api, String sessionToken, List<? extends IDataSetId> identifiers) {
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("Test Cleanup");
        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, identifiers, deletionOptions);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
    }

    private static ExperimentCreation getExperimentCreationForTest(String code, String description, String organism) {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        if (description != null) {
            creation.setProperty("DESCRIPTION", description);
        }
        if (organism != null) {
            creation.setProperty("ORGANISM", organism);
        }
        return creation;
    }

    private static List<ExperimentCreation> getExperimentCreationsForTest(String term) {
        List<ExperimentCreation> creations = new ArrayList<>();

        // Score 1000 + 100 + 1 = 1101
        creations.add(getExperimentCreationForTest(term, term, term));
        // Score 100 + 1 = 101
        creations.add(getExperimentCreationForTest(term + "2", term, term));
        // Score 100 = 100
        creations.add(getExperimentCreationForTest(term + "3", null, term));
        // Score 1 = 1
        creations.add(getExperimentCreationForTest(term + "4", term, null));

        return creations;
    }

    private static void cleanupExperinmentsForTest(IApplicationServerInternalApi v3api, String sessionToken, List<? extends IExperimentId> identifiers) {
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("Test Cleanup");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, identifiers, deletionOptions);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
    }

    private static List<SampleCreation> getSampleCreationsForTest(String term) {
        List<SampleCreation> creations = new ArrayList<>();

        // Score 1000 + 100 + 1 = 1101
        creations.add(getSampleCreationForTest(term, term, term));
        // Score 100 + 1 = 101
        creations.add(getSampleCreationForTest(term + "2", term, term));
        // Score 100 = 100
        creations.add(getSampleCreationForTest(term + "3", null, term));
        // Score 1 = 1
        creations.add(getSampleCreationForTest(term + "4", term, null));

        return creations;
    }

    private static SampleCreation getSampleCreationForTest(String code, String comment, String organism) {
        SampleCreation creation = new SampleCreation();
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        if (comment != null) {
            creation.setProperty("COMMENT", comment);
        }
        if (organism != null) {
            creation.setProperty("ORGANISM", organism);
        }
        return creation;
    }

    private static void cleanupSamplesForTest(IApplicationServerInternalApi v3api, String sessionToken, List<? extends ISampleId> identifiers) {
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("Test Cleanup");
        IDeletionId deletionId = v3api.deleteSamples(sessionToken, identifiers, deletionOptions);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
    }

    private SearchResult<GlobalSearchObject> search(String user, GlobalSearchCriteria criteria, GlobalSearchObjectFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<GlobalSearchObject> result = v3api.searchGlobally(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return result;
    }

    private GlobalSearchObject searchAndAssertOne(String permId, GlobalSearchObjectFetchOptions fetchOptions)
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly(permId);

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fetchOptions);
        assertEquals(result.getObjects().size(), 1);

        return result.getObjects().get(0);
    }

    private GlobalSearchObject searchAndAssertOneOrNone(String user, String permId, GlobalSearchObjectKind... objectKinds)
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly(permId);
        criteria.withObjectKind().thatIn(objectKinds);

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertTrue(result.getObjects().size() <= 1);

        return result.getObjects().isEmpty() ? null : result.getObjects().get(0);
    }

    private void assertStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 3);
        final GlobalSearchObject obj1 = findObjectByPermId(objects, "200902091219327-1025");
        final GlobalSearchObject obj2 = findObjectByPermId(objects, "200902091250077-1026");
        final GlobalSearchObject obj3 = findObjectByPermId(objects, "200902091225616-1027");

        assertSample(obj1, "200902091219327-1025", "/CISD/CP-TEST-1", "Property 'Comment': very advanced stuff");
        assertSample(obj2, "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff");
        assertSample(obj3, "200902091225616-1027", "/CISD/CP-TEST-3", "Property 'Comment': stuff like others");
    }

    private void assertSimpleStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 1);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        assertSample(iter.next(), "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff");
    }

    private void assertSimpleOrStuff(final SearchResult<GlobalSearchObject> result)
    {
        final List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 7);

        // Even though we have 8 results, one of them has two matches. Therefore, we need just 7 search objects.
        final GlobalSearchObject[] searchObjects = new GlobalSearchObject[] {
                findObjectByPermId(objects, "200902091219327-1025"),
                findObjectByPermId(objects, "200902091250077-1026"),
                findObjectByPermId(objects, "200902091225616-1027"),
                findObjectByPermId(objects, "201108050937246-1031"),
                findObjectByPermId(objects, "200811050951882-1028"),
                findObjectByPermId(objects, "200811050952663-1029"),
                findObjectByPermId(objects, "200811050952663-1030"),
        };

        assertSample(searchObjects[0], "200902091219327-1025", "/CISD/CP-TEST-1",
                "Property 'Comment': very advanced stuff");
        assertSample(searchObjects[1], "200902091250077-1026", "/CISD/CP-TEST-2",
                "Property 'Comment': extremely simple stuff");
        assertSample(searchObjects[2], "200902091225616-1027", "/CISD/CP-TEST-3",
                "Property 'Comment': stuff like others");
        assertExperiment(searchObjects[3], "201108050937246-1031", "/CISD/DEFAULT/EXP-Y",
                "Property 'Description': A simple experiment");
        assertExperiment(searchObjects[4], "200811050951882-1028", "/CISD/NEMO/EXP1",
                "Property 'Description': A simple experiment");
        assertExperiment(searchObjects[5], "200811050952663-1029", "/CISD/NEMO/EXP10",
                "Property 'Description': A simple experiment");
        assertExperiment(searchObjects[6], "200811050952663-1030", "/CISD/NEMO/EXP11",
                "Property 'Description': A simple experiment");
    }

    /**
     * Searches for an object with specified perm ID.
     * @param objects collection of objects to search in.
     * @param permId perm ID to search by.
     * @return the first found object with the perm ID or {@code null} if none is found.
     */
    private GlobalSearchObject findObjectByPermId(final Collection<GlobalSearchObject> objects, final String permId)
    {
        return objects.stream().filter((obj) -> obj.getObjectPermId().toString().equals(permId)).limit(1).findFirst()
                .orElse(null);
    }

    private void assertExperiment(GlobalSearchObject object, String permId, String identifier, String match)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(object.getObjectPermId(), new ExperimentPermId(permId));
        assertEquals(object.getObjectIdentifier(), new ExperimentIdentifier(identifier));
        assertEquals(object.getMatch(), match);
        assertTrue(object.getScore() > 0);
    }

    private void assertSample(GlobalSearchObject object, String permId, String identifier, String match)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(object.getObjectPermId(), new SamplePermId(permId));
        assertEquals(object.getObjectIdentifier(), new SampleIdentifier(identifier));
        assertEquals(object.getMatch(), match);
        assertTrue(object.getScore() > 0);
    }

    private void assertDataSet(GlobalSearchObject object, String code, String match, DataSetKind dataSetKind)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.DATA_SET);
        assertEquals(object.getObjectPermId(), new DataSetPermId(code));
        assertEquals(object.getObjectIdentifier(), new DataSetPermId(code));
        assertEquals(object.getMatch(), match);
        assertTrue(object.getScore() > 0);
        if (dataSetKind != null)
        {
            assertEquals(object.getDataSet().getKind(), dataSetKind);
        }
    }

    private void assertMaterial(GlobalSearchObject object, String code, String typeCode, String match)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.MATERIAL);
        assertEquals(object.getObjectPermId(), new MaterialPermId(code, typeCode));
        assertEquals(object.getObjectIdentifier(), new MaterialPermId(code, typeCode));
        assertEquals(object.getMatch(), match);
        assertTrue(object.getScore() > 0);
    }

    private void assertExperimentNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getExperiment();
                }
            });
    }

    private void assertSampleNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getSample();
                }
            });
    }

    private void assertDataSetNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getDataSet();
                }
            });
    }

    private void assertMaterialNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getMaterial();
                }
            });
    }

}
