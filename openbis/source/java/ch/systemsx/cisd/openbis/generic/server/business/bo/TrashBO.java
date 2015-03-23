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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Piotr Buczek
 */
public class TrashBO extends AbstractBusinessObject implements ITrashBO
{
    private enum CascadeSampleDependentComponents
    {
        TRUE, FALSE
    }

    private final ICommonBusinessObjectFactory boFactory;

    private DeletionPE deletion;

    public TrashBO(IDAOFactory daoFactory, ICommonBusinessObjectFactory boFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, 
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker, 
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.boFactory = boFactory;
    }

    @Override
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    @Override
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

    @Override
    public void trashDataSets(List<TechId> dataSetIds)
    {
        trashDataSets(dataSetIds, true, null);
    }

    @Override
    public void trashExperiments(List<TechId> experimentIds)
    {
        assert deletion != null;
        
        TrashBatchOperation batchOperation =
                new TrashBatchOperation(EntityKind.EXPERIMENT, experimentIds, deletion,
                        getDeletionDAO(), true);
        BatchOperationExecutor.executeInBatches(batchOperation);
        
        if (batchOperation.counter > 0)
        {
            trashExperimentDependentDataSets(experimentIds);
            trashExperimentDependentSamples(experimentIds);
        }
    }

    @Override
    public void trashSamples(final List<TechId> sampleIds)
    {
        assert deletion != null;

        trashSamples(sampleIds, CascadeSampleDependentComponents.TRUE, true);
    }

    private void trashSamples(final List<TechId> sampleIds,
            final CascadeSampleDependentComponents cascadeType, boolean isOriginalDeletion)
    {
        assert deletion != null;

        TrashBatchOperation batchOperation =
                new TrashBatchOperation(EntityKind.SAMPLE, sampleIds, deletion, getDeletionDAO(),
                        isOriginalDeletion);
        BatchOperationExecutor.executeInBatches(batchOperation);

        if (batchOperation.counter > 0)
        {
            if (cascadeType == CascadeSampleDependentComponents.TRUE)
            {
                trashSampleDependentComponents(sampleIds);
            }
            trashSampleDependentDataSets(sampleIds);
        }
    }

    private static interface IDataSetFilter
    {
        public List<TechId> filter(List<DataPE> dataSets);
    }
    
    private static interface IIdHolderProvider
    {
        public IIdHolder getIdHolder(DataPE dataSet);
    }
    
    private static final class DataSetFilter implements IDataSetFilter
    {
        private Set<Long> ids;
        private IIdHolderProvider idHolderProvider;
        DataSetFilter(Collection<TechId> ids, IIdHolderProvider idHolderProvider)
        {
            this.ids = new HashSet<Long>(TechId.asLongs(ids));
            this.idHolderProvider = idHolderProvider;
            
        }
        @Override
        public List<TechId> filter(List<DataPE> dataSets)
        {
            List<TechId> deletableDataSets = new ArrayList<TechId>();
            for (DataPE dataSet : dataSets)
            {
                IIdHolder entity = idHolderProvider.getIdHolder(dataSet);
                if (entity != null && ids.contains(entity.getId()))
                {
                    deletableDataSets.add(new TechId(dataSet.getId()));
                }
            }
            return deletableDataSets;
        }
    }

    private void trashDataSets(final List<TechId> dataSetIds, boolean isOriginalDeletion, IDataSetFilter filterOrNull)
    {
        assert deletion != null;

        if (dataSetIds.isEmpty())
        {
            return;
        }
        IDatasetLister datasetLister = boFactory.createDatasetLister(session);
        List<TechId> allDeletables = DataSetUtils.getAllDeletableComponentsRecursively(dataSetIds,
                isOriginalDeletion, datasetLister, this);
        IDataSetTable dataSetTable = boFactory.createDataSetTable(session);
        dataSetTable.loadByIds(allDeletables);
        checkForNonDeletableDataSets(dataSetTable.getNonDeletableExternalDataSets());
        if (filterOrNull != null)
        {
            allDeletables = filterOrNull.filter(dataSetTable.getDataSets());
        }
        List<TechId> deletableOriginals = new ArrayList<TechId>(dataSetIds);
        deletableOriginals.retainAll(allDeletables);
        if (isOriginalDeletion)
        {
            allDeletables.removeAll(deletableOriginals);

            TrashBatchOperation batchOperation =
                    new TrashBatchOperation(EntityKind.DATA_SET, deletableOriginals, deletion,
                            getDeletionDAO(), true);
            BatchOperationExecutor.executeInBatches(batchOperation);

            batchOperation =
                    new TrashBatchOperation(EntityKind.DATA_SET, allDeletables, deletion,
                            getDeletionDAO(), false);
            BatchOperationExecutor.executeInBatches(batchOperation);
        } else
        {
            int nonDeletable = dataSetIds.size() - deletableOriginals.size();
            if (nonDeletable > 0)
            {
                dataSetIds.removeAll(deletableOriginals);
                throw new UserFailureException("The following related data sets couldn't be deleted "
                        + "because they are contained in data sets outside the deletion set: " + dataSetIds);
            }
            TrashBatchOperation batchOperation =
                    new TrashBatchOperation(EntityKind.DATA_SET, allDeletables, deletion,
                            getDeletionDAO(), false);
            BatchOperationExecutor.executeInBatches(batchOperation);
        }
    }

    private void checkForNonDeletableDataSets(List<ExternalDataPE> unavailableDataSets)
    {
        if (unavailableDataSets.isEmpty())
        {
            return;
        }
        Map<DataSetArchivingStatus, List<String>> statusToCodesMap =
                new TreeMap<DataSetArchivingStatus, List<String>>();
        for (ExternalDataPE dataSet : unavailableDataSets)
        {
            final DataSetArchivingStatus status = dataSet.getStatus();
            List<String> codes = statusToCodesMap.get(status);
            if (codes == null)
            {
                codes = new ArrayList<String>();
                statusToCodesMap.put(status, codes);
            }
            codes.add(dataSet.getCode());
        }

        StringBuilder builder = new StringBuilder();
        Set<Entry<DataSetArchivingStatus, List<String>>> entrySet = statusToCodesMap.entrySet();
        for (Entry<DataSetArchivingStatus, List<String>> entry : entrySet)
        {
            builder.append("\n Status: ").append(entry.getKey()).append(", data sets: ");
            builder.append(entry.getValue());
        }
        throw new UserFailureException(
                "Deletion not possible because the following data sets are not deletable:"
                        + builder);
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
        trashSamples(batchOperation.getResults(), CascadeSampleDependentComponents.FALSE, false);
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
                            List<TechId> ids = getDataDAO().listDataSetIdsBySampleIds(entities);
                            return ids;
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        trashDataSets(batchOperation.getResults(), false, new DataSetFilter(sampleIds,
                new IIdHolderProvider()
                    {
                        @Override
                        public IIdHolder getIdHolder(DataPE dataSet)
                        {
                            return dataSet.tryGetSample();
                        }
                    }));
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
        trashSamples(batchOperation.getResults(), CascadeSampleDependentComponents.TRUE, false);
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
        trashDataSets(batchOperation.getResults(), false, new DataSetFilter(experimentIds,
                new IIdHolderProvider()
                    {
                        @Override
                        public IIdHolder getIdHolder(DataPE dataSet)
                        {
                            return dataSet.getExperiment();
                        }
                    }));
    }

    @Override
    public void revertDeletion(TechId deletionId)
    {
        try
        {
            deletion = getDeletionDAO().getByTechId(deletionId);
            getDeletionDAO().revert(deletion, session.tryGetPerson());
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

        private final boolean isOriginalDeletion;

        private int counter = 0;

        public TrashBatchOperation(EntityKind entityKind, List<TechId> entityIds,
                DeletionPE deletion, IDeletionDAO deletionDAO, boolean isOriginalDeletion)
        {
            this.entityKind = entityKind;
            this.entityIds = entityIds;
            this.deletion = deletion;
            this.deletionDAO = deletionDAO;
            this.isOriginalDeletion = isOriginalDeletion;
        }

        @Override
        public void execute(List<TechId> entities)
        {
            counter += deletionDAO.trash(entityKind, entities, deletion, isOriginalDeletion);
        }

        @Override
        public List<TechId> getAllEntities()
        {
            return entityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityKind.getLabel();
        }

        @Override
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

        private final Set<TechId> results = new LinkedHashSet<TechId>();

        public AbstractQueryBatchOperation(EntityKind entityKind, List<TechId> entityIds,
                String operationName)
        {
            this.entityKind = entityKind;
            this.entityIds = entityIds;
            this.operationName = operationName;
        }

        public abstract Collection<TechId> listAction(List<TechId> entities);

        @Override
        public void execute(List<TechId> entities)
        {
            results.addAll(listAction(entities));
        }

        @Override
        public List<TechId> getAllEntities()
        {
            return entityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityKind.getLabel();
        }

        @Override
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
