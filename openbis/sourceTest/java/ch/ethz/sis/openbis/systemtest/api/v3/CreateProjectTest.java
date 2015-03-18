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
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.IPersonId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.PersonPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class CreateProjectTest extends AbstractTest
{

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ProjectCreation project = new ProjectCreation();
        project.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ProjectCreation project = new ProjectCreation();
        project.setCode("PROJECT_WITH_EXISTING_CODE");
        project.setSpaceId(new SpacePermId("CISD"));

        v3api.createProjects(sessionToken, Arrays.asList(project));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, "Project already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ProjectCreation project = new ProjectCreation();
        project.setCode("?!*");
        project.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, "The code '?!*' contains illegal characters");
    }

    @Test
    public void testCreateWithSpaceNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ProjectCreation project = new ProjectCreation();
        project.setCode("TEST_PROJECT");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, "Space id cannot be null");
    }

    @Test
    public void testCreateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final ProjectCreation project = new ProjectCreation();
        project.setCode("TEST_PROJECT");
        project.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, spaceId);
    }

    @Test
    public void testCreateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final ProjectCreation project = new ProjectCreation();
        project.setCode("TEST_PROJECT");
        project.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, spaceId);
    }

    @Test
    public void testCreateWithLeaderNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IPersonId leaderId = new PersonPermId("IDONTEXIST");
        final ProjectCreation project = new ProjectCreation();
        project.setCode("TEST_PROJECT");
        project.setSpaceId(new SpacePermId("CISD"));
        project.setLeaderId(leaderId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createProjects(sessionToken, Arrays.asList(project));
                }
            }, leaderId);
    }

    @Test
    public void testCreateWithMultipleProjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpacePermId spaceId1 = new SpacePermId("CISD");
        SpacePermId spaceId2 = new SpacePermId("TEST-SPACE");

        PersonPermId leaderId1 = new PersonPermId(TEST_SPACE_USER);

        ProjectCreation creation1 = new ProjectCreation();
        creation1.setCode("TEST_PROJECT_1");
        creation1.setDescription("description 1");
        creation1.setSpaceId(spaceId1);
        creation1.setLeaderId(leaderId1);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        byte[] attachmentContent = "attachment".getBytes();
        attachmentCreation.setContent(attachmentContent);
        attachmentCreation.setDescription("attachment description");
        attachmentCreation.setFileName("attachment.txt");
        attachmentCreation.setTitle("attachment title");
        creation1.setAttachments(Arrays.asList(attachmentCreation));

        ProjectCreation creation2 = new ProjectCreation();
        creation2.setCode("TEST_PROJECT_2");
        creation2.setSpaceId(spaceId2);

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Arrays.asList(creation1, creation2));

        assertEquals(permIds.size(), 2);

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withModifier();
        fetchOptions.withRegistrator();
        fetchOptions.withLeader();
        fetchOptions.withAttachments().withContent();

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, permIds, fetchOptions);

        Assert.assertEquals(2, map.size());

        Project project1 = map.get(permIds.get(0));
        assertEquals(project1.getCode(), creation1.getCode());
        assertEquals(project1.getSpace().getCode(), spaceId1.getPermId());
        assertEquals(project1.getIdentifier().getIdentifier(), "/" + spaceId1.getPermId() + "/" + creation1.getCode());
        assertEquals(project1.getDescription(), creation1.getDescription());
        assertEquals(project1.getRegistrator().getUserId(), TEST_USER);
        assertEquals(project1.getModifier().getUserId(), TEST_USER);
        assertEquals(project1.getLeader().getUserId(), leaderId1.getPermId());

        List<Attachment> attachments = project1.getAttachments();
        assertEquals(attachments.size(), 1);
        assertEquals(attachments.get(0).getContent(), attachmentContent);

        Project project2 = map.get(permIds.get(1));

        assertEquals(project2.getCode(), creation2.getCode());
        assertEquals(project2.getIdentifier().getIdentifier(), "/" + spaceId2.getPermId() + "/" + creation2.getCode());
        assertEquals(project2.getSpace().getCode(), spaceId2.getPermId());
        assertEquals(project2.getModifier().getUserId(), TEST_USER);
        assertEquals(project2.getRegistrator().getUserId(), TEST_USER);
    }

}
