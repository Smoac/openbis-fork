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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;

/**
 * @author pkupczyk
 */
public class SampleListTest extends AbstractSampleTest
{

    @Test
    public void testListSamplesWithoutFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091219327-1025")),
                        new SampleFetchOptions());
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
    public void testListSamplesWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);

        SampleCreation newSample = new SampleCreation();
        newSample.setCode("SAMPLE_WITH_MODIFIER");
        newSample.setTypeId(new EntityTypePermId("CELL_PLATE"));
        newSample.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> newSamplePermIds = v3api.createSamples(sessionToken, Collections.singletonList(newSample));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchModifier().fetchRegistrator();
        fetchOptions.fetchRegistrator();
        List<Sample> samples =
                v3api.listSamples(sessionToken, newSamplePermIds,
                        fetchOptions);

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
    public void testListSampleWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        TagFetchOptions tagfe = fetchOptions.fetchTags();
        tagfe.fetchOwner();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("201206191219327-1055")),
                        fetchOptions);

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

    public void testListSamplesWithSpace()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.fetchSpace();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091219327-1025")), fetchOptions);
        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "CISD");
        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testListSamplesWithParentsAndProperties()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.fetchParents().fetchProperties();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050946559-982")), fetchOptions);
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
    public void testListSamplesWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.fetchChildren();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050929940-1019")), fetchOptions);
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
    public void testListSamplesWithContainer()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.fetchContained().fetchContainer();
        fetchOptions.fetchProperties();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050919915-8")), fetchOptions);
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

    /**
     * Test that translation can handle reference loops
     */
    @Test
    public void testListSamplesWithContainerLoop()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch contained, with the container and loop.
        fetchOptions.fetchContained().fetchContainer(fetchOptions);
        fetchOptions.fetchProperties();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050919915-8")), fetchOptions);
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
    public void testListSamplesWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.fetchExperiment();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050946559-979")), fetchOptions);
        assertEquals(samples.size(), 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().toString(), "/CISD/3VCP5");

        Experiment experiment = sample.getExperiment();

        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP10");
        v3api.logout(sessionToken);
    }

    @Test
    public void testListSamplesWithMultipleFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.fetchContained().fetchContainer().fetchExperiment();
        fetchOptions.fetchProperties();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091250077-1050")), fetchOptions);
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
    public void testListSamplesWithType()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.fetchSampleType();

        List<Sample> samples =
                v3api.listSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050946559-979")), fetchOptions);
        assertEquals(samples.size(), 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().toString(), "/CISD/3VCP5");

        SampleType type = sample.getSampleType();
        assertEquals(type.getCode(), "CELL_PLATE");
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
    public void testListSamplesOrder()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.fetchSampleType();

        List<Sample> samples =
                v3api.listSamples(sessionToken,
                        Arrays.asList(
                                new SamplePermId("200811050946559-979"),
                                new SampleIdentifier("/CISD/MP002-1"),
                                new SamplePermId("200811050919915-8")
                                ), fetchOptions);

        assertEquals(samples.get(0).getPermId(), new SamplePermId("200811050946559-979"));
        assertEquals(samples.get(1).getIdentifier(), new SampleIdentifier("/CISD/MP002-1"));
        assertEquals(samples.get(2).getPermId(), new SamplePermId("200811050919915-8"));

        assertEquals(samples.size(), 3);
        v3api.logout(sessionToken);
    }

}
