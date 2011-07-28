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

import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Piotr Buczek
 */
public class TrashBO extends AbstractBusinessObject implements ITrashBO
{
    private static class TrashBatchOperation implements IBatchOperation<TechId>
    {
        private final EntityKind entityKind;

        private final List<TechId> entityIds;

        private final DeletionPE deletion;

        private final IDeletionDAO deletionDAO;

        private int counter = 0;

        public TrashBatchOperation(EntityKind entityKind, List<TechId> entityIds,
                DeletionPE deletion, IDeletionDAO deletionDAO)
        {
            this.entityKind = entityKind;
            this.entityIds = entityIds;
            this.deletion = deletion;
            this.deletionDAO = deletionDAO;
        }

        public void execute(List<TechId> entities)
        {
            counter += deletionDAO.trash(entityKind, entities, deletion);
        }

        public List<TechId> getAllEntities()
        {
            return entityIds;
        }

        public String getEntityName()
        {
            return entityKind.getLabel();
        }

        public String getOperationName()
        {
            return "trash";
        }
    }

    private DeletionPE deletion;

    public TrashBO(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
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

    public void trashSamples(final List<TechId> sampleIds)
    {
        assert deletion != null;

        TrashBatchOperation batchOperation =
                new TrashBatchOperation(EntityKind.SAMPLE, sampleIds, deletion, getDeletionDAO());
        BatchOperationExecutor.executeInBatches(batchOperation);

        if (batchOperation.counter > 0)
        {
            trashSampleDependentChildrenAndComponents(sampleIds);
            trashSampleDependentDataSets(sampleIds);
        }
    }

    public void trashExperiments(final List<TechId> experimentIds)
    {
        assert deletion != null;

        TrashBatchOperation batchOperation =
                new TrashBatchOperation(EntityKind.EXPERIMENT, experimentIds, deletion,
                        getDeletionDAO());
        BatchOperationExecutor.executeInBatches(batchOperation);

        if (batchOperation.counter > 0)
        {
            trashExperimentDependentDataSets(experimentIds);
            trashExperimentDependentSamples(experimentIds);
        }
    }

    public void trashDataSets(final List<TechId> dataSetIds)
    {
        assert deletion != null;

        TrashBatchOperation batchOperation =
                new TrashBatchOperation(EntityKind.DATA_SET, dataSetIds, deletion, getDeletionDAO());
        BatchOperationExecutor.executeInBatches(batchOperation);
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
