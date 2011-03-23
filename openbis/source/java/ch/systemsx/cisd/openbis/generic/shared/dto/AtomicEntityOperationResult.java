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
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    // This is currently always an empty list since there is no way to update experiments from the
    // DSS.
    private final ArrayList<Experiment> experimentsUpdated;

    private final ArrayList<Space> spacesCreated;

    private final ArrayList<Project> projectsCreated;

    private final ArrayList<Experiment> experimentsCreated;

    private final ArrayList<Sample> samplesUpdated;

    private final ArrayList<Sample> samplesCreated;

    private final ArrayList<ExternalData> dataSetsCreated;

    public AtomicEntityOperationResult()
    {
        this(Collections.<Space> emptyList(), Collections.<Project> emptyList(),
                Collections.<Experiment> emptyList(), Collections.<Sample> emptyList(), Collections
                .<Sample> emptyList(), Collections.<ExternalData> emptyList());
    }

    public AtomicEntityOperationResult(List<Space> spacesCreated, List<Project> projectsCreated,
            List<Experiment> experimentsCreated,
            List<Sample> samplesUpdated, List<Sample> samplesCreated,
            List<ExternalData> dataSetsCreated)
    {
        this.spacesCreated = new ArrayList<Space>(spacesCreated);
        this.projectsCreated = new ArrayList<Project>(projectsCreated);
        this.experimentsUpdated = new ArrayList<Experiment>();
        this.experimentsCreated = new ArrayList<Experiment>(experimentsCreated);
        this.samplesUpdated = new ArrayList<Sample>(samplesUpdated);
        this.samplesCreated = new ArrayList<Sample>(samplesCreated);
        this.dataSetsCreated = new ArrayList<ExternalData>(dataSetsCreated);
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

    public ArrayList<ExternalData> getDataSetsCreated()
    {
        return dataSetsCreated;
    }

    public ArrayList<Space> getSpacesCreated()
    {
        return spacesCreated;
    }

    public ArrayList<Project> getProjectsCreated()
    {
        return projectsCreated;
    }

}
