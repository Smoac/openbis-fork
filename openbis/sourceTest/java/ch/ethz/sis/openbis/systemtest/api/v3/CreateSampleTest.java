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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class CreateSampleTest extends AbstractSampleTest
{

    @Test
    public void testCreateSampleWithoutCode()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateSampleWithExistingCode()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample = sampleCreation("SAMPLE_WITH_EXISTING_CODE");
        v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Insert/Update of sample (code: SAMPLE_WITH_EXISTING_CODE) failed because sample with the same code already exists");
    }

    @Test
    public void testCreateSampleWithIncorrectCode()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation("?!*");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "The code '?!*' contains illegal characters");
    }

    @Test
    public void testCreateSampleWithoutType()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITHOUT_TYPE");
        sample.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Type id cannot be null");
    }

    @Test
    public void testCreateSampleWithNonexistentType()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final SampleCreation sample = new SampleCreation();
        sample.setTypeId(typeId);
        sample.setCode("SAMPLE_WITH_NONEXISTENT_TYPE");
        sample.setSpaceId(new SpacePermId("CISD"));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, typeId);
    }

    @Test
    public void testCreateSampleWithNonexistentPropertyCode()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_NONEXISTENT_PROPERTY_CODE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(creation));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testCreateSampleWithIncorrectVocabularyPropertyValue()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCORRECT_PROPERTY_VALUE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("ORGANISM", "NON_EXISTENT_ORGANISM");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(creation));
                }
            }, "Vocabulary value 'NON_EXISTENT_ORGANISM' is not valid. It must exist in 'ORGANISM' controlled vocabulary");
    }

    @Test
    public void testCreateSampleWithoutMandatoryPropertyValue()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EMPTY_MANDATORY_PROPERTY");
        creation.setTypeId(new EntityTypePermId("CONTROL_LAYOUT"));
        creation.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(creation));
                }
            }, "Value of mandatory property '$PLATE_GEOMETRY' not specified");
    }

    @Test
    public void testCreateSampleWithoutSpaceButWithExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Shared samples cannot be attached to experiments");
    }

    @Test
    public void testCreateSampleWithoutSpaceAsAdminUser()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchSpace();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, permIds, fetchOptions);
        Sample sample = map.values().iterator().next();

        assertEquals(sample.getCode(), "SHARED_SAMPLE_TEST");
        assertNull(sample.getSpace());
    }

    @Test
    public void testCreateSampleWithoutSpaceAsSpaceUser()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, new SampleIdentifier("/SHARED_SAMPLE_TEST"));
    }

    @Test
    public void testCreateSampleWithUnauthorizedSpace()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("UNAUTHORIZED_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, spaceId);
    }

    @Test
    public void testCreateSampleWithNonexistentSpace()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("NONEXISTENT_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, spaceId);
    }

    @Test
    public void testCreateSampleWithInconsistentSpace()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Sample space must be the same as experiment space");
    }

    @Test
    public void testCreateSampleWithUnauthorizedExperiment()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("UNAUTHORIZED_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, experimentId);
    }

    @Test
    public void testCreateSampleWithNonexistentExperiment()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("NONEXISTENT_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, experimentId);
    }

    @Test
    public void testCreateSampleWithUnauthorizedParent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = createCisdSample("PARENT_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_PARENT");
        creation.setParentIds(Collections.singletonList(parentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, parentId);
    }

    @Test
    public void testCreateSampleWithNonexistentParent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_PARENT");
        creation.setParentIds(Collections.singletonList(parentId));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, parentId);
    }

    @Test
    public void testCreateSampleWithUnauthorizedChild()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = createCisdSample("CHILD_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CHILD");
        creation.setChildIds(Collections.singletonList(childId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, childId);
    }

    @Test
    public void testCreateSampleWithNonexistentChild()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CHILD");
        creation.setChildIds(Collections.singletonList(childId));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, childId);
    }

    @Test
    public void testCreateSampleWithUnauthorizedContainer()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = createCisdSample("CONTAINER_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CONTAINER");
        creation.setContainerId(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, containerId);
    }

    @Test
    public void testCreateSampleWithNonExistentContainer()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CONTAINER");
        creation.setContainerId(containerId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, containerId);
    }

    @Test
    public void testCreateSampleWithUnauthorizedContained()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containedId = createCisdSample("CONTAINED_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CONTAINED");
        creation.setContainedIds(Collections.singletonList(containedId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, containedId);
    }

    @Test
    public void testCreateSampleWithNonexistentContained()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containedId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CONTAINED");
        creation.setContainedIds(Collections.singletonList(containedId));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, containedId);
    }

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

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

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

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

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
        fetchOptions.fetchType();
        fetchOptions.fetchSpace();
        fetchOptions.fetchRegistrator();
        fetchOptions.fetchAttachments().fetchContent();
        fetchOptions.fetchContainer();
        fetchOptions.fetchTags();
        fetchOptions.fetchParents();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sampleWithSpace1 = samples.get(0);
        assertEquals(sampleWithSpace1.getCode(), "SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getType().getCode(), "CELL_PLATE");
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
        assertEquals(sampleWithSpace2.getType().getCode(), "CELL_PLATE");
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
        assertEquals(sampleWithoutSpace.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithoutSpace.getSpace(), null);
        assertEquals(sampleWithoutSpace.getIdentifier().getIdentifier(), "/SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getRegistrator().getUserId(), TEST_USER);

        SampleFetchOptions onlyParentsAndChildren = new SampleFetchOptions();
        onlyParentsAndChildren.fetchParents();
        onlyParentsAndChildren.fetchChildren();

        map = v3api.mapSamples(sessionToken, sample2Parents, onlyParentsAndChildren);
        samples = new ArrayList<Sample>(map.values());

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
    public void testCreateSampleSetContainerToCreatedSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = sampleCreation("SAMPLE_1");
        SampleCreation sc2 = sampleCreation("SAMPLE_2");

        sc2.setContainerId(new SampleIdentifier("/CISD/SAMPLE_1"));

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchContainer();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateSampleSetContainerWithCreationId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = sampleCreation("SAMPLE_1");
        SampleCreation sc2 = sampleCreation("SAMPLE_2");

        sc2.setContainerId(sc1.getCreationId());

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchContainer();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateSampleWithCircularContainerDependency()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sample1 = sampleCreation("SAMPLE_1");
        SampleCreation sample2 = sampleCreation("SAMPLE_2");
        SampleCreation sample3 = sampleCreation("SAMPLE_3");

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

    private SampleCreation sampleCreation(String code)
    {
        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode(code);
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("creation " + code));
        return sampleParent;
    }

    private SampleCreation sampleCreation(String spaceCode, String code)
    {
        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode(code);
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId(spaceCode));
        sampleParent.setCreationId(new CreationId("creation " + code));
        return sampleParent;
    }

    private SamplePermId createCisdSample(String code)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation = sampleCreation("CISD", code);
        List<SamplePermId> ids = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        v3api.logout(sessionToken);
        return ids.get(0);
    }

}
