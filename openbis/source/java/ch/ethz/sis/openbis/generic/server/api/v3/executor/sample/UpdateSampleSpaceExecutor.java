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
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
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
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @SuppressWarnings("unused")
    private UpdateSampleSpaceExecutor()
    {
    }

    public UpdateSampleSpaceExecutor(IRelationshipService relationshipService, IMapSpaceByIdExecutor mapSpaceByIdExecutor)
    {
        this.relationshipService = relationshipService;
        this.mapSpaceByIdExecutor = mapSpaceByIdExecutor;
    }

    @Override
    public void update(IOperationContext context, Map<SampleUpdate, SamplePE> updatesMap)
    {
        List<ISpaceId> spaceIds = new LinkedList<ISpaceId>();

        for (SampleUpdate update : updatesMap.keySet())
        {
            if (update.getSpaceId() != null && update.getSpaceId().isModified())
            {
                spaceIds.add(update.getSpaceId().getValue());
            }
        }

        Map<ISpaceId, SpacePE> spaceMap = mapSpaceByIdExecutor.map(context, spaceIds);

        for (Map.Entry<SampleUpdate, SamplePE> entry : updatesMap.entrySet())
        {
            SampleUpdate update = entry.getKey();
            SamplePE sample = entry.getValue();
            update(context, sample, update.getSpaceId(), spaceMap);
        }
    }

    private void update(IOperationContext context, SamplePE sample, FieldUpdateValue<ISpaceId> update, Map<ISpaceId, SpacePE> spaceMap)
    {
        if (update != null && update.isModified())
        {
            if (update.getValue() == null)
            {
                if (sample.getSpace() != null)
                {
                    checkSpace(context, new SpacePermId(sample.getSpace().getCode()), sample.getSpace());
                    relationshipService.shareSample(context.getSession(), sample);
                }
            }
            else
            {
                SpacePE space = spaceMap.get(update.getValue());

                if (space == null)
                {
                    throw new ObjectNotFoundException(update.getValue());
                }

                checkSpace(context, update.getValue(), space);

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

    private void checkSpace(IOperationContext context, ISpaceId spaceId, SpacePE space)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), space))
        {
            throw new UnauthorizedObjectAccessException(spaceId);
        }
    }
}
