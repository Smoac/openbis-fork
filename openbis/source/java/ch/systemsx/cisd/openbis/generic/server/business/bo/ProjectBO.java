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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
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

    private IRelationshipService relationshipService;

    public ProjectBO(final IDAOFactory daoFactory, final Session session,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session);
        this.relationshipService = relationshipService;
    }

    private ProjectPE createProject(final ProjectIdentifier projectIdentifier, String description,
            String leaderIdOrNull)
    {
        final ProjectPE result = new ProjectPE();
        final SpacePE group =
                SpaceIdentifierHelper.tryGetSpace(projectIdentifier, session.tryGetPerson(), this);
        result.setSpace(group);
        result.setRegistrator(findPerson());
        result.setPermId(getPermIdDAO().createPermId());
        result.setCode(projectIdentifier.getProjectCode());
        result.setDescription(description);
        if (leaderIdOrNull != null)
        {
            PersonPE leader = getPersonDAO().tryFindPersonByUserId(leaderIdOrNull);
            if (leader == null)
            {
                throw new UserFailureException("Person '%s' not found in the database.");
            }
            result.setProjectLeader(leader);
        }
        return result;
    }

    @Override
    public final void save()
    {
        assert project != null : "Can not save an undefined project.";
        if (dataChanged)
        {
            try
            {
                getProjectDAO().createProject(project, findPerson());
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
                    throwException(
                            e,
                            String.format("Filename '%s' for project '%s'", fileName,
                                    project.getIdentifier()));
                }
            }
            attachments.clear();
        }
    }

    @Override
    public final ProjectPE getProject()
    {
        return project;
    }

    @Override
    public void define(ProjectIdentifier projectIdentifier, String description, String leaderId)
            throws UserFailureException
    {
        assert projectIdentifier != null : "Unspecified project identifier.";
        this.project = createProject(projectIdentifier, description, leaderId);
        dataChanged = true;
    }

    @Override
    public void loadByProjectIdentifier(ProjectIdentifier identifier)
    {
        String databaseInstanceCode = identifier.getDatabaseInstanceCode();
        String spaceCode = identifier.getSpaceCode();
        String projectCode = identifier.getProjectCode();
        project = getProjectDAO().tryFindProject(databaseInstanceCode, spaceCode, projectCode);
        if (project == null)
        {
            throw new UserFailureException(
                    String.format("Project '%s' does not exist.", identifier));
        }
        dataChanged = false;
    }

    @Override
    public void loadByPermId(String permId)
    {
        project = getProjectDAO().tryGetByPermID(permId);
        if (project == null)
        {
            throw new UserFailureException(String.format(
                    "Project with PERM_ID '%s' does not exist.", permId));
        }
        dataChanged = false;
    }

    @Override
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

    @Override
    public final void addAttachment(final AttachmentPE attachment)
    {
        assert project != null : "no project has been loaded";
        attachment.setRegistrator(findPerson());
        escapeFileName(attachment);
        attachments.add(attachment);
    }

    private void escapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            attachment.setFileName(AttachmentHolderPE.escapeFileName(attachment.getFileName()));
        }
    }

    @Override
    public AttachmentPE getProjectFileAttachment(final String filename, final Integer versionOrNull)
    {
        checkProjectLoaded();
        project.ensureAttachmentsLoaded();
        AttachmentPE att =
                versionOrNull == null ? getAttachment(filename) : getAttachment(filename,
                        versionOrNull);
        if (att != null)
        {
            HibernateUtils.initialize(att.getAttachmentContent());
            return att;
        }

        throw new UserFailureException(
                "Attachment '"
                        + filename
                        + "' "
                        + (versionOrNull == null ? "(latest version)" : "(version '"
                                + versionOrNull + "')") + " not found in project '"
                        + project.getIdentifier() + "'.");
    }

    private AttachmentPE getAttachment(String filename, final int version)
    {
        final Set<AttachmentPE> attachmentsSet = project.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                return att;
            }
        }

        return null;
    }

    private AttachmentPE getAttachment(String filename)
    {
        AttachmentPE latest = null;
        final Set<AttachmentPE> attachmentsSet = project.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename))
            {
                if (latest == null || latest.getVersion() < att.getVersion())
                {
                    latest = att;
                }
            }
        }

        return latest;
    }

    private void checkProjectLoaded()
    {
        if (project == null)
        {
            throw new IllegalStateException("Unloaded project.");
        }
    }

    @Override
    public final void enrichWithAttachments()
    {
        if (project != null)
        {
            project.ensureAttachmentsLoaded();
        }
    }

    @Override
    public void update(ProjectUpdatesDTO updates)
    {
        loadDataByTechId(updates.getTechId());
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
        if (groupCode != null && groupCode.equals(project.getSpace().getCode()) == false)
        {

            relationshipService.assignProjectToSpace(session, project, findGroup(groupCode));
        }
        dataChanged = true;
    }

    private SpacePE findGroup(String groupCode)
    {
        SpacePE group =
                getSpaceDAO().tryFindSpaceByCodeAndDatabaseInstance(groupCode,
                        project.getSpace().getDatabaseInstance());
        if (group == null)
        {
            throw UserFailureException
                    .fromTemplate("No space with the name '%s' found!", groupCode);
        }
        return group;
    }

    @Override
    public void deleteByTechId(TechId projectId, String reason) throws UserFailureException
    {
        loadDataByTechId(projectId);
        try
        {
            List<String> codes = new ArrayList<String>();
            List<String> trashedCodes = new ArrayList<String>();
            List<ExperimentPE> experiments =
                    getExperimentDAO().listExperimentsWithProperties(project, false, false);
            for (ExperimentPE experiment : experiments)
            {
                codes.add(experiment.getCode());
            }
            IDeletionDAO deletionDAO = getDeletionDAO();
            List<DeletionPE> deletionPEs = deletionDAO.listAllEntities();
            List<TechId> ids = new ArrayList<TechId>();
            for (DeletionPE deletion : deletionPEs)
            {
                ids.add(new TechId(deletion.getId()));
            }
            List<TechId> deletedExperimentIds = deletionDAO.findTrashedExperimentIds(ids);
            List<DeletedExperimentPE> deletedExperiments =
                    cast(deletionDAO.listDeletedEntities(EntityKind.EXPERIMENT,
                            deletedExperimentIds));
            for (DeletedExperimentPE deletedExperiment : deletedExperiments)
            {
                if (deletedExperiment.getProject().getId() == project.getId())
                {
                    trashedCodes.add(deletedExperiment.getCode());
                }
            }
            if (codes.isEmpty() && trashedCodes.isEmpty())
            {
                getProjectDAO().delete(project);
                getEventDAO().persist(createDeletionEvent(project, session.tryGetPerson(), reason));
            } else
            {
                StringBuilder builder = new StringBuilder();
                if (codes.isEmpty() == false)
                {
                    builder.append("the following experiments still exist: ");
                    builder.append(CollectionUtils.abbreviate(codes, 10));
                }
                if (trashedCodes.isEmpty() == false)
                {
                    if (codes.isEmpty() == false)
                    {
                        builder.append("\nIn addition ");
                    }
                    builder.append("the following experiments are in the trash can: ");
                    builder.append(CollectionUtils.abbreviate(trashedCodes, 10));
                }
                throw new UserFailureException("Project '" + project.getCode()
                        + "' can not be deleted because " + builder);
            }
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
        event.setIdentifiers(Collections.singletonList(project.getIdentifier()));
        event.setDescription(getDeletionDescription(project));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T cast(final Object object)
    {
        return (T) object;
    }

    private static String getDeletionDescription(ProjectPE project)
    {
        return String.format("%s", project.getIdentifier());
    }
}
