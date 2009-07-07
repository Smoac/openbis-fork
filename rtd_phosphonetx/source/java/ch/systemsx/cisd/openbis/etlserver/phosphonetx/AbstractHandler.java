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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractHandler
{
    protected final IProtDAO dao;

    AbstractHandler(IProtDAO dao)
    {
        this.dao = dao;
    }
    
    protected Sample getOrCreateSample(Experiment experiment, String samplePermID)
    {
        Sample sample = dao.tryToGetSampleByPermID(samplePermID);
        if (sample == null)
        {
            sample = new Sample();
            sample.setPermID(samplePermID);
            sample.setId(dao.createSample(experiment.getId(), samplePermID));
        }
        return sample;
    }

}
