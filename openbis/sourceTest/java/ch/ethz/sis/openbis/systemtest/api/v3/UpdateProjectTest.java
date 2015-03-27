/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.IPersonId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.PersonPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class UpdateProjectTest extends AbstractTest
{

    @Test
    public void testUpdateWithProjectNull()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ProjectUpdate update = new ProjectUpdate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, "Project id cannot be null");
    }

    @Test
    public void testUpdateWithProjectUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, projectId);
    }

    @Test
    public void testUpdateWithProjectNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectPermId("IDONTEXIST");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, projectId);
    }

    @Test
    public void testUpdateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ISpaceId spaceId = new SpacePermId("CISD");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceNull()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, "Space id cannot be null");
    }

    @Test
    public void testUpdateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceWhenSamplesExist()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withSpace();
        projectFetchOptions.withExperiments().withSamples().withSpace();

        Map<IProjectId, Project> projectMap = v3api.mapProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);

        for (Project project : projectMap.values())
        {
            Assert.assertEquals(project.getSpace().getCode(), "TEST-SPACE");

            for (Experiment experiment : project.getExperiments())
            {
                for (Sample sample : experiment.getSamples())
                {
                    Assert.assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
                }
            }

        }

        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(new SpacePermId("CISD"));

        v3api.updateProjects(sessionToken, Arrays.asList(update));

        projectMap = v3api.mapProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);

        for (Project project : projectMap.values())
        {
            Assert.assertEquals(project.getSpace().getCode(), "CISD");

            for (Experiment experiment : project.getExperiments())
            {
                for (Sample sample : experiment.getSamples())
                {
                    Assert.assertEquals(sample.getSpace().getCode(), "CISD");
                }
            }
        }
    }

    @Test
    public void testUpdateWithLeaderNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final IPersonId leaderId = new PersonPermId("IDONTEXIST");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setLeaderId(leaderId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, leaderId);
    }

    @Test
    public void testUpdateWithLeader()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withLeader();

        Map<IProjectId, Project> projectMap = v3api.mapProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);
        Project project = projectMap.get(projectId);

        Assert.assertNull(project.getLeader());

        final IPersonId leaderId = new PersonPermId(TEST_SPACE_USER);
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setLeaderId(leaderId);

        v3api.updateProjects(sessionToken, Arrays.asList(update));
        projectMap = v3api.mapProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);
        project = projectMap.get(projectId);

        Assert.assertEquals(project.getLeader().getPermId(), leaderId);

        update.setLeaderId(null);

        v3api.updateProjects(sessionToken, Arrays.asList(update));
        projectMap = v3api.mapProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);
        project = projectMap.get(projectId);

        Assert.assertNull(project.getLeader());
    }

    @Test
    public void testUpdateWithAttachments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("PROJECT_ATTACHMENTS_TEST");
        projectCreation.setSpaceId(new SpacePermId("CISD"));

        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withAttachments().withContent();

        List<ProjectPermId> projectPermIds = v3api.createProjects(sessionToken, Arrays.asList(projectCreation));

        Map<IProjectId, Project> projectMap = v3api.mapProjects(sessionToken, projectPermIds, projectFetchOptions);
        Project project = projectMap.values().iterator().next();

        assertAttachments(project.getAttachments());

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(projectPermIds.get(0));
        projectUpdate.getAttachments().add(attachmentCreation);

        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));

        projectMap = v3api.mapProjects(sessionToken, projectPermIds, projectFetchOptions);
        project = projectMap.values().iterator().next();

        assertAttachments(project.getAttachments(), attachmentCreation);
    }

    public void testUpdateWithDescription()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IProjectId projectId1 = new ProjectIdentifier("/CISD/NEMO");
        final IProjectId projectId2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(projectId1, projectId2), new ProjectFetchOptions());

        Project project1 = map.get(projectId1);
        Project project2 = map.get(projectId2);

        Assert.assertEquals(project1.getCode(), "NEMO");
        Assert.assertEquals(project1.getDescription(), "nemo description");
        Assert.assertEquals(project2.getCode(), "TEST-PROJECT");
        Assert.assertNull(project2.getDescription());

        ProjectUpdate update1 = new ProjectUpdate();
        update1.setProjectId(projectId1);
        update1.setDescription("a new description 1");

        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProjectId(projectId2);
        update2.setDescription("a new description 2");

        v3api.updateProjects(sessionToken, Arrays.asList(update1, update2));

        map = v3api.mapProjects(sessionToken, Arrays.asList(projectId1, projectId2), new ProjectFetchOptions());
        project1 = map.get(projectId1);
        project2 = map.get(projectId2);

        Assert.assertEquals(project1.getCode(), "NEMO");
        Assert.assertEquals(project1.getDescription(), update1.getDescription());
        Assert.assertEquals(project2.getCode(), "TEST-PROJECT");
        Assert.assertEquals(project2.getDescription(), update2.getDescription());
    }

}
