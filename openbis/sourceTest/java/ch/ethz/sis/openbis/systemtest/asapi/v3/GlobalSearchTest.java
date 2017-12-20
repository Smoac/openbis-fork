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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

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

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
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
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertStuff(result);
    }

    @Test
    public void testSearchWithOneContainsMultipleWords()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsOneWord()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple");
        criteria.withText().thatContains("stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsMultpleWords()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");
        criteria.withText().thatContains("stuff simple");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithOneContainsExactlyOneWord()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertStuff(result);
    }

    @Test
    public void testSearchWithOneContainsExactlyMultipleWords()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsExactlyOneWord()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple");
        criteria.withText().thatContainsExactly("stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsExactlyMultipleWords()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");
        criteria.withText().thatContainsExactly("simple stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleStuff(result);
    }

    @Test
    public void testSearchWithObjectKindsSpecified()
    {
        GlobalSearchObject object = null;

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
    public void testSearchWithContainsAndWildCardsEnabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuf*");
        criteria.withWildCards();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertStuff(result);
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
    public void testSearchWithContainsExactlyAndWildCardsEnabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuf*");
        criteria.withWildCards();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertSimpleStuff(result);
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

        assertEquals(objects.get(0).getScore(), 1.0);
        assertEquals(objects.get(objects.size() - 1).getScore(), 20.0);
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

        assertEquals(objects.get(0).getScore(), 20.0);
        assertEquals(objects.get(objects.size() - 1).getScore(), 1.0);
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

        assertEquals(objects.get(0).getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(objects.get(objects.size() - 1).getObjectKind(), GlobalSearchObjectKind.MATERIAL);
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

        assertEquals(objects.get(0).getObjectKind(), GlobalSearchObjectKind.MATERIAL);
        assertEquals(objects.get(objects.size() - 1).getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
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

        assertEquals(objects.get(0).getObjectPermId().toString(), "200811050951882-1028");
        assertEquals(objects.get(objects.size() - 1).getObjectPermId().toString(), "HSV1 (VIRUS)");
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

        assertEquals(objects.get(0).getObjectPermId().toString(), "HSV1 (VIRUS)");
        assertEquals(objects.get(objects.size() - 1).getObjectPermId().toString(), "200811050951882-1028");
    }

    @Test
    public void testSearchWithSortingByObjectIdentifierAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(objects.get(0).getObjectIdentifier().toString(), "/CISD/CP-TEST-1");
        assertEquals(objects.get(objects.size() - 1).getObjectIdentifier().toString(), "HSV1 (VIRUS)");
    }

    @Test
    public void testSearchWithSortingByObjectIdentifierDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(objects.get(0).getObjectIdentifier().toString(), "HSV1 (VIRUS)");
        assertEquals(objects.get(objects.size() - 1).getObjectIdentifier().toString(), "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithPaging()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.from(1).count(2);

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(result.getTotalCount(), 8);
        assertEquals(objects.size(), 2);

        assertExperiment(objects.get(0), "201108050937246-1031", "/CISD/DEFAULT/EXP-Y", "Property 'Description': A simple experiment");
        assertExperiment(objects.get(1), "200811050951882-1028", "/CISD/NEMO/EXP1", "Property 'Description': A simple experiment");
    }

    @Test
    public void testSearchWithExperimentPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "200811050951882-1028", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "200811050951882-1028", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "200811050951882-1028", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "200902091219327-1025", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "200902091219327-1025", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025");
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

        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("20110509092359990-11");
        criteria.withText().thatContainsExactly("result-18");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getObjects().size(), 2);

        GlobalSearchObject object1 = result.getObjects().get(0);
        GlobalSearchObject object2 = result.getObjects().get(1);

        assertDataSet(object1, "20110509092359990-11", object1.getMatch(), DataSetKind.PHYSICAL);
        AssertionUtil.assertContains("Location: contained/20110509092359990-11", object1.getMatch());
        AssertionUtil.assertContains("Perm ID: 20110509092359990-11", object1.getMatch());
        assertEquals(object1.getDataSet().getCode(), "20110509092359990-11");
        assertEquals(object1.getScore(), 210.0);
        assertExperimentNotFetched(object1);
        assertSampleNotFetched(object1);
        assertMaterialNotFetched(object1);

        assertDataSet(object2, "20081105092259000-18", object2.getMatch(), DataSetKind.PHYSICAL);
        AssertionUtil.assertContains("Location: xml/result-18", object2.getMatch());
        assertEquals(object2.getDataSet().getCode(), "20081105092259000-18");
        assertEquals(object2.getScore(), 10.0);
        assertExperimentNotFetched(object2);
        assertSampleNotFetched(object2);
        assertMaterialNotFetched(object2);
    }

    @Test
    public void testSearchWithDataSetPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "20081105092159111-1", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "20081105092159111-1", fo);

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

        // when
        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "20120628092259000-23", fo);

        // then
        assertDataSet(object, "20120628092259000-23", "Perm ID: 20120628092259000-23", DataSetKind.LINK);
    }

    @Test
    public void testSearchWithDataSetPermIdAndNonDataSetFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "20081105092159111-1", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "HSV1 (VIRUS)", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "HSV1 (VIRUS)", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "HSV1 (VIRUS)", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "HSV1", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "HSV1", fo);

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

        GlobalSearchObject object = searchAndAssertOne(TEST_USER, "HSV1", fo);

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

    private SearchResult<GlobalSearchObject> search(String user, GlobalSearchCriteria criteria, GlobalSearchObjectFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<GlobalSearchObject> result = v3api.searchGlobally(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return result;
    }

    private GlobalSearchObject searchAndAssertOne(String user, String permId, GlobalSearchObjectFetchOptions fetchOptions)
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

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertTrue(result.getObjects().size() <= 1);

        return result.getObjects().isEmpty() ? null : result.getObjects().get(0);
    }

    private void assertStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 3);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        assertSample(iter.next(), "200902091219327-1025", "/CISD/CP-TEST-1", "Property 'Comment': very advanced stuff");
        assertSample(iter.next(), "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff");
        assertSample(iter.next(), "200902091225616-1027", "/CISD/CP-TEST-3", "Property 'Comment': stuff like others");
    }

    private void assertSimpleStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 1);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        assertSample(iter.next(), "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff");
    }

    private void assertSimpleOrStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 8);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        assertSample(iter.next(), "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff");
        assertExperiment(iter.next(), "201108050937246-1031", "/CISD/DEFAULT/EXP-Y", "Property 'Description': A simple experiment");
        assertExperiment(iter.next(), "200811050951882-1028", "/CISD/NEMO/EXP1", "Property 'Description': A simple experiment");
        assertExperiment(iter.next(), "200811050952663-1029", "/CISD/NEMO/EXP10", "Property 'Description': A simple experiment");
        assertExperiment(iter.next(), "200811050952663-1030", "/CISD/NEMO/EXP11", "Property 'Description': A simple experiment");
        assertSample(iter.next(), "200902091219327-1025", "/CISD/CP-TEST-1", "Property 'Comment': very advanced stuff");
        assertSample(iter.next(), "200902091225616-1027", "/CISD/CP-TEST-3", "Property 'Comment': stuff like others");
        assertMaterial(iter.next(), "HSV1", "VIRUS", "Property 'Description': Herpes Simplex Virus 1");
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
