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
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql.ISampleSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class MapSampleSqlMethodExecutor extends AbstractMapMethodExecutor<ISampleId, Long, Sample, SampleFetchOptions> implements
        IMapSampleMethodExecutor
{

    @Autowired
    private IMapSampleByIdExecutor mapExecutor;

    @Autowired
    private ISampleSqlTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<ISampleId, Long> getMapExecutor()
    {
        // TODO replace with ISampleId -> Long mapExecutor once there is one
        return new IMapObjectByIdExecutor<ISampleId, Long>()
            {
                @Override
                public Map<ISampleId, Long> map(IOperationContext context, Collection<? extends ISampleId> ids)
                {
                    Map<ISampleId, SamplePE> peMap = mapExecutor.map(context, ids);
                    Map<ISampleId, Long> idMap = new LinkedHashMap<ISampleId, Long>();

                    for (Map.Entry<ISampleId, SamplePE> peEntry : peMap.entrySet())
                    {
                        idMap.put(peEntry.getKey(), peEntry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, Sample, SampleFetchOptions> getTranslator()
    {
        return translator;
    }

}
