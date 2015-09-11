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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.sql.ISpaceSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class MapSpaceSqlMethodExecutor extends AbstractMapMethodExecutor<ISpaceId, Long, Space, SpaceFetchOptions> implements IMapSpaceMethodExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapExecutor;

    @Autowired
    private ISpaceSqlTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<ISpaceId, Long> getMapExecutor()
    {
        // TODO replace with ISpaceId -> Long mapExecutor once there is one
        return new IMapObjectByIdExecutor<ISpaceId, Long>()
            {
                @Override
                public Map<ISpaceId, Long> map(IOperationContext context, Collection<? extends ISpaceId> ids)
                {
                    Map<ISpaceId, SpacePE> peMap = mapExecutor.map(context, ids);
                    Map<ISpaceId, Long> idMap = new LinkedHashMap<ISpaceId, Long>();

                    for (Map.Entry<ISpaceId, SpacePE> peEntry : peMap.entrySet())
                    {
                        idMap.put(peEntry.getKey(), peEntry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, Space, SpaceFetchOptions> getTranslator()
    {
        return translator;
    }

}
