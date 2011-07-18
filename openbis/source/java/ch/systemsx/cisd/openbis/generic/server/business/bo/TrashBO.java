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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Piotr Buczek
 */
public class TrashBO extends AbstractBusinessObject implements ITrashBO
{

    private final ICommonBusinessObjectFactory boFactory;

    private DeletionPE deletion;

    public TrashBO(IDAOFactory daoFactory, Session session, ICommonBusinessObjectFactory boFactory)
    {
        super(daoFactory, session);
        this.boFactory = boFactory;
    }

    public void createDeletion(String reason)
    {
        try
        {
            deletion = new DeletionPE();
            deletion.setReason(reason);
            deletion.setRegistrator(session.tryGetPerson());
            getDeletionDAO().create(deletion);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Deletion");
        }
    }

    public void trashSamples(List<TechId> sampleIds)
    {
        assert deletion != null;
        ISampleTable sampleTableBO = boFactory.createSampleTable(session);
        int trashedCount = sampleTableBO.trashByTechIds(sampleIds, deletion);
        if (trashedCount > 0)
        {
            trashSampleDependentChildrenAndComponents(sampleIds);
            trashSampleDependentDataSets(sampleIds);
        }
    }

    public void trashExperiments(List<TechId> experimentIds)
    {
        assert deletion != null;
        IExperimentBO experimentBO = boFactory.createExperimentBO(session);
        int trashedCount = experimentBO.trashByTechIds(experimentIds, deletion);
        if (trashedCount > 0)
        {
            trashExperimentDependentDataSets(experimentIds);
            trashExperimentDependentSamples(experimentIds);
        }
    }

    public void trashDataSets(List<TechId> dataSetIds)
    {
        assert deletion != null;
        IDataSetTable dataSetTable = boFactory.createDataSetTable(session);
        dataSetTable.trashByTechIds(dataSetIds, deletion);
        // NOTE: data set children are not cascade trashed - a conscious decision made by Tomek
    }

    private void trashSampleDependentChildrenAndComponents(List<TechId> sampleIds)
    {
        ISampleDAO sampleDAO = getSampleDAO();
        trashSamples(new ArrayList<TechId>(sampleDAO.listSampleIdsByParentIds(sampleIds)));
        trashSamples(sampleDAO.listSampleIdsByContainerIds(sampleIds));
    }

    private void trashSampleDependentDataSets(List<TechId> sampleIds)
    {
        IDataDAO dataDAO = getDataDAO();
        trashDataSets(dataDAO.listDataSetIdsBySampleIds(sampleIds));
    }

    private void trashExperimentDependentSamples(List<TechId> experimentIds)
    {
        ISampleDAO sampleDAO = getSampleDAO();
        trashSamples(sampleDAO.listSampleIdsByExperimentIds(experimentIds));
    }

    private void trashExperimentDependentDataSets(List<TechId> experimentIds)
    {
        IDataDAO dataDAO = getDataDAO();
        trashDataSets(dataDAO.listDataSetIdsByExperimentIds(experimentIds));
    }

    public void revertDeletion(TechId deletionId)
    {
        try
        {
            deletion = getDeletionDAO().getByTechId(deletionId);
            getDeletionDAO().revert(deletion);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Deletion");
        }
    }

}
