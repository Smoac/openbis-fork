/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base;

import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author anttil
 */
public class RelationshipServiceStub implements IRelationshipService
{

    @Override
    public void assignExperimentToProject(IAuthSession session, ExperimentIdentifier experiment,
            ProjectIdentifier project)
    {
    }

    @Override
    public void assignProjectToSpace(IAuthSession session, ProjectIdentifier project,
            SpaceIdentifier space)
    {
    }

    @Override
    public void assignSampleToExperiment(IAuthSession session, SampleIdentifier sampleId,
            ExperimentIdentifier experimentId)
    {
    }

    @Override
    public void unassignSampleFromExperiment(IAuthSession session, SampleIdentifier sample)
    {
    }

    @Override
    public void unshareSample(IAuthSession session, SampleIdentifier sample, SpaceIdentifier space)
    {
    }

    @Override
    public void assignSampleToSpace(IAuthSession session, SampleIdentifier sample,
            SpaceIdentifier space)
    {
    }

    @Override
    public void shareSample(IAuthSession session, SampleIdentifier sample)
    {
    }

    @Override
    public void assignDataSetToExperiment(IAuthSession session, String dataSetCode,
            ExperimentIdentifier experiment)
    {
    }

    @Override
    public void assignDataSetToSample(IAuthSession session, String dataSetCode,
            SampleIdentifier sample)
    {
    }

    @Override
    public void addParentToSample(IAuthSession session, SampleIdentifier sample,
            SampleIdentifier parent)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeParentFromSample(IAuthSession session, SampleIdentifier sample,
            SampleIdentifier parent)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void assignSampleToContainer(IAuthSession session, SampleIdentifier sampleId,
            SamplePE sample,
            SampleIdentifier containerId, SamplePE container)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeSampleFromContainer(IAuthSession session, SampleIdentifier sampleId,
            SamplePE sample)
    {
        // TODO Auto-generated method stub

    }

}
