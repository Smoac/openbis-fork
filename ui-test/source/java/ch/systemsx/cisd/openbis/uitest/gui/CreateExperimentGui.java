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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.functionality.AbstractExecution;
import ch.systemsx.cisd.openbis.uitest.functionality.CreateExperiment;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterExperimentLocation;
import ch.systemsx.cisd.openbis.uitest.page.RegisterExperiment;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;

/**
 * @author anttil
 */
public class CreateExperimentGui extends AbstractExecution<CreateExperiment, Experiment>
{
    @Override
    public Experiment run(CreateExperiment request)
    {
        Experiment experiment = request.getExperiment();
        RegisterExperiment register = browseTo(new RegisterExperimentLocation());
        register.selectExperimentType(experiment.getType());
        register.fillWith(experiment);
        register.save();
        return experiment;
    }

}
