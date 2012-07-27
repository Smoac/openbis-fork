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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * An object that captures the state for performing the registration of one or many openBIS entities
 * atomically.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetails<T extends DataSetInformation> implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Used to uniquely identify the registration in the database
    private final TechId registrationId;

    // The userid on whose behalf the operations are done.
    private final String userIdOrNull;

    private final List<NewSpace> spaceRegistrations;

    private final List<NewProject> projectRegistrations;

    private final List<ExperimentUpdatesDTO> experimentUpdates;

    private final List<NewExperiment> experimentRegistrations;

    private final List<SampleUpdatesDTO> sampleUpdates;

    private final List<NewSample> sampleRegistrations;

    private final Map<String /* material type */, List<NewMaterial>> materialRegistrations;

    private final List<MaterialUpdateDTO> materialUpdates;

    private final List<DataSetRegistrationInformation<T>> dataSetRegistrations;

    private final List<DataSetBatchUpdatesDTO> dataSetUpdates;

    public AtomicEntityOperationDetails(TechId registrationId, String userIdOrNull,
            List<NewSpace> spaceRegistrations, List<NewProject> projectRegistrations,
            List<ExperimentUpdatesDTO> experimentUpdates,
            List<NewExperiment> experimentRegistrations, List<SampleUpdatesDTO> sampleUpdates,
            List<NewSample> sampleRegistrations,
            Map<String, List<NewMaterial>> materialRegistrations,
            List<MaterialUpdateDTO> materialUpdates,
            List<DataSetRegistrationInformation<T>> dataSetRegistrations,
            List<DataSetBatchUpdatesDTO> dataSetUpdates)
    {
        this.registrationId = registrationId;
        this.userIdOrNull = userIdOrNull;
        this.spaceRegistrations = new ArrayList<NewSpace>(spaceRegistrations);
        this.projectRegistrations = new ArrayList<NewProject>(projectRegistrations);
        this.experimentUpdates = new ArrayList<ExperimentUpdatesDTO>(experimentUpdates);
        this.experimentRegistrations = new ArrayList<NewExperiment>(experimentRegistrations);
        this.sampleUpdates = new ArrayList<SampleUpdatesDTO>(sampleUpdates);
        this.sampleRegistrations = new ArrayList<NewSample>(sampleRegistrations);
        this.materialRegistrations = new HashMap<String, List<NewMaterial>>(materialRegistrations);
        this.dataSetRegistrations =
                new ArrayList<DataSetRegistrationInformation<T>>(dataSetRegistrations);
        this.dataSetUpdates = new ArrayList<DataSetBatchUpdatesDTO>(dataSetUpdates);
        this.materialUpdates = materialUpdates;
    }

    public TechId getRegistrationId()
    {
        return registrationId;
    }

    public String tryUserIdOrNull()
    {
        return userIdOrNull;
    }

    public List<ExperimentUpdatesDTO> getExperimentUpdates()
    {
        return experimentUpdates;
    }

    public List<NewExperiment> getExperimentRegistrations()
    {
        return experimentRegistrations;
    }

    public List<SampleUpdatesDTO> getSampleUpdates()
    {
        return sampleUpdates;
    }

    public List<NewSample> getSampleRegistrations()
    {
        return sampleRegistrations;
    }

    public List<DataSetRegistrationInformation<T>> getDataSetRegistrations()
    {
        return dataSetRegistrations;
    }

    public List<DataSetBatchUpdatesDTO> getDataSetUpdates()
    {
        return dataSetUpdates;
    }

    public List<NewSpace> getSpaceRegistrations()
    {
        return spaceRegistrations;
    }

    public List<NewProject> getProjectRegistrations()
    {
        return projectRegistrations;
    }

    public Map<String, List<NewMaterial>> getMaterialRegistrations()
    {
        return materialRegistrations;
    }

    public List<MaterialUpdateDTO> getMaterialUpdates()
    {
        return materialUpdates;
    }
}
