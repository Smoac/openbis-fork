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

package ch.systemsx.cisd.openbis.generic.server.business;

import javax.annotation.Resource;

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * The unique {@link IRelationshipService} implementation.
 * 
 * @author anttil
 */
public class RelationshipService implements IRelationshipService
{
    private static final String ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND =
            "Sample '%s' did not have parent '%s'";

    private static final String ERR_DATASET_PARENT_RELATIONSHIP_NOT_FOUND =
            "DataSet '%s' did not have parent '%s'";

    private DAOFactory daoFactory;

    /**
     * Reference to this instance of service, but as a spring bean, so that we can call methods of
     * this service and run the additional authorization.
     */
    @Resource(name = "relationship-service")
    private IRelationshipService service;

    @Override
    public void assignExperimentToProject(IAuthSession session, ExperimentPE experiment,
            ProjectPE project)
    {
        SampleUtils.setSamplesSpace(experiment, project.getSpace());
        experiment.setProject(project);
    }

    @Override
    public void assignProjectToSpace(IAuthSession session, ProjectPE project, SpacePE space)
    {
        project.setSpace(space);
        for (ExperimentPE experiment : project.getExperiments())
        {
            SampleUtils.setSamplesSpace(experiment, space);
        }
    }

    @Override
    public void assignSampleToExperiment(IAuthSession session, SamplePE sample,
            ExperimentPE experiment)
    {
        if (sample.getExperiment() != null)
        {
            service.checkCanUnassignSampleFromExperiment(session, sample);
        }

        sample.setExperiment(experiment);

        for (DataPE dataset : sample.getDatasets())
        {
            dataset.setExperiment(experiment);
        }
    }

    @Override
    public void checkCanUnassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
        // all the logic is done by the authorization mechanism
    }

    @Override
    public void unassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
        if (sample.getExperiment() != null)
        {
            sample.getExperiment().removeSample(sample);
        }
    }

    @Override
    public void assignSampleToSpace(IAuthSession session, SamplePE sample, SpacePE space)
    {
        sample.setDatabaseInstance(null);
        sample.setSpace(space);
    }

    @Override
    public void unshareSample(IAuthSession session, SamplePE sample, SpacePE space)
    {
        assignSampleToSpace(session, sample, space);
    }

    @Override
    public void shareSample(IAuthSession session, SamplePE sample)
    {
        SpacePE space = sample.getSpace();
        sample.setSpace(null);
        sample.setDatabaseInstance(space.getDatabaseInstance());
    }

    @Override
    public void assignDataSetToExperiment(IAuthSession session, DataPE data, ExperimentPE experiment)
    {
        data.setExperiment(experiment);
        data.setSample(null);
    }

    @Override
    public void assignDataSetToSample(IAuthSession session, DataPE data, SamplePE sample)
    {
        data.setExperiment(sample.getExperiment());
        data.setSample(sample);
    }

    @Override
    public void addParentToSample(IAuthSession session, SamplePE sample, SamplePE parent)
    {
        PersonPE actor = session.tryGetPerson();
        RelationshipTypePE relationshipType =
                daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(
                        BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);

        sample.addParentRelationship(new SampleRelationshipPE(parent, sample, relationshipType,
                actor));
    }

    @Override
    public void removeParentFromSample(IAuthSession session, SamplePE sample, SamplePE parent)
    {
        for (SampleRelationshipPE relationship : sample.getParentRelationships())
        {
            if (relationship.getParentSample().equals(parent))
            {
                sample.removeParentRelationship(relationship);
                return;
            }
        }
        throw UserFailureException.fromTemplate(ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND,
                sample.getCode(), parent.getCode());
    }

    @Override
    public void assignSampleToContainer(IAuthSession session, SamplePE sample, SamplePE container)
    {
        sample.setContainer(container);
    }

    @Override
    public void removeSampleFromContainer(IAuthSession session, SamplePE sample)
    {
        sample.setContainer(null);
    }

    @Override
    public void addParentToDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
        PersonPE actor = session.tryGetPerson();
        DataSetRelationshipPE relationship = new DataSetRelationshipPE(parent, data, actor);
        data.addParentRelationship(relationship);
        parent.addChildRelationship(relationship);
    }

    @Override
    public void removeParentFromDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
        DataSetRelationshipPE remove = null;
        for (DataSetRelationshipPE r : data.getParentRelationships())
        {
            if (r.getParentDataSet().equals(parent))
            {
                remove = r;
                break;
            }
        }

        if (remove != null)
        {
            data.removeParentRelationship(remove);
        } else
        {
            throw UserFailureException.fromTemplate(ERR_DATASET_PARENT_RELATIONSHIP_NOT_FOUND,
                    data.getCode(), parent.getCode());
        }
    }

    @Override
    public void assignDataSetToContainer(IAuthSession session, DataPE data, DataPE container)
    {
        PersonPE modifier = session.tryGetPerson();
        container.addComponent(data, modifier);
    }

    @Override
    public void removeDataSetFromContainer(IAuthSession session, DataPE data)
    {
        data.getContainer().removeComponent(data);
    }

    public void setDaoFactory(DAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }
}
