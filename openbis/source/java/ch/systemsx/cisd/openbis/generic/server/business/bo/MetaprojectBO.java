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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectBO extends AbstractBusinessObject implements IMetaprojectBO
{
    private MetaprojectPE metaproject;

    private boolean dataChanged;

    public MetaprojectBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    @Override
    public void loadDataByTechId(TechId metaprojectId)
    {
        try
        {
            metaproject = getMetaprojectDAO().getByTechId(metaprojectId);
        } catch (ObjectRetrievalFailureException exception)
        {
            throw new UserFailureException(String.format(
                    "Metaproject with ID '%s' does not exist.", metaprojectId));
        }
        dataChanged = false;
    }

    @Override
    public void save() throws UserFailureException
    {
        assert metaproject != null : "Can not save an undefined metaproject.";

        if (dataChanged)
        {
            try
            {
                getMetaprojectDAO().createOrUpdateMetaproject(metaproject, findPerson());
            } catch (final DataAccessException ex)
            {
                throwException(ex, "Metaproject '" + metaproject.getName() + "'");
            }

            dataChanged = false;
        }
    }

    public void define(final String metaprojectName, final String description, final String ownerId)
            throws UserFailureException
    {
        assert metaprojectName != null : "Unspecified metaproject name.";
        assert ownerId != null : "Unspecified metaproject owner";

        this.metaproject = createMetaproject(metaprojectName, description, ownerId);
        dataChanged = true;
    }

    private MetaprojectPE createMetaproject(final String metaprojectName, final String description,
            String ownerId)
    {
        final MetaprojectPE result = new MetaprojectPE();

        result.setName(metaprojectName);
        result.setDescription(description);
        PersonPE owner = getPersonDAO().tryFindPersonByUserId(ownerId);
        if (owner == null)
        {
            throw new UserFailureException("Person '%s' not found in the database.");
        }
        result.setOwner(owner);
        result.setPrivate(true);

        return result;
    }

    @Override
    public void deleteByTechId(TechId metaprojectId) throws UserFailureException
    {
        loadDataByTechId(metaprojectId);

        getMetaprojectDAO().delete(metaproject);
        getEventDAO().persist(createDeletionEvent(metaproject, session.tryGetPerson()));
    }

    private static EventPE createDeletionEvent(MetaprojectPE metaproject, PersonPE registrator)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.METAPROJECT);
        event.setIdentifiers(Collections.singletonList(metaproject.getName()));
        event.setDescription(metaproject.getName());
        event.setRegistrator(registrator);

        return event;
    }

    @Override
    public void addExperiments(List<TechId> experimentIds)
    {
        for (TechId experimentId : experimentIds)
        {
            ExperimentPE experimentPE = getExperimentDAO().tryGetByTechId(experimentId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setExperiment(experimentPE);
            metaproject.addAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void addSamples(List<TechId> sampleIds)
    {
        for (TechId sampleId : sampleIds)
        {
            SamplePE samplePE = getSampleDAO().tryGetByTechId(sampleId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setSample(samplePE);
            metaproject.addAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void addDataSets(List<TechId> dataSetIds)
    {
        for (TechId dataSetId : dataSetIds)
        {
            DataPE dataPE = getDataDAO().tryGetByTechId(dataSetId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setDataSet(dataPE);
            metaproject.addAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void addMaterials(List<TechId> materialIds)
    {
        for (TechId materialId : materialIds)
        {
            MaterialPE materialPE = getMaterialDAO().tryGetByTechId(materialId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setMaterial(materialPE);
            metaproject.addAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void removeExperiments(List<TechId> experimentIds)
    {
        for (TechId experimentId : experimentIds)
        {
            ExperimentPE experimentPE = getExperimentDAO().tryGetByTechId(experimentId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setExperiment(experimentPE);
            metaproject.removeAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void removeSamples(List<TechId> sampleIds)
    {
        for (TechId sampleId : sampleIds)
        {
            SamplePE samplePE = getSampleDAO().tryGetByTechId(sampleId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setSample(samplePE);
            metaproject.removeAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void removeDataSets(List<TechId> dataSetIds)
    {
        for (TechId dataSetId : dataSetIds)
        {
            DataPE dataPE = getDataDAO().tryGetByTechId(dataSetId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setDataSet(dataPE);
            metaproject.removeAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    @Override
    public void removeMaterials(List<TechId> materialIds)
    {
        for (TechId materialId : materialIds)
        {
            MaterialPE materialPE = getMaterialDAO().tryGetByTechId(materialId);
            MetaprojectAssignmentPE metaprojectAssignmentPE = createAssignement();
            metaprojectAssignmentPE.setMaterial(materialPE);
            metaproject.removeAssignment(metaprojectAssignmentPE);
        }

        dataChanged = true;
    }

    private MetaprojectAssignmentPE createAssignement()
    {
        MetaprojectAssignmentPE metaprojectAssignmentPE = new MetaprojectAssignmentPE();
        metaprojectAssignmentPE.setMetaproject(metaproject);
        return metaprojectAssignmentPE;
    }
}
