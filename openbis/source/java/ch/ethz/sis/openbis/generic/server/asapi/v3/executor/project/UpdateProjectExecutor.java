/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateProjectExecutor extends AbstractUpdateEntityExecutor<ProjectUpdate, ProjectPE, IProjectId> implements
        IUpdateProjectExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Autowired
    private IUpdateProjectSpaceExecutor updateProjectSpaceExecutor;

    @Autowired
    private IUpdateProjectAttachmentExecutor updateProjectAttachmentExecutor;

    @Override
    protected IProjectId getId(ProjectUpdate update)
    {
        return update.getProjectId();
    }

    @Override
    protected void checkData(IOperationContext context, ProjectUpdate update)
    {
        if (update.getProjectId() == null)
        {
            throw new UserFailureException("Project id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IProjectId id, ProjectPE entity)
    {
        if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, CollectionBatch<ProjectPE> batch)
    {
        // nothing to do
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<ProjectUpdate, ProjectPE> batch)
    {
        updateProjectSpaceExecutor.update(context, batch);

        PersonPE person = context.getSession().tryGetPerson();
        Date timeStamp = daoFactory.getTransactionTimestamp();
        for (Map.Entry<ProjectUpdate, ProjectPE> entry : batch.getObjects().entrySet())
        {
            ProjectUpdate update = entry.getKey();
            ProjectPE project = entry.getValue();

            RelationshipUtils.updateModificationDateAndModifier(project, person, timeStamp);

            if (update.getDescription() != null && update.getDescription().isModified())
            {
                project.setDescription(update.getDescription().getValue());
            }

            if (update.getAttachments() != null && update.getAttachments().hasActions())
            {
                updateProjectAttachmentExecutor.update(context, project, update.getAttachments());
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<ProjectUpdate, ProjectPE> batch)
    {
        // nothing to do
    }

    @Override
    protected Map<IProjectId, ProjectPE> map(IOperationContext context, Collection<IProjectId> ids)
    {
        return mapProjectByIdExecutor.map(context, ids);
    }

    @Override
    protected List<ProjectPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getProjectDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<ProjectPE> entities, boolean clearCache)
    {
        for (ProjectPE entity : entities)
        {
            daoFactory.getProjectDAO().validateAndSaveUpdatedEntity(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "project", null);
    }

}
