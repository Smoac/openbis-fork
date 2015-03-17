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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateProjectExecutor extends AbstractCreateEntityExecutor<ProjectCreation, ProjectPE, ProjectPermId> implements
        ICreateProjectExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetProjectSpaceExecutor setProjectSpaceExecutor;

    @Autowired
    private ISetProjectLeaderExecutor setProjectLeaderExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Override
    protected ProjectPE create(IOperationContext context, ProjectCreation creation)
    {
        ProjectPE project = new ProjectPE();
        project.setCode(creation.getCode());
        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        project.setPermId(createdPermId);
        project.setDescription(creation.getDescription());
        project.setRegistrator(context.getSession().tryGetPerson());
        RelationshipUtils.updateModificationDateAndModifier(project, context.getSession().tryGetPerson());
        return project;
    }

    @Override
    protected ProjectPermId createPermId(IOperationContext context, ProjectPE entity)
    {
        return new ProjectPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, ProjectCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        ProjectIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context, ProjectPE entity)
    {
        if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(new ProjectIdentifier(entity.getIdentifier()));
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<ProjectPE> entities)
    {
        // nothing to do
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<ProjectCreation, ProjectPE> entitiesMap)
    {
        setProjectSpaceExecutor.set(context, entitiesMap);
        setProjectLeaderExecutor.set(context, entitiesMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<ProjectCreation, ProjectPE> entitiesMap)
    {
        Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentMap =
                new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();

        for (Map.Entry<ProjectCreation, ProjectPE> entry : entitiesMap.entrySet())
        {
            ProjectCreation creation = entry.getKey();
            ProjectPE entity = entry.getValue();
            attachmentMap.put(entity, creation.getAttachments());
        }

        createAttachmentExecutor.create(context, attachmentMap);
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
            daoFactory.getProjectDAO().createProject(entity, context.getSession().tryGetPerson());
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "project", null);
    }

}
