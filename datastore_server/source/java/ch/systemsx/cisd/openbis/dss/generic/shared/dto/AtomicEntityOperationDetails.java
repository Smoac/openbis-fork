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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * An object that captures the state for performing the registration of one or many openBIS entities
 * atomically.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetails<T extends DataSetInformation> implements
        Serializable
{
    private static final long serialVersionUID = 1L;

    private final ArrayList<ExperimentUpdatesDTO> experimentUpdates;

    private final ArrayList<NewExperiment> experimentRegistrations;

    private final ArrayList<SampleUpdatesDTO> sampleUpdates;

    private final ArrayList<NewSample> sampleRegistrations;

    private final ArrayList<DataSetRegistrationInformation<T>> dataSetRegistrations;

    public AtomicEntityOperationDetails(List<ExperimentUpdatesDTO> experimentUpdates,
            List<NewExperiment> experimentRegistrations, List<SampleUpdatesDTO> sampleUpdates,
            List<NewSample> sampleRegistrations,
            List<DataSetRegistrationInformation<T>> dataSetRegistrations)
    {
        this.experimentUpdates = new ArrayList<ExperimentUpdatesDTO>(experimentUpdates);
        this.experimentRegistrations = new ArrayList<NewExperiment>(experimentRegistrations);
        this.sampleUpdates = new ArrayList<SampleUpdatesDTO>(sampleUpdates);
        this.sampleRegistrations = new ArrayList<NewSample>(sampleRegistrations);
        this.dataSetRegistrations =
                new ArrayList<DataSetRegistrationInformation<T>>(dataSetRegistrations);
    }

    public ArrayList<ExperimentUpdatesDTO> getExperimentUpdates()
    {
        return experimentUpdates;
    }

    public ArrayList<NewExperiment> getExperimentRegistrations()
    {
        return experimentRegistrations;
    }

    public ArrayList<SampleUpdatesDTO> getSampleUpdates()
    {
        return sampleUpdates;
    }

    public ArrayList<NewSample> getSampleRegistrations()
    {
        return sampleRegistrations;
    }

    public ArrayList<DataSetRegistrationInformation<T>> getDataSetRegistrations()
    {
        return dataSetRegistrations;
    }
}
