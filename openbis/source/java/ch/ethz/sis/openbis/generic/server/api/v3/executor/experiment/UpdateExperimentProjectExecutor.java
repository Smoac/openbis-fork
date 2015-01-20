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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExperimentProjectExecutor implements IUpdateExperimentProjectExecutor
{

    @Autowired
    private IRelationshipService relationshipService;

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @SuppressWarnings("unused")
    private UpdateExperimentProjectExecutor()
    {
    }

    public UpdateExperimentProjectExecutor(IRelationshipService relationshipService, IMapProjectByIdExecutor mapProjectByIdExecutor)
    {
        this.relationshipService = relationshipService;
        this.mapProjectByIdExecutor = mapProjectByIdExecutor;
    }

    @Override
    public void update(IOperationContext context, Map<ExperimentUpdate, ExperimentPE> updatesMap)
    {
        List<IProjectId> projectIds = new LinkedList<IProjectId>();

        for (ExperimentUpdate update : updatesMap.keySet())
        {
            if (update.getProjectId() != null && update.getProjectId().isModified())
            {
                projectIds.add(update.getProjectId().getValue());
            }
        }

        Map<IProjectId, ProjectPE> projectMap = mapProjectByIdExecutor.map(context, projectIds);

        for (Map.Entry<ExperimentUpdate, ExperimentPE> entry : updatesMap.entrySet())
        {
            ExperimentUpdate update = entry.getKey();
            ExperimentPE experiment = entry.getValue();
            update(context, experiment, update.getProjectId(), projectMap);
        }
    }

    private void update(IOperationContext context, ExperimentPE experiment, FieldUpdateValue<IProjectId> update, Map<IProjectId, ProjectPE> projectMap)
    {
        if (update != null && update.isModified())
        {
            if (update.getValue() == null)
            {
                throw new UserFailureException("Project id cannot be null");
            }

            ProjectPE project = projectMap.get(update.getValue());

            if (project == null)
            {
                throw new ObjectNotFoundException(update.getValue());
            }

            if (false == project.equals(experiment.getProject()))
            {
                checkProject(context, update.getValue(), project);
                relationshipService.assignExperimentToProject(context.getSession(), experiment, project);
            }
        }
    }

    private void checkProject(IOperationContext context, IProjectId projectId, ProjectPE project)
    {
        if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), project))
        {
            throw new UnauthorizedObjectAccessException(projectId);
        }
    }
}
