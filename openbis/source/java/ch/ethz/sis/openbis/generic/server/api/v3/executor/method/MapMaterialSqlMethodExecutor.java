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
import ch.ethz.sis.openbis.generic.server.api.v3.executor.material.IMapMaterialByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql.IMaterialSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author pkupczyk
 */
@Component
public class MapMaterialSqlMethodExecutor extends AbstractMapMethodExecutor<IMaterialId, Long, Material, MaterialFetchOptions> implements
        IMapMaterialMethodExecutor
{

    @Autowired
    private IMapMaterialByIdExecutor mapExecutor;

    @Autowired
    private IMaterialSqlTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IMaterialId, Long> getMapExecutor()
    {
        // TODO replace with IMaterialId -> Long mapExecutor once there is one
        return new IMapObjectByIdExecutor<IMaterialId, Long>()
            {
                @Override
                public Map<IMaterialId, Long> map(IOperationContext context, Collection<? extends IMaterialId> ids)
                {
                    Map<IMaterialId, MaterialPE> peMap = mapExecutor.map(context, ids);
                    Map<IMaterialId, Long> idMap = new LinkedHashMap<IMaterialId, Long>();

                    for (Map.Entry<IMaterialId, MaterialPE> peEntry : peMap.entrySet())
                    {
                        idMap.put(peEntry.getKey(), peEntry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, Material, MaterialFetchOptions> getTranslator()
    {
        return translator;
    }

}
