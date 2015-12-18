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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.experiment.IExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
@Component
public class MapExperimentSqlMethodExecutor extends AbstractMapMethodExecutor<IExperimentId, Long, Experiment, ExperimentFetchOptions>
        implements IMapExperimentMethodExecutor
{

    @Autowired
    private IMapExperimentByIdExecutor mapExecutor;

    @Autowired
    private IExperimentTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IExperimentId, Long> getMapExecutor()
    {
        // TODO replace with IExperimentId -> Long mapExecutor once there is one
        return new IMapObjectByIdExecutor<IExperimentId, Long>()
            {
                @Override
                public Map<IExperimentId, Long> map(IOperationContext context, Collection<? extends IExperimentId> ids)
                {
                    Map<IExperimentId, ExperimentPE> peMap = mapExecutor.map(context, ids);
                    Map<IExperimentId, Long> idMap = new LinkedHashMap<IExperimentId, Long>();

                    for (Map.Entry<IExperimentId, ExperimentPE> peEntry : peMap.entrySet())
                    {
                        idMap.put(peEntry.getKey(), peEntry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, Experiment, ExperimentFetchOptions> getTranslator()
    {
        return translator;
    }

}
