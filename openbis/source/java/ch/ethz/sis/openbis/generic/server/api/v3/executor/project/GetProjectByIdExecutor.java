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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class GetProjectByIdExecutor implements IGetProjectByIdExecutor
{

    @Autowired
    private ITryGetProjectByIdExecutor tryGetProjectByIdExecutor;

    @SuppressWarnings("unused")
    private GetProjectByIdExecutor()
    {
    }

    public GetProjectByIdExecutor(ITryGetProjectByIdExecutor tryGetProjectByIdExecutor)
    {
        this.tryGetProjectByIdExecutor = tryGetProjectByIdExecutor;
    }

    @Override
    public ProjectPE get(IOperationContext context, IProjectId projectId)
    {
        if (projectId == null)
        {
            throw new UserFailureException("Unspecified project id.");
        }
        ProjectPE project = tryGetProjectByIdExecutor.tryGet(context, projectId);
        if (project == null)
        {
            throw new UserFailureException("No project found with this id: " + projectId);
        }
        return project;
    }

}
