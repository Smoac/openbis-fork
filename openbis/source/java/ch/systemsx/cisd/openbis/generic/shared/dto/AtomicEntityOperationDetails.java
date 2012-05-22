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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;

/**
 * An object that captures the state for performing the registration of one or many openBIS entities
 * atomically.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetails implements Serializable
{
    private static final long serialVersionUID = 1L;

    // The unique identifier for this registration
    private final TechId registrationIdOrNull;

    // The userid on whose behalf the operations are done.
    private final String userIdOrNull;

    // This is always an empty list, since it is not currently possible to update experiments from
    // the DSS
    private final List<ExperimentUpdatesDTO> experimentUpdates;

    private final List<NewSpace> spaceRegistrations;

    private final List<NewProject> projectRegistrations;

    private final List<NewExperiment> experimentRegistrations;

    private final List<SampleUpdatesDTO> sampleUpdates;

    private final List<NewSample> sampleRegistrations;

    private final Map<String /* material type */, List<NewMaterial>> materialRegistrations;

    private final List<? extends NewExternalData> dataSetRegistrations;

    private final List<DataSetUpdatesDTO> dataSetUpdates;

    public AtomicEntityOperationDetails(TechId registrationId, String userIdOrNull,
            List<NewSpace> spaceRegistrations, List<NewProject> projectRegistrations,
            List<NewExperiment> experimentRegistrations, List<SampleUpdatesDTO> sampleUpdates,
            List<NewSample> sampleRegistrations,
            Map<String, List<NewMaterial>> materialRegistrations,
            List<? extends NewExternalData> dataSetRegistrations,
            List<DataSetUpdatesDTO> dataSetUpdates)
    {
        this.registrationIdOrNull = registrationId;
        this.userIdOrNull = userIdOrNull;
        this.spaceRegistrations = new ArrayList<NewSpace>(spaceRegistrations);
        this.projectRegistrations = new ArrayList<NewProject>(projectRegistrations);
        this.experimentUpdates = new ArrayList<ExperimentUpdatesDTO>();
        this.experimentRegistrations = new ArrayList<NewExperiment>(experimentRegistrations);
        this.sampleUpdates = new ArrayList<SampleUpdatesDTO>(sampleUpdates);
        this.sampleRegistrations = new ArrayList<NewSample>(sampleRegistrations);
        this.materialRegistrations = new TreeMap<String, List<NewMaterial>>(materialRegistrations);
        this.dataSetRegistrations = new ArrayList<NewExternalData>(dataSetRegistrations);
        this.dataSetUpdates = new ArrayList<DataSetUpdatesDTO>(dataSetUpdates);
    }

    public TechId getRegistrationIdOrNull()
    {
        return registrationIdOrNull;
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

    public List<? extends NewExternalData> getDataSetRegistrations()
    {
        return dataSetRegistrations;
    }

    public List<DataSetUpdatesDTO> getDataSetUpdates()
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

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append("registrationIdOrNull", registrationIdOrNull);
        sb.append("userIdOrNull", userIdOrNull);
        sb.append("spaceRegistrations", spaceRegistrations);
        sb.append("projectRegistrations", projectRegistrations);
        sb.append("experimentUpdates", experimentUpdates);
        sb.append("experimentRegistrations", experimentRegistrations);
        sb.append("sampleUpdates", sampleUpdates);
        sb.append("sampleRegistrations", sampleRegistrations);
        sb.append("materialRegistrations", materialRegistrations);
        sb.append("dataSetRegistrations", dataSetRegistrations);
        sb.append("dataSetUpdates", dataSetUpdates);
        return sb.toString();
    }

}
