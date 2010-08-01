/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * A {@link WellContent} with the {@link Experiment} that the well is in.
 *
 * @author Bernd Rinn
 */
public class WellContentWithExperiment extends WellContent
{
    private Experiment experiment;
    
    public WellContentWithExperiment(WellLocation locationOrNull, EntityReference well,
            EntityReference plate, EntityReference materialContent, Experiment experiment)
    {
        super(locationOrNull, well, plate, materialContent);
        this.experiment = experiment;
    }

    /**
     * Returns the experiment that the well is in.
     */
    public Experiment getExperiment()
    {
        return experiment;
    }

}
