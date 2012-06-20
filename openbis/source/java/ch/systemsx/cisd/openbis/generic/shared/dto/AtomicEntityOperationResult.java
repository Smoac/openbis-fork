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
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final long spacesCreatedCount;

    private final long projectsCreatedCount;

    private final long materialsCreatedCount;

    private final long experimentsCreatedCount;

    private final long samplesCreatedCount;

    private final long samplesUpdatedCount;

    private final long dataSetsCreatedCount;

    private final long dataSetsUpdatedCount;

    public AtomicEntityOperationResult()
    {
        this(Collections.<Space> emptyList(), Collections.<Project> emptyList(), Collections
                .<Experiment> emptyList(), Collections.<Sample> emptyList(), Collections
                .<Sample> emptyList(), Collections.<Material> emptyList(), Collections
                .<ExternalData> emptyList(), Collections.<ExternalData> emptyList());
    }

    public AtomicEntityOperationResult(List<Space> spacesCreated, List<Project> projectsCreated,
            List<Experiment> experimentsCreated, List<Sample> samplesUpdated,
            List<Sample> samplesCreated, List<Material> materialsCreated,
            List<ExternalData> dataSetsCreated, List<ExternalData> dataSetsUpdated)
    {
        this(spacesCreated.size(), projectsCreated.size(), materialsCreated.size(),
                experimentsCreated.size(), samplesCreated.size(), samplesUpdated.size(),
                dataSetsCreated.size(), dataSetsUpdated.size());
    }

    public AtomicEntityOperationResult(long spacesCreated, long projectsCreated,
            long materialsCreated, long experimentsCreated, long samplesCreated,
            long samplesUpdated, long dataSetsCreated, long dataSetsUpdated)
    {
        this.spacesCreatedCount = spacesCreated;
        this.projectsCreatedCount = projectsCreated;
        this.materialsCreatedCount = materialsCreated;
        this.experimentsCreatedCount = experimentsCreated;
        this.samplesCreatedCount = samplesCreated;
        this.samplesUpdatedCount = samplesUpdated;
        this.dataSetsCreatedCount = dataSetsCreated;
        this.dataSetsUpdatedCount = dataSetsUpdated;
    }

    public long getExperimentsUpdatedCount()
    {
        // There is no way to update experiments from performEntityOperations at the moment, so this
        // is always 0
        return 0;
    }

    public long getExperimentsCreatedCount()
    {
        return experimentsCreatedCount;
    }

    public long getSamplesUpdatedCount()
    {
        return samplesUpdatedCount;
    }

    public long getSamplesCreatedCount()
    {
        return samplesCreatedCount;
    }

    public long getDataSetsCreatedCount()
    {
        return dataSetsCreatedCount;
    }

    public long getDataSetsUpdatedCount()
    {
        return dataSetsUpdatedCount;
    }

    public long getSpacesCreatedCount()
    {
        return spacesCreatedCount;
    }

    public long getProjectsCreatedCount()
    {
        return projectsCreatedCount;
    }

    public long getMaterialsCreatedCount()
    {
        return materialsCreatedCount;
    }
}
