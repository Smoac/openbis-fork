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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IGetProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
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
    private IGetProjectByIdExecutor getProjectByIdExecutor;

    @SuppressWarnings("unused")
    private UpdateExperimentProjectExecutor()
    {
    }

    public UpdateExperimentProjectExecutor(IRelationshipService relationshipService, IGetProjectByIdExecutor getProjectByIdExecutor)
    {
        this.relationshipService = relationshipService;
        this.getProjectByIdExecutor = getProjectByIdExecutor;
    }

    @Override
    public void update(IOperationContext context, ExperimentPE experiment, FieldUpdateValue<IProjectId> update)
    {
        if (update != null && update.isModified())
        {
            ProjectPE project = getProjectByIdExecutor.get(context, update.getValue());
            if (false == project.equals(experiment.getProject()))
            {
                relationshipService.assignExperimentToProject(context.getSession(), experiment, project);
            }
        }
    }

}
