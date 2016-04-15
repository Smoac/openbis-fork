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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IMapTagByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.tag.ITagTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class GetTagMethodExecutor extends AbstractGetMethodExecutor<ITagId, Long, Tag, TagFetchOptions> implements
        IGetTagMethodExecutor
{

    @Autowired
    private IMapTagByIdExecutor mapExecutor;

    @Autowired
    private ITagTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<ITagId, Long> getMapExecutor()
    {
        return new IMapObjectByIdExecutor<ITagId, Long>()
            {
                @Override
                public Map<ITagId, Long> map(IOperationContext context, Collection<? extends ITagId> ids)
                {
                    Map<ITagId, Long> idMap = new LinkedHashMap<ITagId, Long>();
                    Map<ITagId, MetaprojectPE> peMap = mapExecutor.map(context, ids);

                    for (Map.Entry<ITagId, MetaprojectPE> entry : peMap.entrySet())
                    {
                        idMap.put(entry.getKey(), entry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, Tag, TagFetchOptions> getTranslator()
    {
        return translator;
    }

}
