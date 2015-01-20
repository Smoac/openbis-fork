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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class SetExperimentProjectExecutor implements ISetExperimentProjectExecutor
{

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Override
    public void set(IOperationContext context, Map<ExperimentCreation, ExperimentPE> creationsMap)
    {
        List<IProjectId> projectIds = new LinkedList<IProjectId>();

        for (ExperimentCreation creation : creationsMap.keySet())
        {
            if (creation.getProjectId() != null)
            {
                projectIds.add(creation.getProjectId());
            }
        }

        Map<IProjectId, ProjectPE> projectMap = mapProjectByIdExecutor.map(context, projectIds);

        for (Map.Entry<ExperimentCreation, ExperimentPE> creationEntry : creationsMap.entrySet())
        {
            ExperimentCreation creation = creationEntry.getKey();
            ExperimentPE experiment = creationEntry.getValue();

            context.pushContextDescription("set project for experiment " + creation.getCode());

            if (creation.getProjectId() == null)
            {
                throw new UserFailureException("Project id cannot be null.");
            } else
            {
                ProjectPE project = projectMap.get(creation.getProjectId());
                if (project == null)
                {
                    throw new ObjectNotFoundException(creation.getProjectId());
                }

                if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), project))
                {
                    throw new UnauthorizedObjectAccessException(creation.getProjectId());
                }

                experiment.setProject(project);
            }

            context.popContextDescription();
        }
    }
}
