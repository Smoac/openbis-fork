/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class ExperimentImmutable implements IExperimentImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment;

    public ExperimentImmutable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment)
    {
        this.experiment = experiment;
    }

    public String getExperimentIdentifier()
    {
        return experiment.getIdentifier();
    }

    public boolean isExistingExperiment()
    {
        return true;
    }

    /**
     * Throw an exception if the sample does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingExperiment())
        {
            throw new UserFailureException("Experiment does not exist.");
        }
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment getExperiment()
    {
        return experiment;
    }

    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(experiment, propertyCode);
    }

    public String getType()
    {
        if (experiment.getExperimentType() != null)
        {
            return experiment.getExperimentType().getCode();
        }
        return null;
    }

}
