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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IGetExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleExperimentExecutor implements IUpdateSampleExperimentExecutor
{

    @Autowired
    private IRelationshipService relationshipService;

    @Autowired
    private IGetExperimentByIdExecutor getExperimentByIdExecutor;

    @SuppressWarnings("unused")
    private UpdateSampleExperimentExecutor()
    {
    }

    public UpdateSampleExperimentExecutor(IRelationshipService relationshipService, IGetExperimentByIdExecutor getExperimentByIdExecutor)
    {
        this.relationshipService = relationshipService;
        this.getExperimentByIdExecutor = getExperimentByIdExecutor;
    }

    @Override
    public void update(IOperationContext context, SamplePE sample, FieldUpdateValue<IExperimentId> update)
    {
        if (update != null && update.isModified())
        {
            if (update.getValue() == null)
            {
                if (sample.getExperiment() != null)
                {
                    checkExperiment(context, new ExperimentIdentifier(sample.getExperiment().getIdentifier()), sample.getExperiment());
                    relationshipService.unassignSampleFromExperiment(context.getSession(), sample);
                }
            }
            else
            {
                ExperimentPE experiment = getExperimentByIdExecutor.get(context, update.getValue());
                if (false == experiment.equals(sample.getExperiment()))
                {
                    checkExperiment(context, update.getValue(), experiment);
                    relationshipService.assignSampleToExperiment(context.getSession(), sample, experiment);
                }
            }
        }
    }

    private void checkExperiment(IOperationContext context, IExperimentId experimentId, ExperimentPE experiment)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
        {
            throw new UnauthorizedObjectAccessException(experimentId);
        }
    }

}
