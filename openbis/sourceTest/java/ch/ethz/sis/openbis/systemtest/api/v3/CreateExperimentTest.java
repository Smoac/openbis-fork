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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.helper.experiment.ExperimentContextDescription;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class CreateExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Code cannot be empty", ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_EXISTING_CODE");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment.setProperty("DESCRIPTION", "a description");

        v3api.createExperiments(sessionToken, Arrays.asList(experiment));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Experiment already exists in the database and needs to be unique", ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("?!*");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "The code '?!*' contains illegal characters", ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithProjectNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Project id cannot be null", ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithProjectUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TESTGROUP/TESTPROJ");
        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(projectId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, projectId, ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithProjectNonexistent()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TESTGROUP/IDONTEXIST");
        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, projectId, ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Type id cannot be null", ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final IProjectId projectId = new ProjectIdentifier("/TESTGROUP/TESTPROJ");
        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(typeId);
        experiment.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, typeId, ExperimentContextDescription.creating(experiment));
    }

    @Test
    public void testCreateWithTagExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final Date now = new Date();

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(new TagPermId("/test/TEST_METAPROJECTS")));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);

        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertEquals(experiment.getTags().size(), 1);

        Tag tag = experiment.getTags().iterator().next();

        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getPermId().getPermId(), "/test/TEST_METAPROJECTS");
        assertTrue(tag.getRegistrationDate().getTime() < now.getTime());
    }

    @Test
    public void testCreateWithTagNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final Date now = new Date();

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(new TagPermId("/test/NEW_TAG_THAT_SHOULD_BE_CREATED")));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);

        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertEquals(experiment.getTags().size(), 1);

        Tag tag = experiment.getTags().iterator().next();

        assertEquals(tag.getCode(), "NEW_TAG_THAT_SHOULD_BE_CREATED");
        assertEquals(tag.getPermId().getPermId(), "/test/NEW_TAG_THAT_SHOULD_BE_CREATED");
        // there can be a 1 second rounding when converting database date to java date
        assertTrue(tag.getRegistrationDate().getTime() + 1000 >= now.getTime());
    }

    @Test
    public void testCreateWithTagUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ITagId tagId = new TagPermId("/test/TEST_METAPROJECTS");
        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setTagIds(Arrays.asList(tagId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, tagId, ExperimentContextDescription.creating(creation));
    }

    @Test
    public void testCreateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testCreateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("PURCHASE_DATE", "this should be a date");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, "Date value 'this should be a date' has improper format");
    }

    @Test
    public void testCreateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, "Value of mandatory property 'DESCRIPTION' not specified");
    }

    @Test
    public void testCreateWithMandatoryFieldsOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        assertEquals(permIds.size(), 1);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withModifier();
        fetchOptions.withRegistrator();
        fetchOptions.withProperties();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withTags();

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT1");
        assertEquals(experiment.getPermId(), permIds.get(0));
        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertNotNull(experiment.getType().getCode(), "SIRNA_HCS");
        assertNotNull(experiment.getProject().getIdentifier(), "/TESTGROUP/TESTPROJ");
        assertEquals(experiment.getProperties().size(), 1);
        assertTrue(experiment.getAttachments().isEmpty());
        assertTrue(experiment.getTags().isEmpty());
        assertEquals(experiment.getRegistrator().getUserId(), TEST_USER);
        assertEquals(experiment.getModifier().getUserId(), TEST_USER);
    }

    @Test
    public void testCreateWithMultipleExperiments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation experiment1 = new ExperimentCreation();
        experiment1.setCode("TEST_EXPERIMENT1");
        experiment1.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment1.setProjectId(new ProjectPermId("20120814110011738-103"));
        experiment1.setProperty("DESCRIPTION", "a description");
        experiment1.setProperty("PURCHASE_DATE", "2008-11-05 09:18:00");

        AttachmentCreation a = new AttachmentCreation();

        byte[] attachmentContent = "attachment".getBytes();
        a.setContent(attachmentContent);
        a.setDescription("attachment description");
        a.setFileName("attachment.txt");
        a.setTitle("attachment title");
        experiment1.setAttachments(Arrays.asList(a));

        ExperimentCreation experiment2 = new ExperimentCreation();
        experiment2.setCode("TEST_EXPERIMENT2");
        experiment2.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment2.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment2.setProperty("DESCRIPTION", "a description");
        experiment2.setProperty("GENDER", "MALE");
        experiment2.setTagIds(Arrays.<ITagId> asList(
                new TagPermId("/test/TEST_METAPROJECTS")
                , new TagPermId("/test/ANOTHER_TEST_METAPROJECTS")
                ));

        List<ExperimentPermId> result = v3api.createExperiments(sessionToken, Arrays.asList(experiment1, experiment2));

        assertEquals(result.size(), 2);

        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withType();
        experimentFetchOptions.withProject();
        experimentFetchOptions.withModifier();
        experimentFetchOptions.withRegistrator();
        experimentFetchOptions.withProperties();
        experimentFetchOptions.withAttachments().withContent();
        experimentFetchOptions.withTags();

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, result, experimentFetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        Assert.assertEquals(2, experiments.size());

        Assert.assertFalse(experiments.get(0).getPermId().getPermId().equals(experiments.get(1).getPermId().getPermId()));

        Experiment exp = experiments.get(0);
        assertEquals(exp.getCode(), "TEST_EXPERIMENT1");
        assertEquals(exp.getType().getCode(), "SIRNA_HCS");
        assertEquals(exp.getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(exp.getProperties().size(), 2, exp.getProperties().toString());
        assertEquals(exp.getProperties().get("DESCRIPTION"), "a description");
        assertEquals(exp.getProperties().get("PURCHASE_DATE"), "2008-11-05 09:18:00 +0100");
        List<Attachment> attachments = exp.getAttachments();
        assertEquals(attachments.size(), 1);
        assertEquals(attachments.get(0).getContent(), attachmentContent);

        exp = experiments.get(1);
        assertEquals(exp.getCode(), "TEST_EXPERIMENT2");
        assertEquals(exp.getType().getCode(), "SIRNA_HCS");
        assertEquals(exp.getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(exp.getProperties().size(), 2, exp.getProperties().toString());
        assertEquals(exp.getProperties().get("GENDER"), "MALE");
        assertEquals(exp.getProperties().get("DESCRIPTION"), "a description");

        HashSet<String> tagIds = new HashSet<String>();
        for (Tag tag : exp.getTags())
        {
            tagIds.add(tag.getPermId().getPermId());
        }
        assertEquals(tagIds, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));
        assertEquals(exp.getModifier().getUserId(), TEST_USER);
        assertEquals(exp.getRegistrator().getUserId(), TEST_USER);

    }

}
