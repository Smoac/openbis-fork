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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
@Component
public class GetExperimentByIdExecutor implements IGetExperimentByIdExecutor
{

    @Autowired
    private IListExperimentByIdExecutor listExperimentByIdExecutor;

    @SuppressWarnings("unused")
    private GetExperimentByIdExecutor()
    {
    }

    public GetExperimentByIdExecutor(IListExperimentByIdExecutor listExperimentByIdExecutor)
    {
        this.listExperimentByIdExecutor = listExperimentByIdExecutor;
    }

    @Override
    public ExperimentPE get(IOperationContext context, IExperimentId experimentId)
    {
        List<ExperimentPE> experiments = listExperimentByIdExecutor.list(context, Collections.singletonList(experimentId));

        if (experiments.isEmpty())
        {
            throw new UserFailureException("No experiment for id " + experimentId);
        }

        return experiments.get(0);
    }

}
