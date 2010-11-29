/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Tomasz Pylak
 */
public class SampleUtils
{
    public static final void checkSampleWithoutDatasets(IExternalDataDAO externalDataDAO,
            SamplePE sample)
    {
        if (hasDatasets(externalDataDAO, sample))
        {
            throw UserFailureException
                    .fromTemplate(
                            "Operation cannot be performed, because some datasets have been already produced for the sample '%s'.",
                            sample.getSampleIdentifier());
        }
    }

    public static boolean hasDatasets(IExternalDataDAO externalDataDAO, SamplePE sample)
    {
        assert sample != null;

        return externalDataDAO.hasExternalData(sample); 
    }

    /** for all experiment samples which belonged to a group the specified group will be set */
    public static void setSamplesGroup(ExperimentPE experiment, SpacePE group)
    {
        for (SamplePE sample : experiment.getSamples())
        {
            if (sample.getSpace() != null)
            {
                sample.setSpace(group);
            }
        }
    }

}
