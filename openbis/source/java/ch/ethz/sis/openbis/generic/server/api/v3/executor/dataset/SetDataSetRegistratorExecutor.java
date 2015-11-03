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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.person.IMapPersonByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.IPersonId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetRegistratorExecutor extends AbstractSetEntityToOneRelationExecutor<DataSetCreation, DataPE, IPersonId, PersonPE> implements
        ISetDataSetRegistratorExecutor
{

    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Override
    protected IPersonId getRelatedId(DataSetCreation creation)
    {
        return creation.getRegistratorId();
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, List<IPersonId> relatedIds)
    {
        return mapPersonByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IPersonId relatedId, PersonPE related)
    {
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, PersonPE related)
    {
        PersonPE person = null;

        if (related == null)
        {
            person = context.getSession().tryGetPerson();
        } else
        {
            person = related;
        }

        entity.setRegistrator(person);
        entity.setModifier(person);
    }

}
