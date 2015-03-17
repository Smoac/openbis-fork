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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleExperimentExecutor extends AbstractSetEntityRelationExecutor<SampleCreation, SamplePE, IExperimentId, ExperimentPE> implements
        ISetSampleExperimentExecutor
{

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Override
    protected IExperimentId getRelatedId(SampleCreation creation)
    {
        return creation.getExperimentId();
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, List<IExperimentId> relatedIds)
    {
        return mapExperimentByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, IExperimentId relatedId, ExperimentPE related)
    {
        if (relatedId != null && related != null)
        {
            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
            {
                throw new UnauthorizedObjectAccessException(relatedId);
            }
        }
    }

    @Override
    protected void set(IOperationContext context, SamplePE entity, ExperimentPE related)
    {
        entity.setExperiment(related);
    }
}
