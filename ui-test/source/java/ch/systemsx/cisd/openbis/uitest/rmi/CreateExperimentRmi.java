/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.CreateExperiment;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class CreateExperimentRmi extends Executor<CreateExperiment, Experiment>
{

    @Override
    public Experiment run(CreateExperiment request)
    {
        Experiment experiment = request.getExperiment();
        genericServer.registerExperiment(session, convert(experiment),
                new ArrayList<NewAttachment>());
        return experiment;
    }

    private NewExperiment convert(Experiment experiment)
    {
        String experimentId = Identifiers.get(experiment).toString();
        NewExperiment data = new NewExperiment(experimentId, experiment.getType().getCode());
        data.setAttachments(new ArrayList<NewAttachment>());
        data.setGenerateCodes(false);
        data.setNewSamples(null);
        data.setProperties(new IEntityProperty[0]);
        data.setRegisterSamples(false);

        String[] sampleIds = new String[experiment.getSamples().size()];
        int i = 0;
        for (Sample sample : experiment.getSamples())
        {
            sampleIds[i] = sample.getCode();
            i++;
        }

        data.setSamples(sampleIds);
        return data;
    }
}
