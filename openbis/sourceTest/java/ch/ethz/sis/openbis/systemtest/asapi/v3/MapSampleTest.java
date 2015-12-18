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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

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
        SampleIdentifier identifier4 = new SampleIdentifier("/MP");
        SampleIdentifier identifier5 = new SampleIdentifier("/MP:a03");
        SampleIdentifier identifier6 = new SampleIdentifier("/cisd/cl1:A03");
        SampleIdentifier identifier7 = new SampleIdentifier("//CL1:A01");

        List<SampleIdentifier> identifiers = Arrays.asList(identifier1, identifier2, identifier3, identifier4,
                identifier5, identifier6, identifier7);
        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, identifiers, new SampleFetchOptions());

        assertEquals(map.size(), identifiers.size());

        Iterator<Sample> iter = map.values().iterator();
        for (SampleIdentifier identifier : identifiers)
        {
            assertEquals(iter.next().getIdentifier(), normalize(identifier));
        }
        for (SampleIdentifier identifier : identifiers)
        {
            assertEquals(map.get(identifier).getIdentifier(), normalize(identifier));
        }

        v3api.logout(sessionToken);
    }

    private SampleIdentifier normalize(SampleIdentifier identifier)
    {
        ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier identifier2 =
                SampleIdentifierFactory.parse(identifier.getIdentifier());
        String spaceCode = null;
        if (identifier2.isSpaceLevel())
        {
            if (identifier2.isInsideHomeSpace())
            {
                spaceCode = "CISD";
            } else
            {
                spaceCode = CodeConverter.tryToDatabase(identifier2.getSpaceLevel().getSpaceCode());
            }
        }
        String sampleSubCode = CodeConverter.tryToDatabase(identifier2.getSampleSubCode());
        String containerCode = CodeConverter.tryToDatabase(identifier2.tryGetContainerCode());
        return new SampleIdentifier(spaceCode, containerCode, sampleSubCode);

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
        assertEqualsDate(sample.getRegistrationDate(), "2009-02-09 12:09:19");
        assertEqualsDate(sample.getModificationDate(), "2009-08-18 17:54:11");

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertComponentsNotFetched(sample);
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
        fetchOptions.withComponents().withContainer().withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map =
                v3api.mapSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091250077-1050")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getCode(), "PLATE_WELLSEARCH");

        // assert that components / container is fetched
        Assert.assertTrue(sample.getComponents().get(0).getContainer() == sample);

        // assert properties are fetched (original fetch options)
        assertEquals(sample.getProperties().size(), 0);

        // assert that experiment is fetched as well. (fetch options via component's container)
        Experiment experiment = sample.getExperiment();
        assertEquals(experiment.getIdentifier().toString(), "/CISD/DEFAULT/EXP-WELLS");
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithRegistratorAndModifier()
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
        assertComponentsNotFetched(sample);
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
        assertComponentsNotFetched(sample);
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
    public void testMapWithComponentsAndContainer()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.withComponents().withContainer();
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

        List<Sample> components = sample.getComponents();
        assertEquals(components.size(), 2);

        for (Sample s : components)
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

        // fetch components, with the container and loop.
        fetchOptions.withComponents().withContainerUsing(fetchOptions);
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

        List<Sample> components = sample.getComponents();
        assertEquals(components.size(), 2);

        Assert.assertTrue(sample.getComponents().get(0).getContainer() == sample);

        for (Sample s : components)
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
        assertEquals(type.getGeneratedCodePrefix(), "S");
        assertEqualsDate(type.getModificationDate(), "2009-03-23 15:34:44");

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
    public void testMapWithSpaceWithProjectAndExperiments()
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

    @Test
    public void testMapWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        SamplePermId permId = new SamplePermId("200902091219327-1025");

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(permId), fetchOptions);

        Sample sample = map.get(permId);

        assertEquals(sample.getProperties().get("BACTERIUM"), "BACTERIUM-X (BACTERIUM)");
        assertEquals(sample.getProperties().get("ANY_MATERIAL"), "1 (GENE)");

        Map<String, Material> materialProperties = sample.getMaterialProperties();

        Material bacterium = materialProperties.get("BACTERIUM");
        assertEquals(bacterium.getPermId(), new MaterialPermId("BACTERIUM-X", "BACTERIUM"));
        assertEquals(bacterium.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(bacterium);

        Material gene = materialProperties.get("ANY_MATERIAL");
        assertEquals(gene.getPermId(), new MaterialPermId("1", "GENE"));
        assertEquals(gene.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(gene);
    }

    @Test
    public void testMapWithHistoryEmpty()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EMPTY_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<HistoryEntry> history = testMapWithHistory(creation, new SampleUpdate[] {});

        assertEquals(history, Collections.emptyList());
    }

    @Test(enabled = false)
    public void testMapWithHistoryDates()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        Date start = new Date();

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091219327-1025"));
        update.setProperty("SIZE", "12");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withHistory();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(update.getSampleId()), fetchOptions);

        Date end = new Date();

        Sample sample = map.get(update.getSampleId());

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 1);

        PropertyHistoryEntry entry = (PropertyHistoryEntry) history.get(0);
        assertEquals(entry.getPropertyName(), "SIZE");
        assertEquals(entry.getPropertyValue(), "123");

        assertTrue(entry.getValidFrom().after(start));
        assertTrue(entry.getValidFrom().before(end));
        assertTrue(entry.getValidTo().after(entry.getValidFrom()));
        assertTrue(entry.getValidTo().before(end));

        assertHistoryNotFetched(entry);
    }

    @Test
    public void testMapWithHistoryProperty()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_PROPERTY_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("COMMENT", "comment1");

        SampleUpdate update = new SampleUpdate();
        update.setProperty("COMMENT", "comment2");

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        PropertyHistoryEntry entry = (PropertyHistoryEntry) history.get(0);
        assertEquals(entry.getPropertyName(), "COMMENT");
        assertEquals(entry.getPropertyValue(), "comment1");
        assertTrue(entry.getValidFrom() != null);
        assertTrue(entry.getValidTo() != null);
        assertEquals(entry.getAuthor().getUserId(), TEST_USER);
    }

    @Test
    public void testMapWithHistorySystemProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String systemPropertyCode = "$PLATE_GEOMETRY";
        String simplePropertyCode = "PLATE_GEOMETRY";
        String originalSystemPropertyValue = "384_WELLS_16X24";
        String originalSimplePropertyValue = "I'm just random";
        String sampleTypeCode = "MASTER_PLATE";

        createNewPropertyType(sessionToken, sampleTypeCode, simplePropertyCode);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_SYS_PROPERTY");
        creation.setTypeId(new EntityTypePermId(sampleTypeCode));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(systemPropertyCode, originalSystemPropertyValue);
        creation.setProperty(simplePropertyCode, originalSimplePropertyValue);

        SampleUpdate update1 = new SampleUpdate();
        update1.setProperty(systemPropertyCode, "96_WELLS_8X12");

        SampleUpdate update2 = new SampleUpdate();
        update2.setProperty(simplePropertyCode, "I have been updated");

        List<HistoryEntry> history = testMapWithHistory(creation, update1, update2);

        assertEquals(history.size(), 2);

        PropertyHistoryEntry entry1 = (PropertyHistoryEntry) history.get(0);
        PropertyHistoryEntry entry2 = (PropertyHistoryEntry) history.get(1);

        assertEquals(entry1.getPropertyName(), systemPropertyCode);
        assertEquals(entry1.getPropertyValue(), originalSystemPropertyValue + " [PLATE_GEOMETRY]");

        assertEquals(entry2.getPropertyName(), simplePropertyCode);
        assertEquals(entry2.getPropertyValue(), originalSimplePropertyValue);
    }

    @Test
    public void testMapWithHistorySpace()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_SPACE_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        SampleUpdate update = new SampleUpdate();
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.SPACE);
        assertEquals(entry.getRelatedObjectId(), new SpacePermId("CISD"));
    }

    @Test
    public void testMapWithHistoryExperiment()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EXPERIMENT_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        SampleUpdate update = new SampleUpdate();
        update.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP11"));

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.EXPERIMENT);
        assertEquals(entry.getRelatedObjectId(), new ExperimentPermId("200811050951882-1028"));
    }

    @Test
    public void testMapWithHistoryDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATA_SET_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(creation));

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update.setSampleId(permIds.get(0));

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update.setSampleId(null);

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withHistory();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, permIds, fetchOptions);
        assertEquals(map.size(), 1);

        Sample sample = map.get(permIds.get(0));

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.DATA_SET);
        assertEquals(entry.getRelatedObjectId(), new DataSetPermId("COMPONENT_1A"));
    }

    @Test
    public void testMapWithHistoryContainer()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_CONTAINER_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setContainerId(new SampleIdentifier("/CISD/CL1"));

        SampleUpdate update = new SampleUpdate();
        update.setContainerId(null);

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.CONTAINER);
        assertEquals(entry.getRelatedObjectId(), new SamplePermId("200811050919915-8"));
    }

    @Test
    public void testMapWithHistoryComponents()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_COMPONENTS_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setComponentIds(Arrays.asList(new SampleIdentifier("/CISD/CL1")));

        SampleUpdate update = new SampleUpdate();
        update.getComponentIds().set();

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.COMPONENT);
        assertEquals(entry.getRelatedObjectId(), new SamplePermId("200811050919915-8"));
    }

    @Test
    public void testMapWithHistoryParent()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_PARENT_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setParentIds(Arrays.asList(new SampleIdentifier("/CISD/CL1")));

        SampleUpdate update = new SampleUpdate();
        update.getParentIds().set();

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.PARENT);
        assertEquals(entry.getRelatedObjectId(), new SamplePermId("200811050919915-8"));
    }

    @Test
    public void testMapWithHistoryChild()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_CHILD_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setChildIds(Arrays.asList(new SampleIdentifier("/CISD/CL1")));

        SampleUpdate update = new SampleUpdate();
        update.getChildIds().set();

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), SampleRelationType.CHILD);
        assertEquals(entry.getRelatedObjectId(), new SamplePermId("200811050919915-8"));
    }

    private List<HistoryEntry> testMapWithHistory(SampleCreation creation, SampleUpdate... updates)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(creation));

        if (updates != null)
        {
            for (SampleUpdate update : updates)
            {
                update.setSampleId(permIds.get(0));
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withHistory().withAuthor();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, permIds, fetchOptions);

        assertEquals(map.size(), 1);
        Sample sample = map.get(permIds.get(0));

        v3api.logout(sessionToken);

        return sample.getHistory();
    }

    @Test
    public void testMapWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ISampleId id = new SamplePermId("200811050946559-980");

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withAttachments();

        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        Sample sample = map.get(id);

        List<Attachment> attachments = sample.getAttachments();
        assertEquals(attachments.size(), 1);

        Attachment attachment = attachments.get(0);
        assertEquals(attachment.getFileName(), "sampleHistory.txt");

        v3api.logout(sessionToken);
    }

}
