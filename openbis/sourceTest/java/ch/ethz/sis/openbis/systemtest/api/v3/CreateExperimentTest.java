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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
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
    public void testCreateExperimentWithUnauthorizedProject()
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
            }, projectId);
    }

    @Test
    public void testCreateExperimentWithNonexistentProject()
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
            }, projectId);
    }

    @Test
    public void testCreateExperiment()
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
        experiment2.setProperty("GENDER", "MALE");
        experiment2.setTagIds(Arrays.<ITagId> asList(
                new TagPermId("/test/TEST_METAPROJECTS")
                , new TagPermId("/test/ANOTHER_TEST_METAPROJECTS")
                ));

        List<ExperimentPermId> result = v3api.createExperiments(sessionToken, Arrays.asList(experiment1, experiment2));

        assertEquals(result.size(), 2);

        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.fetchType();
        experimentFetchOptions.fetchProject();
        experimentFetchOptions.fetchModifier();
        experimentFetchOptions.fetchRegistrator();
        experimentFetchOptions.fetchProperties();
        experimentFetchOptions.fetchAttachments().fetchContent();
        experimentFetchOptions.fetchTags();

        List<Experiment> experiments = v3api.listExperiments(sessionToken, result, experimentFetchOptions);

        Collections.sort(experiments, new Comparator<Experiment>()
            {
                @Override
                public int compare(Experiment arg0, Experiment arg1)
                {
                    return arg0.getCode().compareTo(arg1.getCode());
                }
            });

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
        assertEquals(exp.getProperties().size(), 1, exp.getProperties().toString());
        assertEquals(exp.getProperties().get("GENDER"), "MALE");

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
