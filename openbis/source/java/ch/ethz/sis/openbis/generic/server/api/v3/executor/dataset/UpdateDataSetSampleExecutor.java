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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityFieldUpdateValueRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetSampleExecutor extends AbstractUpdateEntityFieldUpdateValueRelationExecutor<DataSetUpdate, DataPE, ISampleId, SamplePE>
        implements
        IUpdateDataSetSampleExecutor
{

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Override
    protected ISampleId getRelatedId(SamplePE related)
    {
        return new SampleIdentifier(related.getIdentifier());
    }

    @Override
    protected SamplePE getCurrentlyRelated(DataPE entity)
    {
        return entity.tryGetSample();
    }

    @Override
    protected FieldUpdateValue<ISampleId> getRelatedUpdate(DataSetUpdate update)
    {
        return update.getSampleId();
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<ISampleId> relatedIds)
    {
        return mapSampleByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, ISampleId relatedId, SamplePE related)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, DataPE entity, SamplePE related)
    {
        if (related != null)
        {
            this.boFactory.createDataBO(context.getSession()).assignDataSetToSampleAndExperiment(entity, related, related.getExperiment());
        } else
        {
            this.boFactory.createDataBO(context.getSession()).assignDataSetToSampleAndExperiment(entity, null, entity.getExperiment());
        }
    }

}
