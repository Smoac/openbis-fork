/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;

public class FetchOptionsMatcherTest
{

    @Test
    public void testMatchTheSameObjects()
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        assertMatch(fo, fo, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchEmptyObjects()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        SampleFetchOptions fo2 = new SampleFetchOptions();
        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsOfDifferentTypes()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        DataSetFetchOptions fo2 = new DataSetFetchOptions();
        assertMatch(fo1, fo2, null);
    }

    @Test
    public void testMatchObjectsWithTheSameParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withExperiment();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withExperiment();

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithDifferentParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withExperiment();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();

        assertMatch(fo1, fo2, null);
    }

    @Test
    public void testMatchObjectsWithSameTopLevelRecursiveParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withChildrenUsing(fo2);

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithDifferentTopLevelRecursiveParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withExperiment();
        fo2.withChildrenUsing(fo2);

        assertMatch(fo1, fo2, null);
    }

    @Test
    public void testMatchObjectsWithSameSubLevelRecursiveParts()
    {
        SampleFetchOptions fo1Children = new SampleFetchOptions();
        fo1Children.withExperiment();
        fo1Children.withChildrenUsing(fo1Children);

        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1Children);

        SampleFetchOptions fo2Children = new SampleFetchOptions();
        fo2Children.withExperiment();
        fo2Children.withChildrenUsing(fo2Children);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withChildrenUsing(fo2Children);

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithDifferentSubLevelRecursiveParts()
    {
        SampleFetchOptions fo1Children = new SampleFetchOptions();
        fo1Children.withExperiment();
        fo1Children.withChildrenUsing(fo1Children);

        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1Children);

        SampleFetchOptions fo2Children = new SampleFetchOptions();
        fo2Children.withContainer();
        fo2Children.withChildrenUsing(fo2Children);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withChildrenUsing(fo2Children);

        assertMatch(fo1, fo2, null);
    }

    @Test
    public void testMatchObjectsWithSameMultiLevelParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace().withProjects().withAttachments();
        fo1.withChildren().withDataSets().withHistory();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace().withProjects().withAttachments();
        fo2.withChildren().withDataSets().withHistory();

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithDifferentMultiLevelParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace().withProjects().withAttachments();
        fo1.withChildren().withDataSets().withHistory();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace().withProjects().withAttachments();
        fo2.withChildren().withDataSets();

        assertMatch(fo1, fo2, null);
    }

    @Test
    public void testMatchObjectsWithSameTopLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.from(10).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.count(5).from(10);

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithDifferentTopLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.from(10).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.from(3).count(7);

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_SUB_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithSameSubLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.withTags().from(1).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.withTags().from(1).count(5);

        assertMatch(fo1, fo2, FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING);
    }

    @Test
    public void testMatchObjectsWithDifferentSubLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.withTags().from(1).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.withTags().from(2).count(6);

        assertMatch(fo1, fo2, null);
    }

    private void assertMatch(Object o1, Object o2, FetchOptionsMatchType matchType)
    {
        Assert.assertEquals(FetchOptionsMatcher.match(o1, o2), matchType);
    }

}
