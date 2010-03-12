/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IProjectBO}. We are using an interface here to keep
 * the system testable.
 * 
 * @author Christian Ribeaud
 */
public final class ProjectBO extends AbstractBusinessObject implements IProjectBO
{

    /**
     * The business object held by this implementation.
     * <p>
     * Package protected so that <i>Unit Test</i> can access it.
     * </p>
     */
    private ProjectPE project;

    private boolean dataChanged;

    private final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();

    public ProjectBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private ProjectPE createProject(final ProjectIdentifier projectIdentifier, String description,
            String leaderId)
    {
        final ProjectPE result = new ProjectPE();
        final GroupPE group =
                GroupIdentifierHelper.tryGetGroup(projectIdentifier, session.tryGetPerson(), this);
        result.setGroup(group);
        result.setRegistrator(findRegistrator());
        result.setCode(projectIdentifier.getProjectCode());
        result.setDescription(description);
        if (leaderId != null)
        {
            PersonPE leader = getPersonDAO().tryFindPersonByUserId(leaderId);
            if (leader == null)
            {
                throw new UserFailureException("Person '%s' not found in the database.");
            }
            result.setProjectLeader(leader);
        }
        return result;
    }

    public final void save()
    {
        assert project != null : "Can not save an undefined project.";
        if (dataChanged)
        {
            try
            {
                getProjectDAO().createProject(project);
            } catch (final DataAccessException ex)
            {
                throwException(ex, "Project '" + project.getCode() + "'");
            }
        }
        if (attachments.isEmpty() == false)
        {
            final IAttachmentDAO attachmentDAO = getAttachmentDAO();
            for (final AttachmentPE attachment : attachments)
            {
                try
                {
                    attachmentDAO.createAttachment(attachment, project);
                } catch (final DataAccessException e)
                {
                    final String fileName = attachment.getFileName();
                    throwException(e, String.format("Filename '%s' for project '%s'", fileName,
                            project.getIdentifier()));
                }
            }
            attachments.clear();
        }
    }

    public final ProjectPE getProject()
    {
        return project;
    }

    public void define(ProjectIdentifier projectIdentifier, String description, String leaderId)
            throws UserFailureException
    {
        assert projectIdentifier != null : "Unspecified project identifier.";
        this.project = createProject(projectIdentifier, description, leaderId);
        dataChanged = true;
    }

    public void loadByProjectIdentifier(ProjectIdentifier identifier)
    {
        String databaseInstanceCode = identifier.getDatabaseInstanceCode();
        String spaceCode = identifier.getSpaceCode();
        String projectCode = identifier.getProjectCode();
        project = getProjectDAO().tryFindProject(databaseInstanceCode, spaceCode, projectCode);
        if (project == null)
        {
            throw new UserFailureException(String
                    .format("Project '%s' does not exist.", identifier));
        }
        dataChanged = false;
    }

    public void loadDataByTechId(TechId projectId)
    {
        try
        {
            project = getProjectDAO().getByTechId(projectId);
        } catch (ObjectRetrievalFailureException exception)
        {
            throw new UserFailureException(String.format("Project with ID '%s' does not exist.",
                    projectId));
        }
        dataChanged = false;
    }

    public final void addAttachment(final AttachmentPE attachment)
    {
        assert project != null : "no project has been loaded";
        attachment.setRegistrator(findRegistrator());
        escapeFileName(attachment);
        attachments.add(attachment);
    }

    private void escapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            attachment.setFileName(ProjectPE.escapeFileName(attachment.getFileName()));
        }
    }

    public AttachmentPE getProjectFileAttachment(final String filename, final int version)
    {
        checkProjectLoaded();
        project.ensureAttachmentsLoaded();
        final Set<AttachmentPE> attachmentsSet = project.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                HibernateUtils.initialize(att.getAttachmentContent());
                return att;
            }
        }
        throw new UserFailureException("Attachment '" + filename + "' (version '" + version
                + "') not found in project '" + project.getIdentifier() + "'.");
    }

    private void checkProjectLoaded()
    {
        if (project == null)
        {
            throw new IllegalStateException("Unloaded project.");
        }
    }

    public final void enrichWithAttachments()
    {
        if (project != null)
        {
            project.ensureAttachmentsLoaded();
        }
    }

    public void update(ProjectUpdatesDTO updates)
    {
        loadByProjectIdentifier(updates.getIdentifier());
        if (updates.getVersion().equals(project.getModificationDate()) == false)
        {
            throwModifiedEntityException("Project");
        }
        project.setDescription(updates.getDescription());
        for (NewAttachment attachment : updates.getAttachments())
        {
            addAttachment(AttachmentTranslator.translate(attachment));
        }
        String groupCode = updates.getGroupCode();
        if (groupCode != null && groupCode.equals(project.getGroup().getCode()) == false)
        {
            updateGroup(groupCode);
        }
        dataChanged = true;
    }

    private void updateGroup(String groupCode)
    {
        GroupPE group = findGroup(groupCode);
        project.setGroup(group);
        for (ExperimentPE experiment : project.getExperiments())
        {
            SampleUtils.setSamplesGroup(experiment, group);
        }
    }

    private GroupPE findGroup(String groupCode)
    {
        GroupPE group =
                getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(groupCode,
                        project.getGroup().getDatabaseInstance());
        if (group == null)
        {
            throw UserFailureException
                    .fromTemplate("No space with the name '%s' found!", groupCode);
        }
        return group;
    }

    public void deleteByTechId(TechId projectId, String reason) throws UserFailureException
    {
        loadDataByTechId(projectId);
        try
        {
            // Experiments need to be initialized because Hibernate Search updates index after
            // deletion and LazyInitializationException occurs if this collection is not
            // initialized.
            HibernateUtils.initialize(project.getExperiments());
            getProjectDAO().delete(project);
            getEventDAO().persist(createDeletionEvent(project, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Project '%s'", project.getCode()));
        }
    }

    public static EventPE createDeletionEvent(ProjectPE project, PersonPE registrator, String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.PROJECT);
        event.setIdentifier(project.getIdentifier());
        event.setDescription(getDeletionDescription(project));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(ProjectPE project)
    {
        return String.format("%s", project.getIdentifier());
    }
}
