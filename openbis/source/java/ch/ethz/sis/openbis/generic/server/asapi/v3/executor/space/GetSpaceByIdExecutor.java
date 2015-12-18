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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class GetSpaceByIdExecutor implements IGetSpaceByIdExecutor
{

    @Autowired
    private ITryGetSpaceByIdExecutor tryGetSpaceByIdExecutor;

    @SuppressWarnings("unused")
    private GetSpaceByIdExecutor()
    {
    }

    public GetSpaceByIdExecutor(ITryGetSpaceByIdExecutor tryGetSpaceByIdExecutor)
    {
        this.tryGetSpaceByIdExecutor = tryGetSpaceByIdExecutor;
    }

    @Override
    public SpacePE get(IOperationContext context, ISpaceId spaceId)
    {
        SpacePE space = tryGetSpaceByIdExecutor.tryGet(context, spaceId);
        if (space == null)
        {
            throw new ObjectNotFoundException(spaceId);
        }
        return space;
    }

}
