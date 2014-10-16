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

import java.util.Collections;
import java.util.Map;

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
public class TryGetProjectByIdExecutor implements ITryGetProjectByIdExecutor
{

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @SuppressWarnings("unused")
    private TryGetProjectByIdExecutor()
    {
    }

    public TryGetProjectByIdExecutor(IMapProjectByIdExecutor mapProjectByIdExecutor)
    {
        this.mapProjectByIdExecutor = mapProjectByIdExecutor;
    }

    @Override
    public ProjectPE tryGet(IOperationContext context, IProjectId projectId)
    {
        if (projectId == null)
        {
            throw new UserFailureException("Unspecified project id.");
        }

        Map<IProjectId, ProjectPE> projects = mapProjectByIdExecutor.map(context, Collections.singletonList(projectId));
        return projects.get(projectId);
    }

}
