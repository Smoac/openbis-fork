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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExperimentProjectExecutor extends AbstractUpdateEntityToOneRelationExecutor<ExperimentUpdate, ExperimentPE, IProjectId, ProjectPE>
        implements IUpdateExperimentProjectExecutor
{

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Override
    protected IProjectId getRelatedId(ProjectPE related)
    {
        return new ProjectIdentifier(related.getIdentifier());
    }

    @Override
    protected ProjectPE getCurrentlyRelated(ExperimentPE entity)
    {
        return entity.getProject();
    }

    @Override
    protected FieldUpdateValue<IProjectId> getRelatedUpdate(ExperimentUpdate update)
    {
        return update.getProjectId();
    }

    @Override
    protected Map<IProjectId, ProjectPE> map(IOperationContext context, List<IProjectId> relatedIds)
    {
        return mapProjectByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ExperimentPE entity, IProjectId relatedId, ProjectPE related)
    {
        if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, ExperimentPE entity, ProjectPE related)
    {
        if (related == null)
        {
            throw new UserFailureException("Project id cannot be null");
        } else
        {
            relationshipService.assignExperimentToProject(context.getSession(), entity, related);
        }
    }
}
