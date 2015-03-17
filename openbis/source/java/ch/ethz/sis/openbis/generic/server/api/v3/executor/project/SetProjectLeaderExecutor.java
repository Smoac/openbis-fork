/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.IPersonId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class SetProjectLeaderExecutor extends AbstractSetEntityRelationExecutor<ProjectCreation, ProjectPE, IPersonId, PersonPE> implements
        ISetProjectLeaderExecutor
{

    @Override
    protected IPersonId getRelatedId(ProjectCreation creation)
    {
        return creation.getLeaderId();
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, List<IPersonId> relatedIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void check(IOperationContext context, ProjectPE entity, IPersonId relatedId, PersonPE related)
    {
        // nothing to do
    }

    @Override
    protected void set(IOperationContext context, ProjectPE entity, PersonPE related)
    {
        entity.setProjectLeader(related);
    }

}
