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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
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
    private enum CascadeSampleDependentComponents
    {
        TRUE, FALSE
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
        trashSamples(sampleIds, CascadeSampleDependentComponents.TRUE);
    }

    void trashSamples(final List<TechId> sampleIds,
            final CascadeSampleDependentComponents cascadeType)
    {
        assert deletion != null;

        TrashBatchOperation batchOperation =
                new TrashBatchOperation(EntityKind.SAMPLE, sampleIds, deletion, getDeletionDAO());
        BatchOperationExecutor.executeInBatches(batchOperation);

        if (batchOperation.counter > 0)
        {
            trashSampleDependentChildren(sampleIds);
            if (cascadeType == CascadeSampleDependentComponents.TRUE)
            {
                trashSampleDependentComponents(sampleIds);
            }
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

    private void trashSampleDependentChildren(List<TechId> sampleIds)
    {
        final ISampleDAO sampleDAO = getSampleDAO();

        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.SAMPLE, sampleIds,
                        "listSampleIdsByParentIds")
                    {
                        @Override
                        public Collection<TechId> listAction(List<TechId> entities)
                        {
                            return sampleDAO.listSampleIdsByParentIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        trashSamples(batchOperation.getResults());
    }
    
    private void trashSampleDependentComponents(List<TechId> sampleIds)
    {
        final ISampleDAO sampleDAO = getSampleDAO();

        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.SAMPLE, sampleIds,
                        "listSampleIdsByContainerIds")
                    {
                        @Override
                        public Collection<TechId> listAction(List<TechId> entities)
                        {
                            return sampleDAO.listSampleIdsByContainerIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        // We have a business rule that there is just 1 level of components and using this here
        // improves performance.
        trashSamples(batchOperation.getResults(), CascadeSampleDependentComponents.FALSE);
    }

    private void trashSampleDependentDataSets(List<TechId> sampleIds)
    {
        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.DATA_SET, sampleIds,
                        "listDataSetIdsBySampleIds")
                    {
                        @Override
                        public List<TechId> listAction(List<TechId> entities)
                        {
                            return getDataDAO().listDataSetIdsBySampleIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        trashDataSets(batchOperation.getResults());
    }

    private void trashExperimentDependentSamples(List<TechId> experimentIds)
    {
        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.SAMPLE, experimentIds,
                        "listSampleIdsByExperimentIds")
                    {
                        @Override
                        public List<TechId> listAction(List<TechId> entities)
                        {
                            return getSampleDAO().listSampleIdsByExperimentIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        trashSamples(batchOperation.getResults());
    }

    private void trashExperimentDependentDataSets(List<TechId> experimentIds)
    {
        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.DATA_SET, experimentIds,
                        "listDataSetIdsByExperimentIds")
                    {
                        @Override
                        public List<TechId> listAction(List<TechId> entities)
                        {
                            return getDataDAO().listDataSetIdsByExperimentIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        trashDataSets(batchOperation.getResults());
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

    private abstract static class AbstractQueryBatchOperation implements IBatchOperation<TechId>
    {
        private final EntityKind entityKind;

        private final List<TechId> entityIds;

        private final String operationName;

        private final Set<TechId> results = new HashSet<TechId>();

        public AbstractQueryBatchOperation(EntityKind entityKind, List<TechId> entityIds,
                String operationName)
        {
            this.entityKind = entityKind;
            this.entityIds = entityIds;
            this.operationName = operationName;
        }

        public abstract Collection<TechId> listAction(List<TechId> entities);

        public void execute(List<TechId> entities)
        {
            results.addAll(listAction(entities));
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
            return operationName;
        }

        public List<TechId> getResults()
        {
            return new ArrayList<TechId>(results);
        }
    }

}
