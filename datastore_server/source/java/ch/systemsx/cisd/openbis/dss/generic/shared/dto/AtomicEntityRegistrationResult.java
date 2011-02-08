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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityRegistrationResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final ArrayList<Experiment> experimentsUpdated;

    private final ArrayList<Experiment> experimentsCreated;

    private final ArrayList<Sample> samplesUpdated;

    private final ArrayList<Sample> samplesCreated;

    private final ArrayList<DataSetInformation> dataSetsCreated;

    public AtomicEntityRegistrationResult(List<Experiment> experimentsUpdated,
            List<Experiment> experimentsCreated, List<Sample> samplesUpdated,
            List<Sample> samplesCreated, List<DataSetInformation> dataSetsCreated)
    {
        this.experimentsUpdated = new ArrayList<Experiment>(experimentsUpdated);
        this.experimentsCreated = new ArrayList<Experiment>(experimentsCreated);
        this.samplesUpdated = new ArrayList<Sample>(samplesUpdated);
        this.samplesCreated = new ArrayList<Sample>(samplesCreated);
        this.dataSetsCreated = new ArrayList<DataSetInformation>(dataSetsCreated);
    }

    public ArrayList<Experiment> getExperimentsUpdated()
    {
        return experimentsUpdated;
    }

    public ArrayList<Experiment> getExperimentsCreated()
    {
        return experimentsCreated;
    }

    public ArrayList<Sample> getSamplesUpdated()
    {
        return samplesUpdated;
    }

    public ArrayList<Sample> getSamplesCreated()
    {
        return samplesCreated;
    }

    public ArrayList<DataSetInformation> getDataSetsCreated()
    {
        return dataSetsCreated;
    }

}
