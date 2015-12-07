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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.entity;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetEntityExperimentRelationExecutor<ENTITY_CREATION, ENTITY_PE> extends
        AbstractSetEntityToOneRelationExecutor<ENTITY_CREATION, ENTITY_PE, IExperimentId, ExperimentPE>
{

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, List<IExperimentId> relatedIds)
    {
        return mapExperimentByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ENTITY_PE entity, IExperimentId relatedId, ExperimentPE related)
    {
        if (relatedId != null && related != null)
        {
            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
            {
                throw new UnauthorizedObjectAccessException(relatedId);
            }
        }
    }

}
