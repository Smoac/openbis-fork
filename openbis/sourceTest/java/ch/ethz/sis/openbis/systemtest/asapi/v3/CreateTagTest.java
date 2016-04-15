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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class CreateTagTest extends AbstractTest
{

    @Test
    public void testCreateWithCodeNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_METAPROJECTS");

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, "Tag already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("i am incorrect");

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, "Tag name cannot contain white spaces, commas, slashes or backslashes");
    }

    @Test
    public void testCreateWithDescription()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setDescription("test description");

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertEquals(tag.getDescription(), creation.getDescription());
    }

    @Test
    public void testCreateWithExperiments()
    {
        final ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setExperimentIds(Arrays.asList(experimentId));

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertExperimentIdentifiers(tag.getExperiments(), experimentId.getIdentifier());
    }

    @Test
    public void testCreateWithExperimentsUnauthorized()
    {
        final ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setExperimentIds(Arrays.asList(experimentId));

                    createTag(TEST_SPACE_USER, PASSWORD, creation);
                }
            }, experimentId);
    }

    @Test
    public void testCreateWithExperimentsNonexistent()
    {
        final ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/IDONTEXIST");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setExperimentIds(Arrays.asList(experimentId));

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, experimentId);
    }

    @Test
    public void testCreateWithSamples()
    {
        final SampleIdentifier sampleId = new SampleIdentifier("/CISD/CP-TEST-1");

        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setSampleIds(Arrays.asList(sampleId));

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertSampleIdentifiers(tag.getSamples(), sampleId.getIdentifier());
    }

    @Test
    public void testCreateWithSamplesUnauthorized()
    {
        final SampleIdentifier sampleId = new SampleIdentifier("/CISD/CP-TEST-1");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setSampleIds(Arrays.asList(sampleId));

                    createTag(TEST_SPACE_USER, PASSWORD, creation);
                }
            }, sampleId);
    }

    @Test
    public void testCreateWithSamplesNonexistent()
    {
        final SampleIdentifier sampleId = new SampleIdentifier("/CISD/IDONTEXIST");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setSampleIds(Arrays.asList(sampleId));

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, sampleId);
    }

    private Tag createTag(String user, String password, TagCreation creation)
    {
        String sessionToken = v3api.login(user, password);

        List<TagPermId> ids = v3api.createTags(sessionToken, Arrays.asList(creation));

        assertEquals(ids.size(), 1);

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withExperiments();
        fetchOptions.withSamples();
        fetchOptions.withDataSets();
        fetchOptions.withMaterials();

        Map<ITagId, Tag> map = v3api.mapTags(sessionToken, ids, fetchOptions);

        assertEquals(map.size(), 1);

        return map.get(ids.get(0));
    }

}
