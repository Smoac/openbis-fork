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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IGetSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleSpaceExecutor implements IUpdateSampleSpaceExecutor
{

    @Autowired
    private IRelationshipService relationshipService;

    @Autowired
    private IGetSpaceByIdExecutor getSpaceByIdExecutor;

    @SuppressWarnings("unused")
    private UpdateSampleSpaceExecutor()
    {
    }

    public UpdateSampleSpaceExecutor(IRelationshipService relationshipService, IGetSpaceByIdExecutor getSpaceByIdExecutor)
    {
        this.relationshipService = relationshipService;
        this.getSpaceByIdExecutor = getSpaceByIdExecutor;
    }

    @Override
    public void update(IOperationContext context, SamplePE sample, FieldUpdateValue<ISpaceId> update)
    {
        if (update != null && update.isModified())
        {
            if (update.getValue() == null)
            {
                if (sample.getSpace() != null)
                {
                    relationshipService.shareSample(context.getSession(), sample);
                }
            }
            else
            {
                SpacePE space = getSpaceByIdExecutor.get(context, update.getValue());

                if (sample.getSpace() == null)
                {
                    relationshipService.unshareSample(context.getSession(), sample, space);
                } else if (false == sample.getSpace().equals(space))
                {
                    relationshipService.assignSampleToSpace(context.getSession(), sample, space);
                }
            }
        }
    }

}
