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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleExperimentExecutor implements IVerifySampleExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private VerifySampleExperimentExecutor()
    {
    }

    public VerifySampleExperimentExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public void verify(IOperationContext context, Collection<SamplePE> samples)
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();

        Map<SamplePE, Boolean> haveDatasetsMap = dataDAO.haveDataSets(samples);

        for (SamplePE sample : samples)
        {
            context.pushContextDescription("verify experiment for sample " + sample.getCode());

            boolean hasDatasets = haveDatasetsMap.get(sample);
            ExperimentPE experiment = sample.getExperiment();

            if (hasDatasets && experiment == null)
            {
                throw UserFailureException.fromTemplate(
                        "Cannot detach the sample '%s' from the experiment "
                                + "because there are already datasets attached to the sample.",
                        sample.getIdentifier());
            }

            if (hasDatasets && sample.getSpace() == null)
            {
                throw UserFailureException.fromTemplate("Cannot detach the sample '%s' from the space "
                        + "because there are already datasets attached to the sample.",
                        sample.getIdentifier());
            }

            if (experiment != null && sample.getSpace() == null)
            {
                throw new UserFailureException("Shared samples cannot be attached to experiments. Sample: "
                        + sample.getIdentifier() + ", Experiment: " + experiment.getIdentifier());
            }

            if (experiment != null && experiment.getProject().getSpace().equals(sample.getSpace()) == false)
            {
                throw new UserFailureException("Sample space must be the same as experiment space. "
                        + "Sample: " + sample.getIdentifier() + ", Experiment: " + experiment.getIdentifier());
            }

            context.popContextDescription();
        }
    }

}
