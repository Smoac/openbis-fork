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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class MapSampleTest extends AbstractSampleTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId permId1 = new SamplePermId("200902091219327-1025");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2),
                        new SampleFetchOptions());

        assertEquals(2, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");
        SampleIdentifier identifier3 = new SampleIdentifier("/CISD/3VCP8");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(identifier1, identifier2, identifier3), new SampleFetchOptions());

        assertEquals(3, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier3);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier3).getIdentifier(), identifier3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");
        SampleIdentifier identifier3 = new SampleIdentifier("/NONEXISTENT_SPACE/CP-TEST-1");
        SamplePermId permId1 = new SamplePermId("200902091250077-1026");
        SamplePermId permId2 = new SamplePermId("NONEXISTENT_SAMPLE");
        SampleIdentifier identifier4 = new SampleIdentifier("/CISD/NONEXISTENT_SAMPLE");
        SampleIdentifier identifier5 = new SampleIdentifier("/CISD/3VCP8");
        SamplePermId permId3 = new SamplePermId("200902091225616-1027");

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken,
                        Arrays.asList(identifier1, identifier2, identifier3, permId1, permId2, identifier4, identifier5, permId3),
                        new SampleFetchOptions());

        assertEquals(5, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getIdentifier(), identifier5);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier5).getIdentifier(), identifier5);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDifferent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/CP-TEST-1");
        SamplePermId permId = new SamplePermId("200902091250077-1026");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Arrays.asList(identifier1, permId, identifier2), new SampleFetchOptions());

        assertEquals(3, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId).getPermId(), permId);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // "/CISD/CP-TEST-1" and "200902091219327-1025" is the same sample
        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/CP-TEST-1");
        SamplePermId permId1 = new SamplePermId("200902091219327-1025");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");
        SamplePermId permId2 = new SamplePermId("200902091219327-1025");

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Arrays.asList(identifier1, permId1, identifier2, permId2), new SampleFetchOptions());

        assertEquals(3, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        assertTrue(map.get(identifier1) == map.get(permId1));

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsUnauthorized()
    {
        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");
        SampleIdentifier identifier3 = new SampleIdentifier("/CISD/CP-TEST-2");
        SampleIdentifier identifier4 = new SampleIdentifier("/TEST-SPACE/EV-TEST");

        List<? extends ISampleId> ids = Arrays.asList(identifier1, identifier2, identifier3, identifier4);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, ids, new SampleFetchOptions());

        assertEquals(map.size(), 4);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.mapSamples(sessionToken, ids, new SampleFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier4);

        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier4).getIdentifier(), identifier4);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091219327-1025")),
                        new SampleFetchOptions());
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200902091219327-1025");
        assertEquals(sample.getCode(), "CP-TEST-1");
        assertEquals(sample.getIdentifier().toString(), "/CISD/CP-TEST-1");

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertContainedNotFetched(sample);
        assertContainerNotFetched(sample);
        assertModifierNotFetched(sample);
        assertRegistratorNotFetched(sample);
        assertTagsNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithFetchOptionsNested()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.withContained().withContainer().withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091250077-1050")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getCode(), "PLATE_WELLSEARCH");

        // assert that contained / container is fetched
        Assert.assertTrue(sample.getContained().get(0).getContainer() == sample);

        // assert properties are fetched (original fetch options)
        assertEquals(sample.getProperties().size(), 0);

        // assert that experiment is fetched as well. (fetch options via contained container)
        Experiment experiment = sample.getExperiment();
        assertEquals(experiment.getIdentifier().toString(), "/CISD/DEFAULT/EXP-WELLS");
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation newSample = new SampleCreation();
        newSample.setCode("SAMPLE_WITH_MODIFIER");
        newSample.setTypeId(new EntityTypePermId("CELL_PLATE"));
        newSample.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> newSamplePermIds = v3api.createSamples(sessionToken, Collections.singletonList(newSample));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier().withRegistrator();
        fetchOptions.withRegistrator();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, newSamplePermIds,
                        fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);

        assertEquals(sample.getRegistrator().getUserId(), "test");
        assertEquals(sample.getModifier().getUserId(), "test");

        assertTrue(sample.getRegistrator() == sample.getModifier());

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertContainedNotFetched(sample);
        assertContainerNotFetched(sample);

        assertTagsNotFetched(sample);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithModifierReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();

        SamplePermId permId1 = new SamplePermId("200811050919915-8");
        SamplePermId permId2 = new SamplePermId("200902091219327-1025");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getModifier().getUserId(), "test_role");
        assertEquals(sample2.getModifier().getUserId(), "test_role");
        assertTrue(sample1.getModifier() == sample2.getModifier());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        TagFetchOptions tagfe = fetchOptions.withTags();
        tagfe.withOwner();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("201206191219327-1055")),
                        fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);

        Set<Tag> tags = sample.getTags();

        assertEquals(tags.size(), 1);

        for (Tag tag : tags)
        {
            assertEquals(TEST_USER, tag.getOwner().getUserId());
        }

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertContainedNotFetched(sample);
        assertContainerNotFetched(sample);
        assertRegistratorNotFetched(sample);
        assertModifierNotFetched(sample);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTagsReused()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withTags();

        SamplePermId permId1 = new SamplePermId("201206191219327-1054");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(map.size(), 2);
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getTags().size(), 1);
        assertEquals(sample2.getTags().size(), 1);
        assertContainSameObjects(sample1.getTags(), sample2.getTags(), 1);

        v3api.logout(sessionToken);
    }

    public void testMapWithSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091219327-1025")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "CISD");
        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithSpaceReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();

        SamplePermId permId1 = new SamplePermId("201206191219327-1054");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample2.getSpace().getCode(), "TEST-SPACE");
        assertTrue(sample1.getSpace() == sample2.getSpace());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithParentsAndProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.withParents().withProperties();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050946559-982")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050946559-982");
        assertEquals(sample.getCode(), "3VCP8");
        assertEquals(sample.getIdentifier().toString(), "/CISD/3VCP8");

        assertExperimentNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertPropertiesNotFetched(sample);

        List<Sample> parents = sample.getParents();
        assertEquals(parents.size(), 1);

        Sample parent = parents.get(0);
        assertEquals(parent.getPermId().toString(), "200811050945092-976");
        assertEquals(parent.getCode(), "3V-125");
        assertEquals(parent.getProperties().size(), 1);
        assertEquals(parent.getProperties().get("OFFSET"), "49");
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithParents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("LIST_SAMPLES__SAMPLE");
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setParentIds(Arrays.asList(new CreationId("parent_1"), new CreationId("parent_2")));

        SampleCreation parent1Creation = new SampleCreation();
        parent1Creation.setCreationId(new CreationId("parent_1"));
        parent1Creation.setCode("LIST_SAMPLES__PARENT_1");
        parent1Creation.setSpaceId(new SpacePermId("CISD"));
        parent1Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        parent1Creation.setParentIds(Arrays.asList(new CreationId("grandparent_1"), new CreationId("grandparent_2")));

        SampleCreation parent2Creation = new SampleCreation();
        parent2Creation.setCreationId(new CreationId("parent_2"));
        parent2Creation.setCode("LIST_SAMPLES__PARENT_2");
        parent2Creation.setSpaceId(new SpacePermId("CISD"));
        parent2Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        parent2Creation.setParentIds(Arrays.asList(new CreationId("grandparent_1"), new CreationId("grandparent_2")));

        SampleCreation grandparent1Creation = new SampleCreation();
        grandparent1Creation.setCreationId(new CreationId("grandparent_1"));
        grandparent1Creation.setCode("LIST_SAMPLES__GRANDPARENT_1");
        grandparent1Creation.setSpaceId(new SpacePermId("CISD"));
        grandparent1Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SampleCreation grandparent2Creation = new SampleCreation();
        grandparent2Creation.setCreationId(new CreationId("grandparent_2"));
        grandparent2Creation.setCode("LIST_SAMPLES__GRANDPARENT_2");
        grandparent2Creation.setSpaceId(new SpacePermId("CISD"));
        grandparent2Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParents().withParents();

        List<SamplePermId> sampleIds =
                v3api.createSamples(sessionToken,
                        Arrays.asList(sampleCreation, parent1Creation, parent2Creation, grandparent1Creation, grandparent2Creation));
        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sample = samples.get(0);
        Sample parent1 = samples.get(1);
        Sample parent2 = samples.get(2);
        Sample grandparent1 = samples.get(3);
        Sample grandparent2 = samples.get(4);

        assertEquals(sample.getCode(), sampleCreation.getCode());
        assertEquals(parent1.getCode(), parent1Creation.getCode());
        assertEquals(parent2.getCode(), parent2Creation.getCode());
        assertEquals(grandparent1.getCode(), grandparent1Creation.getCode());
        assertEquals(grandparent2.getCode(), grandparent2Creation.getCode());

        assertTrue(sample.getParents().get(0) == parent1);
        assertTrue(sample.getParents().get(1) == parent2);
        assertTrue(parent1.getParents().get(0) == grandparent1);
        assertTrue(parent1.getParents().get(1) == grandparent2);
        assertTrue(parent2.getParents().get(0) == grandparent1);
        assertTrue(parent2.getParents().get(1) == grandparent2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithParentsReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParents();

        SamplePermId permId1 = new SamplePermId("200811050946559-980");
        SamplePermId permId2 = new SamplePermId("200811050946559-982");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getParents().size(), 2);
        assertEquals(sample2.getParents().size(), 1);
        assertContainSameObjects(sample1.getParents(), sample2.getParents(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.withChildren();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050929940-1019")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050929940-1019");
        assertEquals(sample.getCode(), "CP1-B1");

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);

        List<Sample> children = sample.getChildren();
        assertEquals(children.size(), 1);

        Sample child = children.get(0);
        assertEquals(child.getPermId().toString(), "200811050931564-1022");
        assertEquals(child.getCode(), "RP1-B1X");
        assertPropertiesNotFetched(child);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithChildrenReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();

        SamplePermId permId1 = new SamplePermId("200811050944030-975");
        SamplePermId permId2 = new SamplePermId("200811050945092-976");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getChildren().size(), 1);
        assertEquals(sample2.getChildren().size(), 4);
        assertContainSameObjects(sample1.getChildren(), sample2.getChildren(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithContainer()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.withContained().withContainer();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050919915-8")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050919915-8");
        assertEquals(sample.getCode(), "CL1");

        assertEquals(sample.getProperties().size(), 2);

        assertExperimentNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);

        List<Sample> contained = sample.getContained();
        assertEquals(contained.size(), 2);

        for (Sample s : contained)
        {
            assertExperimentNotFetched(s);
            assertPropertiesNotFetched(s);
            assertParentsNotFetched(s);
            assertChildrenNotFetched(s);
            assertEquals(s.getContainer().getPermId(), sample.getPermId());
        }
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithContainerReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainer();

        SamplePermId permId1 = new SamplePermId("200811050919915-9");
        SamplePermId permId2 = new SamplePermId("200811050919915-10");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getContainer().getCode(), "CL1");
        assertEquals(sample2.getContainer().getCode(), "CL1");
        assertTrue(sample1.getContainer() == sample2.getContainer());

        v3api.logout(sessionToken);
    }

    /**
     * Test that translation can handle reference loops
     */
    @Test
    public void testMapWithContainerLoop()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch contained, with the container and loop.
        fetchOptions.withContained().withContainerUsing(fetchOptions);
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050919915-8")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050919915-8");
        assertEquals(sample.getCode(), "CL1");

        assertEquals(sample.getProperties().size(), 2);

        assertExperimentNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);

        List<Sample> contained = sample.getContained();
        assertEquals(contained.size(), 2);

        Assert.assertTrue(sample.getContained().get(0).getContainer() == sample);

        for (Sample s : contained)
        {
            assertExperimentNotFetched(s);
            assertPropertiesNotFetched(s);
            assertParentsNotFetched(s);
            assertChildrenNotFetched(s);
            assertSpaceNotFetched(sample);
        }
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Arrays.asList(new SamplePermId("200811050946559-979"), new SampleIdentifier("/CISD/RP1-B1X"),
                        new SampleIdentifier("/CISD/RP2-A1X")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(samples.size(), 3);

        Sample sample1 = samples.get(0);
        assertEquals(sample1.getIdentifier().toString(), "/CISD/3VCP5");
        assertEquals(sample1.getExperiment().getIdentifier().toString(), "/CISD/NEMO/EXP10");

        Sample sample2 = samples.get(1);
        assertEquals(sample2.getIdentifier().toString(), "/CISD/RP1-B1X");
        assertEquals(sample2.getExperiment().getIdentifier().toString(), "/CISD/DEFAULT/EXP-REUSE");

        Sample sample3 = samples.get(2);
        assertEquals(sample3.getIdentifier().toString(), "/CISD/RP2-A1X");
        assertEquals(sample3.getExperiment().getIdentifier().toString(), "/CISD/DEFAULT/EXP-REUSE");

        assertTrue(sample2.getExperiment() == sample3.getExperiment());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithExperimentReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();

        SamplePermId permId1 = new SamplePermId("201206191219327-1054");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());

        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getExperiment().getCode(), "EXP-SPACE-TEST");
        assertEquals(sample2.getExperiment().getCode(), "EXP-SPACE-TEST");
        assertTrue(sample1.getExperiment() == sample2.getExperiment());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.withType();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050946559-979")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(samples.size(), 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().toString(), "/CISD/3VCP5");

        SampleType type = sample.getType();
        assertEquals(type.getCode(), "CELL_PLATE");
        assertEquals(type.getPermId().getPermId(), "CELL_PLATE");
        assertEquals(type.getDescription(), "Cell Plate");
        assertTrue(type.isListable());
        assertFalse(type.isAutoGeneratedCode());
        assertFalse(type.isShowParentMetadata());
        assertFalse(type.isSubcodeUnique());

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertSpaceNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTypeReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();

        SamplePermId permId1 = new SamplePermId("200902091219327-1025");
        SamplePermId permId2 = new SamplePermId("200902091250077-1026");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getType().getCode(), "CELL_PLATE");
        assertEquals(sample2.getType().getCode(), "CELL_PLATE");
        assertTrue(sample1.getType() == sample2.getType());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithDataSetAndItsTypeReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withDataSets().withType();

        SamplePermId permId1 = new SamplePermId("200902091225616-1027");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1), fetchOptions);

        assertEquals(1, map.size());
        Sample sample1 = map.get(permId1);

        List<DataSet> dataSets = sample1.getDataSets();
        AssertionUtil.assertCollectionSize(dataSets, 2);

        DataSet ds1 = dataSets.get(0);
        DataSet ds2 = dataSets.get(1);

        assertFalse(ds1 == ds2);
        assertEquals(ds1.getType().getCode(), "HCS_IMAGE");
        assertEquals(ds2.getType().getCode(), "HCS_IMAGE");
        assertTrue(ds1.getType() == ds2.getType());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithDataSetsInCircularFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withDataSets().withSample().withDataSets().withType();

        SamplePermId permId1 = new SamplePermId("200902091225616-1027");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId1), fetchOptions);

        assertEquals(1, map.size());
        Sample sample1 = map.get(permId1);

        List<DataSet> dataSets = sample1.getDataSets();
        AssertionUtil.assertCollectionSize(dataSets, 2);

        DataSet ds1 = dataSets.get(0);
        assertEquals(ds1.getType().getCode(), "HCS_IMAGE");
        assertTrue(ds1.getSample() == sample1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testComplexWithSpaceWithProjectAndExperiments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace().withProjects().withExperiments();

        SamplePermId permId = new SamplePermId("200902091250077-1060");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId), fetchOptions);

        List<Project> totalProjects = new ArrayList<Project>();
        List<Experiment> totalExperiments = new ArrayList<Experiment>();

        assertEquals(1, map.size());
        Sample sample = map.get(permId);

        for (Project p : sample.getSpace().getProjects())
        {
            totalProjects.add(p);
            totalExperiments.addAll(p.getExperiments());
        }

        Collection<String> projectCodes = CollectionUtils.collect(totalProjects, new Transformer<Project, String>()
            {
                @Override
                public String transform(Project input)
                {
                    return input.getCode();
                }
            });
        Collection<String> experimentCodes = CollectionUtils.collect(totalExperiments, new Transformer<Experiment, String>()
            {
                @Override
                public String transform(Experiment input)
                {
                    return input.getCode();
                }
            });

        AssertionUtil.assertCollectionContainsOnly(projectCodes, "TEST-PROJECT", "NOE", "PROJECT-TO-DELETE");
        AssertionUtil.assertCollectionContainsOnly(experimentCodes, "EXP-SPACE-TEST", "EXP-TEST-2", "EXPERIMENT-TO-DELETE");

        v3api.logout(sessionToken);
    }

}
