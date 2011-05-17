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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
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

    // The userid on whose behalf the operations are done.
    private final String userIdOrNull;

    // This is always an empty list, since it is not currently possible to update experiments from
    // the DSS
    private final ArrayList<ExperimentUpdatesDTO> experimentUpdates;

    private final ArrayList<NewSpace> spaceRegistrations;

    private final ArrayList<NewProject> projectRegistrations;

    private final ArrayList<NewExperiment> experimentRegistrations;

    private final ArrayList<SampleUpdatesDTO> sampleUpdates;

    private final ArrayList<NewSample> sampleRegistrations;

    private final ArrayList<? extends NewExternalData> dataSetRegistrations;

    public AtomicEntityOperationDetails(String userIdOrNull, List<NewSpace> spaceRegistrations,
            List<NewProject> projectRegistrations, List<NewExperiment> experimentRegistrations,
            List<SampleUpdatesDTO> sampleUpdates, List<NewSample> sampleRegistrations,
            List<? extends NewExternalData> dataSetRegistrations)
    {
        this.userIdOrNull = userIdOrNull;
        this.spaceRegistrations = new ArrayList<NewSpace>(spaceRegistrations);
        this.projectRegistrations = new ArrayList<NewProject>(projectRegistrations);
        this.experimentUpdates = new ArrayList<ExperimentUpdatesDTO>();
        this.experimentRegistrations = new ArrayList<NewExperiment>(experimentRegistrations);
        this.sampleUpdates = new ArrayList<SampleUpdatesDTO>(sampleUpdates);
        this.sampleRegistrations = new ArrayList<NewSample>(sampleRegistrations);
        this.dataSetRegistrations = new ArrayList<NewExternalData>(dataSetRegistrations);
    }

    public String tryUserIdOrNull()
    {
        return userIdOrNull;
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

    public ArrayList<? extends NewExternalData> getDataSetRegistrations()
    {
        return dataSetRegistrations;
    }

    public ArrayList<NewSpace> getSpaceRegistrations()
    {
        return spaceRegistrations;
    }

    public ArrayList<NewProject> getProjectRegistrations()
    {
        return projectRegistrations;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append("userIdOrNull", userIdOrNull);
        sb.append("spaceRegistrations", spaceRegistrations);
        sb.append("projectRegistrations", projectRegistrations);
        sb.append("experimentUpdates", experimentUpdates);
        sb.append("experimentRegistrations", experimentRegistrations);
        sb.append("sampleUpdates", sampleUpdates);
        sb.append("sampleRegistrations", sampleRegistrations);
        sb.append("dataSetRegistrations", dataSetRegistrations);
        return sb.toString();
    }

}
