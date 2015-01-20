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
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class SetExperimentTypeExecutor implements ISetExperimentTypeExecutor
{

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Override
    public void set(IOperationContext context, Map<ExperimentCreation, ExperimentPE> creationsMap)
    {
        List<IEntityTypeId> typeIds = new LinkedList<IEntityTypeId>();

        for (ExperimentCreation creation : creationsMap.keySet())
        {
            if (creation.getTypeId() != null)
            {
                typeIds.add(creation.getTypeId());
            }
        }

        Map<IEntityTypeId, EntityTypePE> typeMap = mapEntityTypeByIdExecutor.map(context, EntityKind.EXPERIMENT, typeIds);

        for (Map.Entry<ExperimentCreation, ExperimentPE> creationEntry : creationsMap.entrySet())
        {
            ExperimentCreation creation = creationEntry.getKey();
            ExperimentPE experiment = creationEntry.getValue();

            context.pushContextDescription("set type for experiment " + creation.getCode());

            if (creation.getTypeId() == null)
            {
                throw new UserFailureException("Type id cannot be null.");
            } else
            {
                EntityTypePE type = typeMap.get(creation.getTypeId());
                if (type == null)
                {
                    throw new ObjectNotFoundException(creation.getTypeId());
                }
                experiment.setExperimentType((ExperimentTypePE) type);
            }

            context.popContextDescription();
        }
    }
}
