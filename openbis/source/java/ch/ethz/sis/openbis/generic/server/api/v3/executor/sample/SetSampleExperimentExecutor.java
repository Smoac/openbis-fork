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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleExperimentExecutor implements ISetSampleExperimentExecutor
{

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Override
    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap)
    {
        List<IExperimentId> experimentIds = new LinkedList<IExperimentId>();

        for (SampleCreation creation : creationsMap.keySet())
        {
            if (creation.getExperimentId() != null)
            {
                experimentIds.add(creation.getExperimentId());
            }
        }

        Map<IExperimentId, ExperimentPE> experimentMap = mapExperimentByIdExecutor.map(context, experimentIds);

        for (Map.Entry<SampleCreation, SamplePE> creationEntry : creationsMap.entrySet())
        {
            SampleCreation creation = creationEntry.getKey();
            SamplePE sample = creationEntry.getValue();

            context.pushContextDescription("set experiment for sample " + creation.getCode());

            if (creation.getExperimentId() != null)
            {
                ExperimentPE experiment = experimentMap.get(creation.getExperimentId());
                if (experiment == null)
                {
                    throw new ObjectNotFoundException(creation.getExperimentId());
                }

                if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
                {
                    throw new AuthorizationFailureException("Cannot access experiment " + creation.getExperimentId());
                }

                sample.setExperiment(experiment);
            }

            context.popContextDescription();
        }
    }
}
