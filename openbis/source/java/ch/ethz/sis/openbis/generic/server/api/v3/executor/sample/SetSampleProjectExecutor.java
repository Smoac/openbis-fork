/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SetSampleProjectExecutor extends AbstractSetEntityToOneRelationExecutor<SampleCreation, SamplePE, IProjectId, ProjectPE>
    implements ISetSampleProjectExecutor
{
    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Override
    protected IProjectId getRelatedId(SampleCreation creation)
    {
        return creation.getProjectId();
    }

    @Override
    protected Map<IProjectId, ProjectPE> map(IOperationContext context, List<IProjectId> relatedIds)
    {
        return mapProjectByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, IProjectId relatedId, ProjectPE related)
    {
        if (relatedId != null && related != null)
        {
            if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
            {
                throw new UnauthorizedObjectAccessException(relatedId);
            }
        }
    }

    @Override
    protected void set(IOperationContext context, SamplePE entity, ProjectPE related)
    {
        entity.setProject(related);
    }

}
