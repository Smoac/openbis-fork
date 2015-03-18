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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;
import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionSize;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.attachment.AttachmentFileName;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagCode;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class UpdateSampleTest extends AbstractSampleTest
{

    @Test
    public void testUpdateWithSampleExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));
        update.setProperty("COMMENT", "test update");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(sample.getProperties().size(), 1);
        assertEquals(sample.getProperties().get("COMMENT"), "test update");
    }

    @Test
    public void testUpdateWithSampleNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId sampleId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, sampleId);
    }

    @Test
    public void testUpdateWithSampleUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId sampleId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, sampleId);
    }

    @Test
    public void testUpdateWithSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().getIdentifier(), "/TEST-SPACE/SAMPLE");
    }

    @Test
    public void testUpdateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceNullForSpaceSampleAsAdminUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(null);
        update.setExperimentId(null);

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace(), null);
        assertEquals(sample.getExperiment(), null);
        assertSampleIdentifier(sample, "/SAMPLE");
    }

    @Test
    public void testUpdateWithSpaceNullForSpaceSampleAsSpaceUser()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(null);
        update.setExperimentId(null);

        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            });
    }

    @Test
    public void testUpdateWithSpaceNullForSpaceSampleWithDataSets()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));
        update.setSpaceId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "Cannot detach the sample '/CISD/CP-TEST-1' from the space because there are already datasets attached to the sample");
    }

    @Test
    public void testUpdateWithSpaceNotNullForSharedSampleAsAdminUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertSampleIdentifier(sample, "/TEST-SPACE/SAMPLE");
    }

    @Test
    public void testUpdateWithSpaceNotNullForSharedSampleAsSpaceUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken2 = v3api.login(TEST_SPACE_USER, PASSWORD);
                    v3api.updateSamples(sessionToken2, Arrays.asList(update));
                }
            }, ids.get(0));
    }

    @Test
    public void testUpdateWithExperimentInTheSameSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(new ExperimentPermId("200811050952663-1029"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP10");
    }

    @Test
    public void testUpdateWithExperimentInDifferentSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        try
        {
            v3api.updateSamples(sessionToken, Arrays.asList(update));
        } catch (UserFailureException e)
        {
            Assert.assertEquals(
                    "Sample space must be the same as experiment space. Sample: /CISD/SAMPLE, Experiment: /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST (Context: [verify experiment for sample SAMPLE])",
                    e.getMessage());
        }
    }

    @Test
    public void testUpdateWithExperimentForSharedSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        try
        {
            v3api.updateSamples(sessionToken, Arrays.asList(update));
        } catch (UserFailureException e)
        {
            Assert.assertEquals(
                    "Shared samples cannot be attached to experiments. Sample: /SAMPLE, Experiment: /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST (Context: [verify experiment for sample SAMPLE])",
                    e.getMessage());
        }
    }

    @Test
    public void testUpdateWithExperimentNullForSampleWithoutDataSets()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentPermId("200811050952663-1029"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(null);

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        Assert.assertNull(sample.getExperiment());
    }

    @Test
    public void testUpdateWithExperimentNullForSampleWithDataSets()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));
        update.setExperimentId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "Cannot detach the sample '/CISD/CP-TEST-1' from the experiment because there are already datasets attached to the sample");
    }

    @Test
    public void testUpdateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, experimentId);
    }

    @Test
    public void testUpdateWithExperimentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, experimentId);
    }

    @Test
    public void testUpdateWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("COMMENT", "comment 1");
        creation.setProperty("SIZE", "1");
        creation.setProperty("ORGANISM", "GORILLA");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        // change existing property
        update.setProperty("COMMENT", "comment 2");
        // remove existing property
        update.setProperty("ORGANISM", null);
        // remove non existing property
        update.setProperty("ANY_MATERIAL", null);
        // set new property
        update.setProperty("BACTERIUM", "BACTERIUM1 (BACTERIUM)");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);

        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("COMMENT", "comment 2");
        expectedProperties.put("SIZE", "1");
        expectedProperties.put("BACTERIUM", "BACTERIUM1 (BACTERIUM)");
        assertEquals(sample.getProperties(), expectedProperties);
    }

    @Test
    public void testUpdateWithContainer()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation containerCreation = masterPlateCreation("CISD", "TEST_CONTAINER");
        SampleCreation containedCreation = wellCreation("CISD", "TEST_CONTAINED");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(containerCreation, containedCreation));

        SamplePermId containerId = ids.get(0);
        SamplePermId containedId = ids.get(1);

        SampleUpdate updateContained = new SampleUpdate();
        updateContained.setSampleId(containedId);
        updateContained.setContainerId(containerId);

        v3api.updateSamples(sessionToken, Arrays.asList(updateContained));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainer();
        fetchOptions.withContained();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample container = samples.get(0);
        Sample contained = samples.get(1);

        assertSampleIdentifier(container, "/CISD/TEST_CONTAINER");
        assertCollectionContainsOnly(container.getContained(), contained);

        assertSampleIdentifier(contained, "/CISD/TEST_CONTAINER:TEST_CONTAINED");
        assertEquals(contained.getContainer(), container);
    }

    @Test
    public void testUpdateWithContainerUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setContainerId(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, containerId);
    }

    @Test
    public void testUpdateWithContainerNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setContainerId(containerId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, containerId);
    }

    @Test
    public void testUpdateWithContainerCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050919915-8"));
        update.setContainerId(new SamplePermId("200811050919915-9"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "'/CISD/A01:CL1' cannot be it's own container");
    }

    @Test
    public void testUpdateWithContainerViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/MP"));
        update.setContainerId(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "The database instance sample '/3V-125:MP' can not be contained in the space sample '/CISD/3V-125");
    }

    @Test
    public void testUpdateWithContainedSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation contained1Creation = masterPlateCreation("CISD", "TEST_CONTAINED_1");
        SampleCreation contained2Creation = masterPlateCreation("CISD", "TEST_CONTAINED_2");
        SampleCreation contained3Creation = masterPlateCreation("CISD", "TEST_CONTAINED_3");
        contained1Creation.setCreationId(new CreationId("CONTAINED_1"));
        contained2Creation.setCreationId(new CreationId("CONTAINED_2"));
        contained3Creation.setCreationId(new CreationId("CONTAINED_3"));

        SampleCreation container1Creation = masterPlateCreation("CISD", "TEST_CONTAINER_1");
        SampleCreation container2Creation = masterPlateCreation("CISD", "TEST_CONTAINER_2");
        container1Creation.setContainedIds(Arrays.asList(new CreationId("CONTAINED_3")));
        container2Creation.setContainedIds(Arrays.asList(new CreationId("CONTAINED_1"), new CreationId("CONTAINED_2")));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken,
                        Arrays.asList(container1Creation, container2Creation, contained1Creation, contained2Creation, contained3Creation));

        SamplePermId container1Id = ids.get(0);
        SamplePermId container2Id = ids.get(1);
        SamplePermId contained1Id = ids.get(2);
        SamplePermId contained2Id = ids.get(3);
        SamplePermId contained3Id = ids.get(4);

        SampleUpdate updateContainer1 = new SampleUpdate();
        updateContainer1.setSampleId(container1Id);
        // change from [contained3] to [contained2]
        updateContainer1.getContainedIds().set(contained2Id);

        SampleUpdate updateContainer2 = new SampleUpdate();
        updateContainer2.setSampleId(container2Id);
        // change from [contained1, contained2] to [contained1, contained3]
        updateContainer2.getContainedIds().remove(contained2Id);
        updateContainer2.getContainedIds().add(contained3Id);
        // check that adding a contained twice does not break anything
        updateContainer2.getContainedIds().add(contained1Id);

        v3api.updateSamples(sessionToken, Arrays.asList(updateContainer1, updateContainer2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainer();
        fetchOptions.withContained();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 5);

        Sample container1 = samples.get(0);
        Sample container2 = samples.get(1);
        Sample contained1 = samples.get(2);
        Sample contained2 = samples.get(3);
        Sample contained3 = samples.get(4);

        assertSampleIdentifier(container1, "/CISD/TEST_CONTAINER_1");
        assertCollectionContainsOnly(container1.getContained(), contained2);

        assertSampleIdentifier(container2, "/CISD/TEST_CONTAINER_2");
        assertCollectionContainsOnly(container2.getContained(), contained1, contained3);

        assertSampleIdentifier(contained1, "/CISD/TEST_CONTAINER_2:TEST_CONTAINED_1");
        assertEquals(contained1.getContainer(), container2);

        assertSampleIdentifier(contained2, "/CISD/TEST_CONTAINER_1:TEST_CONTAINED_2");
        assertEquals(contained2.getContainer(), container1);

        assertSampleIdentifier(contained3, "/CISD/TEST_CONTAINER_2:TEST_CONTAINED_3");
        assertEquals(contained3.getContainer(), container2);
    }

    @Test
    public void testUpdateWithContainedUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containedId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getContainedIds().add(containedId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, containedId);
    }

    @Test
    public void testUpdateWithContainedNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containedId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getContainedIds().add(containedId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, containedId);
    }

    @Test
    public void testUpdateWithContainedCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050919915-9"));
        update.getContainedIds().add(new SamplePermId("200811050919915-8"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "'/CISD/CL1:A01' cannot be it's own container");
    }

    @Test
    public void testUpdateWithContainedViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/3V-125"));
        update.getContainedIds().add(new SampleIdentifier("/MP"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "Sample '/CISD/3V-125' can not be a space sample because of a contained database instance sample '/3V-125:MP");
    }

    @Test
    public void testUpdateWithParentSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation parent1Creation = masterPlateCreation("CISD", "TEST_PARENT_1");
        SampleCreation parent2Creation = masterPlateCreation("CISD", "TEST_PARENT_2");
        SampleCreation parent3Creation = masterPlateCreation("CISD", "TEST_PARENT_3");
        parent1Creation.setCreationId(new CreationId("PARENT_1"));
        parent2Creation.setCreationId(new CreationId("PARENT_2"));
        parent3Creation.setCreationId(new CreationId("PARENT_3"));

        SampleCreation child1Creation = masterPlateCreation("CISD", "TEST_CHILD_1");
        SampleCreation child2Creation = masterPlateCreation("CISD", "TEST_CHILD_2");
        child1Creation.setParentIds(Arrays.asList(new CreationId("PARENT_3")));
        child2Creation.setParentIds(Arrays.asList(new CreationId("PARENT_1"), new CreationId("PARENT_2")));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken, Arrays.asList(child1Creation, child2Creation, parent1Creation, parent2Creation, parent3Creation));

        SamplePermId child1Id = ids.get(0);
        SamplePermId child2Id = ids.get(1);
        SamplePermId parent1Id = ids.get(2);
        SamplePermId parent2Id = ids.get(3);
        SamplePermId parent3Id = ids.get(4);

        SampleUpdate updateChild1 = new SampleUpdate();
        updateChild1.setSampleId(child1Id);
        // change from [parent3] to [parent1, parent2]
        updateChild1.getParentIds().set(parent1Id, parent2Id);

        SampleUpdate updateChild2 = new SampleUpdate();
        updateChild2.setSampleId(child2Id);
        // change from [parent1, parent2] to [parent2, parent3]
        updateChild2.getParentIds().remove(parent1Id);
        updateChild2.getParentIds().add(parent3Id);
        // check that adding a parent twice does not break anything
        updateChild2.getParentIds().add(parent2Id);

        v3api.updateSamples(sessionToken, Arrays.asList(updateChild1, updateChild2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParents();
        fetchOptions.withChildren();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 5);

        Sample child1 = samples.get(0);
        Sample child2 = samples.get(1);
        Sample parent1 = samples.get(2);
        Sample parent2 = samples.get(3);
        Sample parent3 = samples.get(4);

        assertSampleIdentifier(child1, "/CISD/TEST_CHILD_1");
        assertCollectionContainsOnly(child1.getParents(), parent1, parent2);

        assertSampleIdentifier(child2, "/CISD/TEST_CHILD_2");
        assertCollectionContainsOnly(child2.getParents(), parent2, parent3);

        assertSampleIdentifier(parent1, "/CISD/TEST_PARENT_1");
        assertCollectionContainsOnly(parent1.getChildren(), child1);

        assertSampleIdentifier(parent2, "/CISD/TEST_PARENT_2");
        assertCollectionContainsOnly(parent2.getChildren(), child1, child2);

        assertSampleIdentifier(parent3, "/CISD/TEST_PARENT_3");
        assertCollectionContainsOnly(parent3.getChildren(), child2);
    }

    @Test
    public void testUpdateWithParentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getParentIds().add(parentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, parentId);
    }

    @Test
    public void testUpdateWithParentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getParentIds().add(parentId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, parentId);
    }

    @Test
    public void testUpdateWithParentCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050945092-976"));
        update.getParentIds().add(new SamplePermId("200811050946559-982"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "Circular dependency found: /CISD/3VCP8");
    }

    @Test
    public void testUpdateWithParentViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/MP"));
        update.getParentIds().add(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "The database instance sample '/MP' can not be child of the space sample '/CISD/3V-125'");
    }

    @Test
    public void testUpdateWithChildSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation child1Creation = masterPlateCreation("CISD", "TEST_CHILD_1");
        SampleCreation child2Creation = masterPlateCreation("CISD", "TEST_CHILD_2");
        SampleCreation child3Creation = masterPlateCreation("CISD", "TEST_CHILD_3");
        child1Creation.setCreationId(new CreationId("CHILD_1"));
        child2Creation.setCreationId(new CreationId("CHILD_2"));
        child3Creation.setCreationId(new CreationId("CHILD_3"));

        SampleCreation parent1Creation = masterPlateCreation("CISD", "TEST_PARENT_1");
        SampleCreation parent2Creation = masterPlateCreation("CISD", "TEST_PARENT_2");
        parent1Creation.setChildIds(Arrays.asList(new CreationId("CHILD_3")));
        parent2Creation.setChildIds(Arrays.asList(new CreationId("CHILD_1"), new CreationId("CHILD_2")));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken, Arrays.asList(parent1Creation, parent2Creation, child1Creation, child2Creation, child3Creation));

        SamplePermId parent1Id = ids.get(0);
        SamplePermId parent2Id = ids.get(1);
        SamplePermId child1Id = ids.get(2);
        SamplePermId child2Id = ids.get(3);
        SamplePermId child3Id = ids.get(4);

        SampleUpdate updateParent1 = new SampleUpdate();
        updateParent1.setSampleId(parent1Id);
        // change from [child3] to [child1, child2]
        updateParent1.getChildIds().set(child1Id, child2Id);

        SampleUpdate updateParent2 = new SampleUpdate();
        updateParent2.setSampleId(parent2Id);
        // change from [child1, child2] to [child2, child3]
        updateParent2.getChildIds().remove(child1Id);
        updateParent2.getChildIds().add(child3Id);
        // check that adding a child twice does not break anything
        updateParent2.getChildIds().add(child2Id);

        v3api.updateSamples(sessionToken, Arrays.asList(updateParent1, updateParent2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParents();
        fetchOptions.withChildren();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 5);

        Sample parent1 = samples.get(0);
        Sample parent2 = samples.get(1);
        Sample child1 = samples.get(2);
        Sample child2 = samples.get(3);
        Sample child3 = samples.get(4);

        assertSampleIdentifier(parent1, "/CISD/TEST_PARENT_1");
        assertCollectionContainsOnly(parent1.getChildren(), child1, child2);

        assertSampleIdentifier(parent2, "/CISD/TEST_PARENT_2");
        assertCollectionContainsOnly(parent2.getChildren(), child2, child3);

        assertSampleIdentifier(child1, "/CISD/TEST_CHILD_1");
        assertCollectionContainsOnly(child1.getParents(), parent1);

        assertSampleIdentifier(child2, "/CISD/TEST_CHILD_2");
        assertCollectionContainsOnly(child2.getParents(), parent1, parent2);

        assertSampleIdentifier(child3, "/CISD/TEST_CHILD_3");
        assertCollectionContainsOnly(child3.getParents(), parent2);
    }

    @Test
    public void testUpdateWithChildUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getChildIds().add(childId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, childId);
    }

    @Test
    public void testUpdateWithChildNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getChildIds().add(childId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, childId);
    }

    @Test
    public void testUpdateWithChildCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050946559-982"));
        update.getChildIds().add(new SamplePermId("200811050945092-976"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "Circular dependency found: /CISD/3VCP8");
    }

    @Test
    public void testUpdateWithChildViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/3V-125"));
        update.getChildIds().add(new SampleIdentifier("/MP"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, "Sample '/CISD/3V-125' can not be a space sample because of a child database instance sample '/MP'");
    }

    @Test
    public void testUpdateWithAttachmentsSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation1 = new AttachmentCreation();
        attachmentCreation1.setFileName("test_file_1");
        attachmentCreation1.setContent(new String("test_content_1").getBytes());

        AttachmentCreation attachmentCreation2 = new AttachmentCreation();
        attachmentCreation2.setFileName("test_file_2");
        attachmentCreation2.setContent(new String("test_content_2").getBytes());

        AttachmentCreation attachmentCreation3 = new AttachmentCreation();
        attachmentCreation3.setFileName("test_file_3");
        attachmentCreation3.setContent(new String("test_content_3").getBytes());

        SampleCreation creation1 = masterPlateCreation("CISD", "SAMPLE_1_WITH_ATTACHMENTS");
        SampleCreation creation2 = masterPlateCreation("CISD", "SAMPLE_2_WITH_ATTACHMENTS");

        creation1.setAttachments(Arrays.asList(attachmentCreation3));
        creation2.setAttachments(Arrays.asList(attachmentCreation1, attachmentCreation2));

        List<SamplePermId> sampleIds =
                v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        SamplePermId sampleId1 = sampleIds.get(0);
        SamplePermId sampleId2 = sampleIds.get(1);

        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(sampleId1);
        // change from [test_file_3] to [test_file_1, test_file_3]
        update1.getAttachments().set(attachmentCreation1, attachmentCreation3);

        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId2);
        // change from [test_file_1, test_file_2] to [test_file_2, test_file_3]
        update2.getAttachments().remove(new AttachmentFileName(attachmentCreation1.getFileName()));
        update2.getAttachments().add(attachmentCreation3);

        v3api.updateSamples(sessionToken, Arrays.asList(update1, update2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withAttachments().withContent();

        Map<ISampleId, Sample> samples = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);

        Map<String, Attachment> attachmentMap1 =
                assertAttachments(samples.get(sampleId1).getAttachments(), attachmentCreation1, attachmentCreation3);

        assertEquals(attachmentMap1.get(attachmentCreation1.getFileName()).getVersion(), Integer.valueOf(1));
        assertEquals(attachmentMap1.get(attachmentCreation3.getFileName()).getVersion(), Integer.valueOf(2));

        Map<String, Attachment> attachmentMap2 =
                assertAttachments(samples.get(sampleId2).getAttachments(), attachmentCreation2, attachmentCreation3);

        assertEquals(attachmentMap2.get(attachmentCreation2.getFileName()).getVersion(), Integer.valueOf(1));
        assertEquals(attachmentMap2.get(attachmentCreation3.getFileName()).getVersion(), Integer.valueOf(1));
    }

    @Test
    public void testUpdateWithTagsWithSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation1 = masterPlateCreation("CISD", "SAMPLE_1_WITH_TAGS");
        SampleCreation creation2 = masterPlateCreation("CISD", "SAMPLE_2_WITH_TAGS");

        ITagId tag1Id = new TagCode("TEST_TAG_1");
        ITagId tag2Id = new TagCode("TEST_TAG_2");
        ITagId tag3Id = new TagCode("TEST_TAG_3");

        creation1.setTagIds(Arrays.asList(tag3Id));
        creation2.setTagIds(Arrays.asList(tag1Id, tag2Id));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(ids.get(0));
        // change from [tag3] to [tag1, tag3]
        update1.getTagIds().set(tag1Id, tag3Id);

        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(ids.get(1));
        // change from [tag1, tag2] to [tag2, tag3]
        update2.getTagIds().remove(tag1Id);
        update2.getTagIds().add(tag3Id);
        // check that adding a tag twice does not break anything
        update2.getTagIds().add(tag2Id);

        v3api.updateSamples(sessionToken, Arrays.asList(update1, update2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withTags();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertSampleIdentifier(sample1, "/CISD/SAMPLE_1_WITH_TAGS");
        assertTags(sample1.getTags(), "/test/TEST_TAG_1", "/test/TEST_TAG_3");

        assertSampleIdentifier(sample2, "/CISD/SAMPLE_2_WITH_TAGS");
        assertTags(sample2.getTags(), "/test/TEST_TAG_2", "/test/TEST_TAG_3");
    }

    @Test
    public void testWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        SamplePermId permId = new SamplePermId("200902091219327-1025");

        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(permId);
        update1.setProperty("ANY_MATERIAL", "BACTERIUM-X (BACTERIUM)");

        v3api.updateSamples(sessionToken, Arrays.asList(update1));

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId), fetchOptions);

        Sample sample = map.get(permId);

        assertEquals(sample.getProperties().get("BACTERIUM"), "BACTERIUM-X (BACTERIUM)");
        assertEquals(sample.getProperties().get("ANY_MATERIAL"), "BACTERIUM-X (BACTERIUM)");

        Map<String, Material> materialProperties = sample.getMaterialProperties();

        Material updatedBacterium = materialProperties.get("ANY_MATERIAL");
        assertEquals(updatedBacterium.getPermId(), new MaterialPermId("BACTERIUM-X", "BACTERIUM"));
    }

}
