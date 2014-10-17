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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleContainerExecutor implements IUpdateSampleContainerExecutor
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    private IRelationshipService relationshipService;

    @Override
    public void update(IOperationContext context, Map<SampleUpdate, SamplePE> updateMap, Map<ISampleId, SamplePE> samplesMap)
    {
        for (SampleUpdate update : updateMap.keySet())
        {
            SamplePE sample = updateMap.get(update);
            FieldUpdateValue<ISampleId> containerUpdate = update.getContainerId();

            if (containerUpdate != null && containerUpdate.isModified())
            {
                if (containerUpdate.getValue() == null)
                {
                    if (sample.getContainer() != null)
                    {
                        checkContainer(context, new SampleIdentifier(sample.getContainer().getIdentifier()), sample.getContainer());
                        UpdateSampleContainedExecutor.removeFromContainer(relationshipService, context, sample, sample.getContainer());
                    }
                } else
                {
                    SamplePE container = samplesMap.get(containerUpdate.getValue());
                    if (container == null)
                    {
                        throw new ObjectNotFoundException(containerUpdate.getValue());
                    }
                    checkContainer(context, containerUpdate.getValue(), container);
                    UpdateSampleContainedExecutor.assignToContainer(relationshipService, context, sample, container);
                }
            }
        }
    }

    private void checkContainer(IOperationContext context, ISampleId containerId, SamplePE container)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), container))
        {
            throw new UnauthorizedObjectAccessException(containerId);
        }
    }

}
