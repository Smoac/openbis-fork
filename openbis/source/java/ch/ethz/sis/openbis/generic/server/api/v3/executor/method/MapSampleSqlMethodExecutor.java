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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IMapSampleTechIdByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.ISampleTranslator;

/**
 * @author pkupczyk
 */
@Component
public class MapSampleSqlMethodExecutor extends AbstractMapMethodExecutor<ISampleId, Long, Sample, SampleFetchOptions> implements
        IMapSampleMethodExecutor
{

    @Autowired
    private IMapSampleTechIdByIdExecutor mapExecutor;

    @Autowired
    private ISampleTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<ISampleId, Long> getMapExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Sample, SampleFetchOptions> getTranslator()
    {
        return translator;
    }

}
