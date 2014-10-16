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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleTypeExecutor implements ISetSampleTypeExecutor
{

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Override
    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap)
    {
        List<IEntityTypeId> typeIds = new LinkedList<IEntityTypeId>();

        for (SampleCreation creation : creationsMap.keySet())
        {
            if (creation.getTypeId() != null)
            {
                typeIds.add(creation.getTypeId());
            }
        }

        Map<IEntityTypeId, EntityTypePE> typeMap = mapEntityTypeByIdExecutor.map(context, EntityKind.SAMPLE, typeIds);

        for (Map.Entry<SampleCreation, SamplePE> creationEntry : creationsMap.entrySet())
        {
            SampleCreation creation = creationEntry.getKey();
            SamplePE sample = creationEntry.getValue();

            context.pushContextDescription("set type for sample " + creation.getCode());

            if (creation.getTypeId() == null)
            {
                throw new UserFailureException("No type for sample provided");
            } else
            {
                EntityTypePE type = typeMap.get(creation.getTypeId());
                if (type == null)
                {
                    throw new ObjectNotFoundException(creation.getTypeId());
                }
                sample.setSampleType((SampleTypePE) type);
            }

            context.popContextDescription();
        }
    }
}
