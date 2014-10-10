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
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class SampleCreateTest extends AbstractSampleTest
{

    @DataProvider(name = "tf-ft-tt")
    Object[][] getDataProviderForRelationTest()
    {
        return new Object[][] {
                new Object[] { true, false },
                new Object[] { false, true },
                new Object[] { true, true },
        };
    }

    @Test(dataProvider = "tf-ft-tt")
    public void testCreateTwoSamplesWithContainerRelation(boolean setRelationOnChild, boolean setRelationOnParent)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_CONTAINER");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_SUB_SAMPLE");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));

        if (setRelationOnChild)
        {
            sampleChild.setContainerId(sampleParent.getCreationId());
        }

        if (setRelationOnParent)
        {
            sampleParent.setContainedIds(Arrays.asList(sampleChild.getCreationId()));
        }

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchContained();
        fetchOptions.fetchContainer();

        List<Sample> samples = v3api.listSamples(sessionToken, sampleIds, fetchOptions);

        Sample container = samples.get(0);
        Sample subSample = samples.get(1);

        AssertionUtil.assertCollectionContains(container.getContained(), subSample);
        assertEquals(subSample.getContainer(), container);

        AssertionUtil.assertCollectionSize(subSample.getContained(), 0);

        assertEquals(container.getContainer(), null);
        AssertionUtil.assertCollectionSize(container.getContained(), 1);
    }

    @Test(dataProvider = "tf-ft-tt")
    public void testCreateTwoSamplesWithParentChildRelation(boolean setRelationOnChild, boolean setRelationOnParent)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));

        if (setRelationOnChild)
        {
            sampleChild.setParentIds(Arrays.asList(sampleParent.getCreationId()));
        }

        if (setRelationOnParent)
        {
            sampleParent.setChildIds(Arrays.asList(sampleChild.getCreationId()));
        }

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchChildren(fetchOptions);
        fetchOptions.fetchParents(fetchOptions);

        List<Sample> samples = v3api.listSamples(sessionToken, sampleIds, fetchOptions);

        Sample parent = samples.get(0);
        Sample child = samples.get(1);

        AssertionUtil.assertCollectionContains(parent.getChildren(), child);
        AssertionUtil.assertCollectionContains(child.getParents(), parent);

        AssertionUtil.assertCollectionSize(child.getParents(), 1);
        AssertionUtil.assertCollectionSize(child.getChildren(), 0);

        AssertionUtil.assertCollectionSize(parent.getParents(), 0);
        AssertionUtil.assertCollectionSize(parent.getChildren(), 1);
    }

    @Test
    public void testCreateSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation samp1 = new SampleCreation();
        samp1.setCode("SAMPLE_WITH_SPACE1");
        samp1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        samp1.setSpaceId(new SpacePermId("CISD"));
        samp1.setProperty("COMMENT", "hello");
        samp1.setContainerId(new SampleIdentifier("/CISD/MP002-1"));
        samp1.setTagIds(Arrays.<ITagId> asList(
                new TagPermId("/test/TEST_METAPROJECTS")
                , new TagPermId("/test/ANOTHER_TEST_METAPROJECTS")
                ));
        AttachmentCreation a = new AttachmentCreation();

        byte[] attachmentContent = "attachment".getBytes();
        a.setContent(attachmentContent);
        a.setDescription("attachment description");
        a.setFileName("attachment.txt");
        a.setTitle("attachment title");
        samp1.setAttachments(Arrays.asList(a));

        SampleCreation samp2 = new SampleCreation();
        samp2.setCode("SAMPLE_WITH_SPACE2");
        samp2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        samp2.setSpaceId(new SpacePermId("CISD"));
        samp2.setContainerId(new SamplePermId("200811050917877-331"));
        List<SamplePermId> sample2Parents = Arrays.asList(new SamplePermId("200811050917877-331"), new SamplePermId("200902091219327-1025"));
        samp2.setParentIds(sample2Parents);

        SampleCreation sampleWithOutSpace = new SampleCreation();
        sampleWithOutSpace.setCode("SAMPLE_WITHOUT_SPACE");
        sampleWithOutSpace.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleWithOutSpace.setChildIds(sample2Parents);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken,
                Arrays.asList(samp1, samp2, sampleWithOutSpace));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchSampleType();
        fetchOptions.fetchSpace();
        fetchOptions.fetchRegistrator();
        fetchOptions.fetchAttachments().fetchContent();
        fetchOptions.fetchContainer();
        fetchOptions.fetchTags();
        fetchOptions.fetchParents();

        List<Sample> samples = v3api.listSamples(sessionToken, sampleIds,
                fetchOptions);

        Sample sampleWithSpace1 = samples.get(0);
        assertEquals(sampleWithSpace1.getCode(), "SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getSampleType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithSpace1.getSpace().getPermId().getPermId(), "CISD");
        assertEquals(sampleWithSpace1.getIdentifier().getIdentifier(), "/CISD/MP002-1:SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getRegistrator().getUserId(), TEST_USER);
        assertEquals(sampleWithSpace1.getContainer().getCode(), "MP002-1");
        List<Attachment> attachments = sampleWithSpace1.getAttachments();
        AssertionUtil.assertCollectionSize(attachments, 1);
        assertEquals(attachments.get(0).getContent(), attachmentContent);
        HashSet<String> tagIds = new HashSet<String>();
        for (Tag tag : sampleWithSpace1.getTags())
        {
            tagIds.add(tag.getPermId().getPermId());
        }
        assertEquals(tagIds, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));

        Sample sampleWithSpace2 = samples.get(1);
        assertEquals(sampleWithSpace2.getCode(), "SAMPLE_WITH_SPACE2");
        assertEquals(sampleWithSpace2.getSampleType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithSpace2.getSpace().getPermId().getPermId(), "CISD");
        assertEquals(sampleWithSpace2.getIdentifier().getIdentifier(), "/CISD/MP002-1:SAMPLE_WITH_SPACE2");
        assertEquals(sampleWithSpace2.getRegistrator().getUserId(), TEST_USER);
        assertEquals(sampleWithSpace2.getContainer().getCode(), "MP002-1");
        List<String> parentsIds = new LinkedList<String>();
        for (Sample s : sampleWithSpace2.getParents())
        {
            parentsIds.add(s.getPermId().getPermId());
        }
        Collections.sort(parentsIds);
        assertEquals(parentsIds.toString(), "[200811050917877-331, 200902091219327-1025]");

        Sample sampleWithoutSpace = samples.get(2);
        assertEquals(sampleWithoutSpace.getCode(), "SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getSampleType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithoutSpace.getSpace(), null);
        assertEquals(sampleWithoutSpace.getIdentifier().getIdentifier(), "/SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getRegistrator().getUserId(), TEST_USER);

        SampleFetchOptions onlyParentsAndChildren = new SampleFetchOptions();
        onlyParentsAndChildren.fetchParents();
        onlyParentsAndChildren.fetchChildren();
        samples = v3api.listSamples(sessionToken, sample2Parents, onlyParentsAndChildren);
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContainsString(sample.getParents(), sampleWithoutSpace.getPermId().getPermId());
            AssertionUtil.assertCollectionContainsString(sample.getChildren(), sampleWithSpace2.getPermId().getPermId());
        }
    }

    public void testCreateSampleWithCircularDependency()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        List<SamplePermId> parentPermId = v3api.createSamples(sessionToken, Arrays.asList(sampleParent));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILD");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));
        sampleChild.setParentIds(parentPermId);

        SampleCreation sampleGrandChild = new SampleCreation();
        sampleGrandChild.setCode("SAMPLE_GRAND_CHILD");
        sampleGrandChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleGrandChild.setSpaceId(new SpacePermId("CISD"));
        sampleGrandChild.setParentIds(Arrays.asList(sampleChild.getCreationId()));
        sampleGrandChild.setChildIds(parentPermId);

        try
        {
            v3api.createSamples(sessionToken, Arrays.asList(sampleChild, sampleGrandChild));
            fail("Expected user failure exception");
        } catch (UserFailureException ufe)
        {
            AssertionUtil.assertContains("dependency", ufe.getMessage());
        }
    }

    @Test(dataProvider = "tf-ft-tt", enabled = false)
    public void testCreateSampleWithInconsistentContainer(boolean setSubSample, boolean setOtherContainer)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation container1 = new SampleCreation();
        container1.setCode("SAMPLE_CONTAINER_1");
        container1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        container1.setSpaceId(new SpacePermId("CISD"));
        container1.setCreationId(new CreationId("cont1"));

        SampleCreation container2 = new SampleCreation();
        container2.setCode("SAMPLE_CONTAINER_2");
        container2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        container2.setSpaceId(new SpacePermId("CISD"));
        container2.setCreationId(new CreationId("cont2"));

        SampleCreation subSample = new SampleCreation();
        subSample.setCode("SAMPLE_SUB_SAMPLE");
        subSample.setTypeId(new EntityTypePermId("CELL_PLATE"));
        subSample.setSpaceId(new SpacePermId("CISD"));
        subSample.setCreationId(new CreationId("subSample"));

        container1.setContainedIds(Arrays.asList(subSample.getCreationId()));

        if (setSubSample)
        {
            subSample.setContainerId(container2.getCreationId());
        }

        if (setOtherContainer)
        {
            container2.setContainedIds(Arrays.asList(subSample.getCreationId()));
        }

        try
        {
            v3api.createSamples(sessionToken, Arrays.asList(container1, container2, subSample));
            fail("Expected user failure exception");
        } catch (UserFailureException ufe)
        {
            AssertionUtil.assertContains("Inconsistent container", ufe.getMessage());
        }
    }

    @Test
    public void testCreateSampleWithChildrenViolatingBusinessRules()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));

        sampleChild.setParentIds(Arrays.asList(sampleParent.getCreationId()));

        try
        {
            v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));
            fail("Expected user failure exception");
        } catch (UserFailureException ufe)
        {
            AssertionUtil.assertContains("can not be a space sample because of a child database instance sample", ufe.getMessage());
        }
    }

    @Test
    public void testCreateSampleWithExistingParentViolatingBusinessRules()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));

        sampleChild.setParentIds(Arrays.asList(new SampleIdentifier("/CISD/MP002-1")));

        try
        {
            v3api.createSamples(sessionToken, Arrays.asList(sampleChild));
            fail("Expected user failure exception");
        } catch (UserFailureException ufe)
        {
            AssertionUtil.assertContains("The database instance sample '/SAMPLE_CHILDREN' can not be child of the space sample '/CISD/MP002-1'",
                    ufe.getMessage());
        }
    }

    @Test
    public void testAssignContainerToCreatedSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = createSimpleSample("SAMPLE_1");
        SampleCreation sc2 = createSimpleSample("SAMPLE_2");

        sc2.setContainerId(new SampleIdentifier("/CISD/SAMPLE_1"));

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchContainer();

        List<Sample> samples = v3api.listSamples(sessionToken, sampleIds, fetchOptions);

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testAssignContainerWithCreationId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = createSimpleSample("SAMPLE_1");
        SampleCreation sc2 = createSimpleSample("SAMPLE_2");

        sc2.setContainerId(sc1.getCreationId());

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchContainer();

        List<Sample> samples = v3api.listSamples(sessionToken, sampleIds, fetchOptions);

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateSampleWithCircularContainerDependency()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sample1 = createSimpleSample("SAMPLE_1");
        SampleCreation sample2 = createSimpleSample("SAMPLE_2");
        SampleCreation sample3 = createSimpleSample("SAMPLE_3");

        sample2.setContainerId(sample1.getCreationId());
        sample3.setContainerId(sample2.getCreationId());
        sample1.setContainerId(sample3.getCreationId());

        try
        {
            v3api.createSamples(sessionToken, Arrays.asList(sample1, sample2, sample3));
            Assert.fail("Expected user failure exception");
        } catch (UserFailureException ufe)
        {
            AssertionUtil.assertContains("cannot be it's own container. (Context: [])", ufe.getMessage());
        }
    }

    @Test
    public void testCreateSampleWithoutCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation sample = createSimpleSample(null);
        try
        {
            v3api.createSamples(sessionToken, Arrays.asList(sample));
            fail("Expected user failure exception");
        } catch (UserFailureException ufe)
        {
            AssertionUtil.assertContains("No code for sample provided", ufe.getMessage());
        }
    }

    private SampleCreation createSimpleSample(String code)
    {
        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode(code);
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("creation " + code));
        return sampleParent;
    }

}
