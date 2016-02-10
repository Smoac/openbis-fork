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
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
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

/**
 * @author pkupczyk
 */
public class GlobalSearchTest extends AbstractTest
{

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

    @Test(enabled = false)
    // enable when SSDM-3164 is done
    public void testSearchWithMultipleContainsExactlyOneWord()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple");
        criteria.withText().thatContainsExactly("stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleOrStuff(result);
    }

    @Test(enabled = false)
    // enable when SSDM-3164 is done
    public void testSearchWithMulipleContainsExactlyMultipleWords()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");
        criteria.withText().thatContainsExactly("simple stuff");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());

        assertSimpleStuff(result);
    }

    private SearchResult<GlobalSearchObject> search(String user, GlobalSearchCriteria criteria, GlobalSearchObjectFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<GlobalSearchObject> result = v3api.searchGlobally(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return result;
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
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(object.getObjectPermId(), new ExperimentPermId(permId));
        assertEquals(object.getObjectIdentifier(), new ExperimentIdentifier(identifier));
        assertEquals(object.getMatch(), match + "\n");
        assertTrue(object.getScore() > 0);
    }

    private void assertSample(GlobalSearchObject object, String permId, String identifier, String match)
    {
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(object.getObjectPermId(), new SamplePermId(permId));
        assertEquals(object.getObjectIdentifier(), new SampleIdentifier(identifier));
        assertEquals(object.getMatch(), match + "\n");
        assertTrue(object.getScore() > 0);
    }

    private void assertDataSet(GlobalSearchObject object, String code, String match)
    {
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.DATA_SET);
        assertEquals(object.getObjectPermId(), new DataSetPermId(code));
        assertEquals(object.getObjectIdentifier(), new DataSetPermId(code));
        assertEquals(object.getMatch(), match + "\n");
        assertTrue(object.getScore() > 0);
    }

    private void assertMaterial(GlobalSearchObject object, String code, String typeCode, String match)
    {
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.MATERIAL);
        assertEquals(object.getObjectPermId(), new MaterialPermId(code, typeCode));
        assertEquals(object.getObjectIdentifier(), new MaterialPermId(code, typeCode));
        assertEquals(object.getMatch(), match + "\n");
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
