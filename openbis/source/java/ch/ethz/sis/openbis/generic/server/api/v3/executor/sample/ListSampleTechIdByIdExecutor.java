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

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class ListSampleTechIdByIdExecutor implements IListSampleTechIdByIdExecutor
{

    @Autowired
    private IListSampleByIdExecutor listSampleByIdExecutor;

    @SuppressWarnings("unused")
    private ListSampleTechIdByIdExecutor()
    {
    }

    public ListSampleTechIdByIdExecutor(IListSampleByIdExecutor listSampleByIdExecutor)
    {
        this.listSampleByIdExecutor = listSampleByIdExecutor;
    }

    @Override
    public Collection<Long> list(IOperationContext context, Collection<? extends ISampleId> sampleIds)
    {
        List<SamplePE> samples = listSampleByIdExecutor.list(context, sampleIds);

        return org.apache.commons.collections.CollectionUtils.collect(samples, new Transformer<SamplePE, Long>()
            {
                @Override
                public Long transform(SamplePE input)
                {
                    return input.getId();
                }
            });
    }
}
