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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.ReindexingState;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class UpdateExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testUpdateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ReindexingState state = new ReindexingState();

        ExperimentUpdate experiment = new ExperimentUpdate();
        experiment.setExperimentId(new ExperimentPermId("200811050951882-1028"));
        experiment.setProperty("DESCRIPTION", "an updated description");

        v3api.updateExperiments(sessionToken, Arrays.asList(experiment));

        assertExperimentsReindexed(state, "200811050951882-1028");
    }

    @Test
    public void testUpdateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, experimentId);
    }

    @Test
    public void testUpdateWithExperimentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, experimentId);
    }

    @Test
    @Rollback(value = false)
    public void testUpdateWithProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProjectId(new ProjectIdentifier("/CISD/NOE"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, ids, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getIdentifier().getIdentifier(), "/CISD/NOE/TEST_EXPERIMENT");
    }

    /*
     * @Test(groups = "project-samples") public void testMovingExperimentWithProjectSamplesToADifferentProject() { String sessionToken =
     * v3api.login(TEST_USER, PASSWORD); SampleCreation sampleCreation = new SampleCreation(); sampleCreation.setCode("PROJECT-SAMPLE-IN-EXPERIMENT");
     * ExperimentIdentifier experimentId = new ExperimentIdentifier("CISD", "NEMO", "EXP1"); sampleCreation.setExperimentId(experimentId);
     * sampleCreation.setSpaceId(new SpacePermId("CISD")); sampleCreation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
     * sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE")); v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));
     * ExperimentUpdate experimentUpdate = new ExperimentUpdate(); experimentUpdate.setExperimentId(experimentId); ProjectIdentifier projectId = new
     * ProjectIdentifier("/CISD/NOE"); experimentUpdate.setProjectId(projectId); v3api.updateExperiments(sessionToken,
     * Arrays.asList(experimentUpdate)); ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions(); fetchOptions.withProject();
     * fetchOptions.withSamples().withProject(); Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(new
     * ExperimentIdentifier("CISD", "NOE", "EXP1")), fetchOptions); List<Experiment> experiments = new ArrayList<Experiment>(map.values());
     * List<Sample> samples = experiments.get(0).getSamples(); assertEquals(experiments.get(0).getProject().getCode(), "NOE");
     * assertEquals(samples.get(0).getProject().getCode(), "NOE"); AssertionUtil.assertCollectionSize(samples, 1);
     * AssertionUtil.assertCollectionSize(experiments, 1); }
     */
    @Test
    public void testUpdateWithProjectNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProjectId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, "Project id cannot be null");
    }

    @Test
    public void testUpdateWithProjectInDifferentSpace()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("TEST_EXPERIMENT_WITH_SAMPLES");
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experimentCreation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));
        IExperimentId experimentId = experimentIds.get(0);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TEST_SAMPLE_WITH_EXPERIMENT");
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setExperimentId(experimentId);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));
        ISampleId sampleId = sampleIds.get(0);

        final ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experimentId);
        experimentUpdate.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));

        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate));

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withSpace();
        sampleFetchOptions.withExperiment();

        Map<ISampleId, Sample> sampleMap = v3api.getSamples(sessionToken, sampleIds, sampleFetchOptions);
        Sample sample = sampleMap.get(sampleId);

        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getPermId(), experimentId);
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/TEST_EXPERIMENT_WITH_SAMPLES");
    }

    @Test
    public void testUpdateWithProjectUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200902091255058-1037");
        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.setProjectId(projectId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, projectId);
    }

    @Test
    public void testUpdateWithProjectNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200902091255058-1037");
        final IProjectId projectId = new ProjectIdentifier("IDONTEXIST");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, projectId);
    }

    @Test
    public void testUpdateWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "description 1");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("DESCRIPTION", "description 2");

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, ids, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(1, experiment.getProperties().size());
        assertEquals("description 2", experiment.getProperties().get("DESCRIPTION"));
    }

    @Test
    public void testUpdateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testUpdateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("PURCHASE_DATE", "this should be a date");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, "Date value 'this should be a date' has improper format");
    }

    @Test
    public void testUpdateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentPermId permId = createExperimentWithoutTags();

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(permId);
        update.setProperty("DESCRIPTION", null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, "Value of mandatory property 'DESCRIPTION' not specified");
    }

    @Test
    public void testUpdateWithAttachmentsAddNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithoutAttachments();

        assertAttachments(experimentId);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().add(attachmentCreation);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertAttachments(experimentId, attachmentCreation);
    }

    @Test
    public void testUpdateWithAttachmentsAddExistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation);

        Map<String, Attachment> attachmentMap = assertAttachments(experimentId, attachmentCreation);
        Attachment attachment = attachmentMap.get(attachmentCreation.getFileName());

        assertEquals(attachment.getVersion(), Integer.valueOf(1));

        attachmentCreation.setTitle("test_title_2");
        attachmentCreation.setDescription("test_description_2");
        attachmentCreation.setContent(new String("test_content_2").getBytes());

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().add(attachmentCreation);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        attachmentMap = assertAttachments(experimentId, attachmentCreation);
        attachment = attachmentMap.get(attachmentCreation.getFileName());

        assertEquals(attachment.getVersion(), Integer.valueOf(2));
    }

    @Test
    public void testUpdateWithAttachmentsRemoveNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation);

        assertAttachments(experimentId, attachmentCreation);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().remove(new AttachmentFileName("test_file_2"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertAttachments(experimentId, attachmentCreation);
    }

    @Test
    public void testUpdateWithAttachmentsRemoveExistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation);

        assertAttachments(experimentId, attachmentCreation);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().remove(new AttachmentFileName("test_file"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertAttachments(experimentId);
    }

    @Test
    public void testUpdateWithAttachmentsSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation1 = new AttachmentCreation();
        attachmentCreation1.setFileName("test_file");
        attachmentCreation1.setTitle("test_title");
        attachmentCreation1.setDescription("test_description");
        attachmentCreation1.setContent(new String("test_content").getBytes());

        AttachmentCreation attachmentCreation2 = new AttachmentCreation();
        attachmentCreation2.setFileName("test_file_2");
        attachmentCreation2.setTitle("test_title_2");
        attachmentCreation2.setDescription("test_description_2");
        attachmentCreation2.setContent(new String("test_content_2").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation1, attachmentCreation2);

        assertAttachments(experimentId, attachmentCreation1, attachmentCreation2);

        attachmentCreation2.setTitle("test_title_3");
        attachmentCreation2.setDescription("test_description_3");
        attachmentCreation2.setContent(new String("test_content_3").getBytes());

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().set(attachmentCreation2);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        Map<String, Attachment> attachmentMap = assertAttachments(experimentId, attachmentCreation2);
        Attachment attachment = attachmentMap.get(attachmentCreation2.getFileName());

        assertEquals(attachment.getVersion(), Integer.valueOf(2));
    }

    @Test
    public void testUpdateWithTagsAddExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().add(new TagCode("TEST_TAG_2"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_1", "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsAddNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentPermId experimentId = createExperimentWithoutTags();
        final TagPermId tagId = new TagPermId("/test/THIS_TAG_SHOULD_BE_CREATED");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().add(tagId);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/THIS_TAG_SHOULD_BE_CREATED");
    }

    @Test
    public void testUpdateWithTagsAddUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentPermId experimentId = createExperimentWithoutTags();

        final TagPermId tagId = new TagPermId("/test_space/TEST_METAPROJECTS");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().add(tagId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, tagId);
    }

    @Test
    public void testUpdateWithTagsRemoveExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"), new TagPermId("/test/TEST_TAG_2"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(new TagPermId("/test/TEST_TAG_1"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsRemoveNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"), new TagPermId("/test/TEST_TAG_2"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(new TagPermId("/test/THIS_TAG_DOES_NOT_EXIST"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_1", "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsRemoveUnassigned()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"), new TagPermId("/test/TEST_TAG_2"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(new TagPermId("/test/TEST_METAPROJECTS"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_1", "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsRemoveUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        final ITagId tagId = new TagPermId("/test/TEST_TAG_1");
        final ExperimentPermId experimentId = createExperimentWithTags(tagId);

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(tagId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, tagId);
    }

    @Test
    public void testUpdateWithTagsSetExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().set(new TagPermId("/test/TEST_METAPROJECTS"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testUpdateWithTagsSetNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().set(new TagCode("THIS_TAG_DOES_NOT_EXIST"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/THIS_TAG_DOES_NOT_EXIST");
    }

    @Test
    public void testUpdateWithTagsSetUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        final ITagId tagId = new TagPermId("/test_space/TEST_METAPROJECTS");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().set(tagId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Arrays.asList(update));
                }
            }, tagId);
    }

    @Test
    public void testUpdateWithAdminUserInAnotherSpace()
    {
        final ExperimentPermId permId = new ExperimentPermId("200902091255058-1037");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    final ExperimentUpdate update = new ExperimentUpdate();
                    update.setExperimentId(permId);

                    v3api.updateExperiments(sessionToken, Collections.singletonList(update));
                }
            }, permId);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setContent("test content".getBytes());
        attachment.setFileName("test.txt");

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        update.setProperty("DESCRIPTION", "an updated description");
        update.getAttachments().add(attachment);

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            v3api.updateExperiments(sessionToken, Collections.singletonList(update));
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.updateExperiments(sessionToken, Collections.singletonList(update));
                    }
                }, update.getExperimentId());
        }
    }

    private ExperimentPermId createExperimentWithoutAttachments()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private ExperimentPermId createExperimentWithAttachments(AttachmentCreation... attachments)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setAttachments(Arrays.asList(attachments));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private ExperimentPermId createExperimentWithoutTags()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private ExperimentPermId createExperimentWithTags(ITagId... tags)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(tags));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private void assertTags(IExperimentId experimentId, String... expectedTagPermIds)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        Map<IExperimentId, Experiment> experiments = v3api.getExperiments(sessionToken, Arrays.asList(experimentId), fetchOptions);
        assertEquals(experiments.size(), 1);

        assertTags(experiments.get(experimentId).getTags(), expectedTagPermIds);
    }

    private Map<String, Attachment> assertAttachments(IExperimentId experimentId, AttachmentCreation... expectedAttachments)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withAttachments().withContent();

        Map<IExperimentId, Experiment> experiments = v3api.getExperiments(sessionToken, Arrays.asList(experimentId), fetchOptions);

        assertEquals(experiments.size(), 1);

        return assertAttachments(experiments.get(experimentId).getAttachments(), expectedAttachments);
    }
}
