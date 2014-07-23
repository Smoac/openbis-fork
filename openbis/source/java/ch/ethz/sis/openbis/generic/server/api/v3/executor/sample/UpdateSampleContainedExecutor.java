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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleContainedExecutor extends AbstractUpdateSampleRelatedSamplesExecutor implements IUpdateSampleContainedExecutor
{

    @SuppressWarnings("unused")
    private UpdateSampleContainedExecutor()
    {
    }

    public UpdateSampleContainedExecutor(IDAOFactory daoFactory)
    {
        super(daoFactory);
    }

    @Override
    protected ListUpdateValue<? extends ISampleId> getRelatedSamplesUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getContainedIds();
    }

    @Override
    protected void setRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesIds)
    {
        getDaoFactory().getSampleDAO().setSampleContained(sample.getId(), relatedSamplesIds);
    }

    @Override
    protected void addRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesIds)
    {
        getDaoFactory().getSampleDAO().addSampleContained(sample.getId(), relatedSamplesIds);
    }

    @Override
    protected void removeRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesIds)
    {
        getDaoFactory().getSampleDAO().removeSampleContained(sample.getId(), relatedSamplesIds);
    }

}
