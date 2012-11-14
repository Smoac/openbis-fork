/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.lang.time.DateUtils;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, DatasetRelationRecord.class, DataStoreRecord.class,
            DatasetCodeWithShareIdRecord.class, IDatasetListingQuery.class })
public class DatasetLister extends AbstractLister implements IDatasetLister
{
    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    private final String baseIndexURL;

    private final Long userId;

    //
    // Working interfaces
    //

    private final IDatasetListingQuery query;

    private final IEntityPropertiesEnricher propertiesEnricher;

    private final SecondaryEntityDAO referencedEntityDAO;

    //
    // Working data structures
    //

    private final Long2ObjectMap<DataSetType> dataSetTypes =
            new Long2ObjectOpenHashMap<DataSetType>();

    private final Long2ObjectMap<DataStore> dataStores = new Long2ObjectOpenHashMap<DataStore>();

    private final Map<Long, FileFormatType> fileFormatTypes = new HashMap<Long, FileFormatType>();

    private final Map<Long, LocatorType> locatorTypes = new HashMap<Long, LocatorType>();

    private final Long2ObjectMap<ExternalDataManagementSystem> externalDataManagementSystems =
            new Long2ObjectOpenHashMap<ExternalDataManagementSystem>();

    public static IDatasetLister create(IDAOFactory daoFactory, String baseIndexURL, Long userId)
    {
        DatasetListerDAO dao = DatasetListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);

        return create(dao, referencedEntityDAO, baseIndexURL, userId);
    }

    static IDatasetLister create(DatasetListerDAO dao, SecondaryEntityDAO referencedEntityDAO,
            String baseIndexURL, Long userId)
    {
        IDatasetListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new DatasetLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                propertiesEnricher, referencedEntityDAO, baseIndexURL, userId);
    }

    // For unit tests
    DatasetLister(final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final IDatasetListingQuery query, IEntityPropertiesEnricher propertiesEnricher,
            SecondaryEntityDAO referencedEntityDAO, String baseIndexURL, Long userId)
    {
        super(referencedEntityDAO);
        assert databaseInstance != null;
        assert query != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.propertiesEnricher = propertiesEnricher;
        this.referencedEntityDAO = referencedEntityDAO;
        this.baseIndexURL = baseIndexURL;
        this.userId = userId;
    }

    @Override
    public List<ExternalData> listBySampleTechId(TechId sampleId, boolean showOnlyDirectlyConnected)
    {
        if (showOnlyDirectlyConnected)
        {
            return enrichDatasets(query.getDatasetsForSample(sampleId.getId()));
        } else
        {
            // get all descendands of the sample
            LongSet sampleIds = referencedEntityDAO.getSampleDescendantIdsAndSelf(sampleId.getId());
            // get directly connected datasets, then go layer by layer into children datasets
            LongSet results = new LongOpenHashSet();
            LongSet currentLayer = new LongOpenHashSet(query.getDatasetIdsForSamples(sampleIds));
            LongSet nextLayer;
            while (currentLayer.isEmpty() == false)
            {
                nextLayer = new LongOpenHashSet(query.getDatasetChildrenIds(currentLayer));
                results.addAll(currentLayer);
                nextLayer.removeAll(results); // don't go twice through the same dataset
                currentLayer = nextLayer;
            }
            return listByDatasetIds(results);
        }
    }

    @Override
    public List<ExternalData> listBySampleIds(Collection<Long> sampleIds)
    {
        LongSet ids = new LongOpenHashSet();
        for (Long id : sampleIds)
        {
            ids.add(id);
        }
        return enrichDatasets(query.getDatasetsForSamples(ids));
    }

    @Override
    public List<ExternalData> listByExperimentTechId(TechId experimentId,
            boolean showOnlyDirectlyConnected)
    {
        DataIterator<DatasetRecord> dataSets;
        if (showOnlyDirectlyConnected)
        {
            dataSets = query.getDatasetsForExperiment(experimentId.getId());
        } else
        {
            dataSets = query.getDataSetsForExperimentAndDescendents(experimentId.getId());
        }
        return enrichDatasets(dataSets);
    }

    @Override
    public Map<Long, Set<Long>> listParentIds(Collection<Long> dataSetIDs)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : dataSetIDs)
        {
            ids.add(id);
        }
        DataIterator<DatasetRelationRecord> relationships = query.listParentDataSetIds(ids);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (DatasetRelationRecord relationship : relationships)
        {
            Set<Long> parents = map.get(relationship.data_id_child);
            if (parents == null)
            {
                parents = new LinkedHashSet<Long>();
                map.put(relationship.data_id_child, parents);
            }
            parents.add(relationship.data_id_parent);
        }
        return map;
    }

    @Override
    public Map<Long, Set<Long>> listChildrenIds(Collection<Long> dataSetIDs)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : dataSetIDs)
        {
            ids.add(id);
        }
        DataIterator<DatasetRelationRecord> relationships = query.listChildrenDataSetIds(ids);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (DatasetRelationRecord relationship : relationships)
        {
            Set<Long> children = map.get(relationship.data_id_parent);
            if (children == null)
            {
                children = new LinkedHashSet<Long>();
                map.put(relationship.data_id_parent, children);
            }
            children.add(relationship.data_id_child);
        }
        return map;
    }

    @Override
    public Map<Sample, List<ExternalData>> listAllDataSetsFor(List<Sample> samples)
    {
        TableMap<Long, Sample> samplesByID =
                new TableMap<Long, Sample>(samples, new IKeyExtractor<Long, Sample>()
                    {
                        @Override
                        public Long getKey(Sample e)
                        {
                            return e.getId();
                        }
                    });
        Map<Sample, List<ExternalData>> result = new HashMap<Sample, List<ExternalData>>();
        Set<Long> sampleIDs = new HashSet<Long>();
        for (Sample sample : samples)
        {
            result.put(sample, new ArrayList<ExternalData>());
            sampleIDs.add(sample.getId());
        }
        List<ExternalData> rootDataSets = listBySampleIds(sampleIDs);
        addChildren(rootDataSets);
        for (ExternalData dataSet : rootDataSets)
        {
            Sample sample = samplesByID.tryGet(dataSet.getSample().getId());
            assert sample != null;
            result.get(sample).add(dataSet);
        }
        return result;
    }

    private void addChildren(List<ExternalData> dataSets)
    {
        Map<Long, ExternalData> dataSetsByID = new HashMap<Long, ExternalData>();
        for (ExternalData dataSet : dataSets)
        {
            dataSetsByID.put(dataSet.getId(), dataSet);
        }
        Map<Long, Set<Long>> childrenIDs = listChildrenIds(dataSetsByID.keySet());
        Set<Entry<Long, Set<Long>>> entrySet = childrenIDs.entrySet();
        Map<Long, Set<Long>> child2ParentsMap = new HashMap<Long, Set<Long>>();
        Set<Long> childIDs = new HashSet<Long>();
        for (Entry<Long, Set<Long>> entry : entrySet)
        {
            Set<Long> value = entry.getValue();
            Long parentID = entry.getKey();
            for (Long childId : value)
            {
                childIDs.add(childId);
                Set<Long> parents = child2ParentsMap.get(childId);
                if (parents == null)
                {
                    parents = new HashSet<Long>(1);
                    child2ParentsMap.put(childId, parents);
                }
                parents.add(parentID);
            }
        }
        if (childIDs.isEmpty() == false)
        {
            List<ExternalData> children = listByDatasetIds(childIDs);
            for (ExternalData child : children)
            {
                Set<Long> parentIDs = child2ParentsMap.get(child.getId());
                for (Long parentID : parentIDs)
                {
                    ExternalData dataSet = dataSetsByID.get(parentID);
                    List<ExternalData> childList = dataSet.getChildren();
                    if (childList == null)
                    {
                        childList = new ArrayList<ExternalData>(1);
                        dataSet.setChildren(childList);
                    }
                    childList.add(child);
                }
            }
            addChildren(children);
        }
    }

    @Override
    public List<ExternalData> listByChildTechId(TechId childDatasetId)
    {
        return enrichDatasets(query.getParentDatasetsForChild(childDatasetId.getId()));
    }

    @Override
    public List<ExternalData> listByContainerTechId(TechId containerDatasetId)
    {
        return enrichDatasets(query.getContainedDatasetsForContainer(containerDatasetId.getId()));
    }

    @Override
    public List<ExternalData> listByParentTechIds(Collection<Long> parentDatasetIds)
    {
        DataIterator<DatasetRecord> childrenDataSets =
                query.getChildDatasetsForParents(new LongOpenHashSet(parentDatasetIds));
        return enrichDatasets(childrenDataSets);
    }

    @Override
    public List<ExternalData> listByDatasetIds(Collection<Long> datasetIds)
    {
        return enrichDatasets(query.getDatasets(new LongOpenHashSet(datasetIds)));
    }

    @Override
    public List<ExternalData> listByDatasetCode(Collection<String> datasetCodes)
    {
        String[] codes = datasetCodes.toArray(new String[datasetCodes.size()]);
        DataIterator<DatasetRecord> datasets = query.getDatasets(codes);
        return enrichDatasets(datasets);
        // loadSmallConnectedTables();
        // return asList(createPrimaryDatasets(asList(datasets)));
    }

    @Override
    public List<ExternalData> listByDataStore(long dataStoreID)
    {
        return enrichDatasets(query.getDatasetsByDataStoreId(dataStoreID));
    }

    @Override
    public List<DataSetShareId> listAllDataSetShareIdsByDataStore(long dataStoreID)
    {
        List<DataSetShareId> results = new ArrayList<DataSetShareId>();

        DataIterator<DatasetCodeWithShareIdRecord> records =
                query.getAllDatasetsWithShareIdsByDataStoreId(dataStoreID);
        for (DatasetCodeWithShareIdRecord record : records)
        {
            DataSetShareId dataSetShareId = new DataSetShareId();
            dataSetShareId.setDataSetCode(record.code);
            dataSetShareId.setShareId(record.share_id);
            results.add(dataSetShareId);
        }
        return results;
    }

    @Override
    public List<ExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria)
    {
        DataIterator<DatasetRecord> dataSets;
        String sampleType = criteria.getConnectedSampleTypeCode();
        long lastSeenDataSetId = criteria.getLastSeenDataSetId();
        if (sampleType == null)
        {
            dataSets = query.getNewDataSets(lastSeenDataSetId);
        } else
        {
            Long sampleTypeId = referencedEntityDAO.getSampleTypeIdForSampleTypeCode(sampleType);
            dataSets = query.getNewDataSetsForSampleType(sampleTypeId, lastSeenDataSetId);
        }
        if (criteria.shouldResultBeEnriched())
        {
            return enrichDatasets(dataSets);
        }
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(dataSets);
        Long2ObjectMap<ExternalData> datasetMap = createPrimaryDatasets(datasetRecords);
        return asList(datasetMap);
    }

    @Override
    public List<ExternalData> listByArchiverCriteria(String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        loadSmallConnectedTables();
        final Long dataStoreId = extractDataStoreId(dataStoreCode);
        final Date lastRegistrationDate = extractLastRegistrationDate(criteria);
        final String dataSetTypeCodeOrNull = criteria.tryGetDataSetTypeCode();
        final boolean presentInArchive = criteria.isPresentInArchive();
        if (dataSetTypeCodeOrNull == null)
        {
            return enrichDatasets(query.getAvailableExtDatasRegisteredBefore(dataStoreId,
                    lastRegistrationDate, presentInArchive));
        } else
        {
            Long dataSetTypeId = extractDataSetTypeId(dataSetTypeCodeOrNull);
            return enrichDatasets(query.getAvailableExtDatasRegisteredBeforeWithDataSetType(
                    dataStoreId, lastRegistrationDate, presentInArchive, dataSetTypeId));
        }
    }

    private Date extractLastRegistrationDate(ArchiverDataSetCriteria criteria)
    {
        return DateUtils.addDays(new Date(), -criteria.getOlderThan());
    }

    private Long extractDataStoreId(String dataStoreCode)
    {
        for (Entry<Long, DataStore> entry : dataStores.entrySet())
        {
            if (dataStoreCode.equalsIgnoreCase(entry.getValue().getCode()))
            {
                return entry.getKey();
            }
        }
        throw new UserFailureException("Data store '" + dataStoreCode + "' unknown.");
    }

    private Long extractDataSetTypeId(String dataSetTypeCode)
    {
        for (Entry<Long, DataSetType> entry : dataSetTypes.entrySet())
        {
            if (dataSetTypeCode.equalsIgnoreCase(entry.getValue().getCode()))
            {
                return entry.getKey();
            }
        }
        throw new UserFailureException("Data Set type '" + dataSetTypeCode + "' unknown.");
    }

    private List<ExternalData> enrichDatasets(Iterable<DatasetRecord> datasets)
    {
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(datasets);
        final Long2ObjectMap<ExternalData> datasetMap = createPrimaryDatasets(datasetRecords);
        enrichWithExperiments(datasetMap);
        filterDatasetsWithNullExperiments(datasetMap);
        enrichWithProperties(datasetMap);
        enrichWithSamples(datasetMap);
        enrichWithContainers(datasetMap);
        enrichWithContainedDataSets(datasetMap);
        enrichWithParents(datasetMap);
        if (this.userId != null)
        {
            enrichWithMetaProjects(datasetMap);
        }
        return asList(datasetMap);
    }

    private void enrichWithMetaProjects(Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet set = new LongOpenHashSet();
        set.addAll(datasetMap.keySet());

        for (MetaProjectWithEntityId metaProject : query.getMetaprojects(
                set, userId))
        {
            Metaproject mp = new Metaproject();
            mp.setId(metaProject.id);
            mp.setCreationDate(metaProject.creation_date);
            mp.setDescription(metaProject.description);
            mp.setIdentifier("/" + metaProject.owner_name + "/" + metaProject.name);
            mp.setName(metaProject.name);
            mp.setOwnerId(metaProject.owner_name);
            mp.setPrivate(metaProject.is_private);

            ExternalData data = datasetMap.get(metaProject.entity_id);

            if (data != null)
            {
                Collection<Metaproject> mps = data.getMetaprojects();
                if (mps == null)
                {
                    mps = new HashSet<Metaproject>();
                    data.setMetaprojects(mps);
                }
                mps.add(mp);
            }
        }
    }

    // assumes that the connection to the sample has been already established and sample has the
    // id set.
    private void enrichWithSamples(Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet ids = extractSampleIds(datasetMap);
        Long2ObjectMap<Sample> samples = referencedEntityDAO.getSamples(ids);
        for (ExternalData dataset : datasetMap.values())
        {
            if (dataset.getSample() != null)
            {
                long sampleId = dataset.getSample().getId();
                Sample sample = samples.get(sampleId);
                dataset.setSample(sample);
            }
        }
    }

    private static LongSet extractSampleIds(Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet ids = new LongOpenHashSet();
        for (ExternalData dataset : datasetMap.values())
        {
            if (dataset.getSample() != null)
            {
                long sampleId = dataset.getSample().getId();
                ids.add(sampleId);
            }
        }
        return ids;
    }

    // assumes that the connection to experiment has been already established and experiment has the
    // id set.
    private void enrichWithExperiments(Long2ObjectMap<ExternalData> datasetMap)
    {
        Long2ObjectMap<Experiment> experimentMap = new Long2ObjectOpenHashMap<Experiment>();

        for (ExternalData dataset : datasetMap.values())
        {
            long experimentId = dataset.getExperiment().getId();
            Experiment experiment = experimentMap.get(experimentId);
            // null value is put if experiment is from different db instance
            if (experimentMap.containsKey(experimentId) == false)
            {
                experiment = referencedEntityDAO.tryGetExperiment(experimentId);
                experimentMap.put(experimentId, experiment);
            }
            dataset.setExperiment(experiment);
        }
    }

    private void filterDatasetsWithNullExperiments(Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet datasetsToRemove = new LongOpenHashSet();
        for (ExternalData dataset : datasetMap.values())
        {
            if (dataset.getExperiment() == null)
            {
                datasetsToRemove.add(dataset.getId());
            }
        }
        for (Long datasetId : datasetsToRemove)
        {
            datasetMap.remove(datasetId);
        }
    }

    private static <T> List<T> asList(Iterable<T> items)
    {
        List<T> result = new ArrayList<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
    }

    private void enrichWithProperties(final Long2ObjectMap<ExternalData> resultMap)
    {
        propertiesEnricher.enrich(resultMap.keySet(), new IEntityPropertiesHolderResolver()
            {
                @Override
                public ExternalData get(long id)
                {
                    return resultMap.get(id);
                }
            });
    }

    private void enrichWithParents(Long2ObjectMap<ExternalData> datasetMap)
    {
        Map<Long, Set<Long>> parentIdsMap = listParentIds(datasetMap.keySet());
        Set<Long> allParentIds = new HashSet<Long>();

        for (Set<Long> parentIds : parentIdsMap.values())
        {
            allParentIds.addAll(parentIds);
        }

        DataIterator<DatasetRecord> parentIterator =
                query.getDatasets(new LongOpenHashSet(allParentIds));

        if (parentIterator != null)
        {
            Long2ObjectMap<ExternalData> parentMap = createPrimaryDatasets(parentIterator);

            for (Entry<Long, Set<Long>> parentIdsEntry : parentIdsMap.entrySet())
            {
                Long datasetId = parentIdsEntry.getKey();
                Set<Long> parentIds = parentIdsEntry.getValue();

                ExternalData dataset = datasetMap.get(datasetId);
                Collection<ExternalData> parents = new ArrayList<ExternalData>();

                for (Long parentId : parentIds)
                {
                    ExternalData parent = parentMap.get(parentId);
                    if (parent != null)
                    {
                        parents.add(parent);
                    }
                }

                dataset.setParents(parents);
            }
        }
    }

    private void enrichWithContainers(Long2ObjectMap<ExternalData> datasetMap)
    {

        Set<Long> containersNotLoaded = new HashSet<Long>();
        for (ExternalData dataSet : datasetMap.values())
        {
            ContainerDataSet containerOrNull = dataSet.tryGetContainer();
            Long containerId = (containerOrNull != null) ? containerOrNull.getId() : null;
            if (containerId != null)
            {
                ContainerDataSet loadedContainer = (ContainerDataSet) datasetMap.get(containerId);
                if (loadedContainer != null)
                {
                    dataSet.setContainer(loadedContainer);
                } else
                {
                    containersNotLoaded.add(containerId);
                }
            }
        }

        if (false == containersNotLoaded.isEmpty())
        {
            // load the unavailable container data sets with an additional query
            List<ExternalData> containersSecondPass = listByDatasetIds(containersNotLoaded);
            TableMap<Long, ExternalData> secondPassMap =
                    new TableMap<Long, ExternalData>(containersSecondPass,
                            KeyExtractorFactory.<ExternalData> createIdKeyExtractor());
            for (ExternalData dataSet : datasetMap.values())
            {
                ContainerDataSet containerOrNull = dataSet.tryGetContainer();
                Long containerId = (containerOrNull != null) ? containerOrNull.getId() : null;
                ContainerDataSet newlyLoaded = (ContainerDataSet) secondPassMap.tryGet(containerId);
                if (newlyLoaded != null)
                {
                    dataSet.setContainer(newlyLoaded);
                }
            }
        }
    }

    private void enrichWithContainedDataSets(Long2ObjectMap<ExternalData> datasetMap)
    {
        Long2ObjectMap<ExternalData> fullContextMap =
                new Long2ObjectOpenHashMap<ExternalData>(datasetMap);
        LongSet containerIDs = new LongOpenHashSet();
        for (ExternalData dataSet : datasetMap.values())
        {
            if (dataSet.isContainer())
            {
                containerIDs.add(dataSet.getId());
            }
            ContainerDataSet containerOrNull = dataSet.tryGetContainer();
            Long containerId = (containerOrNull != null) ? containerOrNull.getId() : null;
            if (containerId != null && false == fullContextMap.containsKey(containerId))
            {
                containerIDs.add(containerId);
                fullContextMap.put(containerId, containerOrNull);
            }
        }

        if (containerIDs.isEmpty())
        {
            return;
        }

        List<Long> containedDataSetIDs = asList(query.getContainedDataSetIds(containerIDs));
        LongSet notYetLoadedChilren = new LongOpenHashSet();

        for (Long containedDataSetID : containedDataSetIDs)
        {
            if (false == fullContextMap.containsKey(containedDataSetID))
            {
                notYetLoadedChilren.add(containedDataSetID);
            }
        }

        if (false == notYetLoadedChilren.isEmpty())
        {
            Long2ObjectMap<ExternalData> childrenSecondPass =
                    createPrimaryDatasets(asList(query.getDatasets(notYetLoadedChilren)));
            fullContextMap.putAll(childrenSecondPass);
        }

        for (Long id : containedDataSetIDs)
        {
            ExternalData contained = fullContextMap.get(id);
            Long containerId = contained.tryGetContainer().getId();
            ContainerDataSet container = fullContextMap.get(containerId).tryGetAsContainerDataSet();
            // set container to the child
            contained.setContainer(container);
            // add the child to the container
            List<ExternalData> containedDataSets = container.getContainedDataSets();
            containedDataSets.add(contained);
            container.setContainedDataSets(containedDataSets);
        }
    }

    private static <T> List<T> asList(Long2ObjectMap<T> items)
    {
        List<T> result = new ArrayList<T>();
        result.addAll(items.values());
        return result;
    }

    private Long2ObjectMap<ExternalData> createPrimaryDatasets(Iterable<DatasetRecord> records)
    {
        Long2ObjectMap<ExternalData> datasets = new Long2ObjectOpenHashMap<ExternalData>();
        for (DatasetRecord record : records)
        {
            DataSetType dsType = dataSetTypes.get(record.dsty_id);
            ExternalData dataSetOrNull = null;
            if (record.is_placeholder)
            {
                // placeholder data sets are filtered out
            } else if (dsType.getDataSetKind() == DataSetKind.CONTAINER)
            {
                dataSetOrNull = convertToContainerDataSet(record);
            } else if (dsType.getDataSetKind() == DataSetKind.LINK)
            {
                dataSetOrNull = convertToLinkDataSet(record);
            } else if (record.location != null)
            {
                dataSetOrNull = convertToDataSet(record);
            }

            if (dataSetOrNull != null)
            {
                enrichWithDeletion(dataSetOrNull, record);
                datasets.put(record.id, dataSetOrNull);
            }
        }
        return datasets;
    }

    // NOTE: this just marks the data set as invalid without loading any details
    private void enrichWithDeletion(final ExternalData dataSet, DatasetRecord row)
    {
        if (row.del_id != null)
        {
            final Deletion deletion = new Deletion();
            dataSet.setDeletion(deletion);
        }
    }

    private DataSet convertToDataSet(DatasetRecord record)
    {
        DataSet dataSet = new DataSet();
        convertStandardProperties(dataSet, record);

        dataSet.setComplete(resolve(record.is_complete));
        dataSet.setStatus(record.status == null ? null : DataSetArchivingStatus
                .valueOf(record.status));
        dataSet.setPresentInArchive(record.present_in_archive == null ? false
                : record.present_in_archive);
        dataSet.setSpeedHint(record.speed_hint == null ? Constants.DEFAULT_SPEED_HINT
                : record.speed_hint);
        dataSet.setShareId(record.share_id);
        dataSet.setFileFormatType(record.ffty_id == null ? null : fileFormatTypes
                .get(record.ffty_id));
        dataSet.setStorageConfirmation(record.storage_confirmation == null ? false
                : record.storage_confirmation);
        dataSet.setLocation(record.location);
        dataSet.setSize(record.size);
        dataSet.setLocatorType(record.loty_id == null ? null : locatorTypes.get(record.loty_id));
        return dataSet;
    }

    private ContainerDataSet convertToContainerDataSet(DatasetRecord record)
    {
        ContainerDataSet containerDataSet = new ContainerDataSet();
        convertStandardProperties(containerDataSet, record);
        return containerDataSet;
    }

    private LinkDataSet convertToLinkDataSet(DatasetRecord record)
    {
        LinkDataSet linkDataSet = new LinkDataSet();

        convertStandardProperties(linkDataSet, record);
        linkDataSet.setExternalDataManagementSystem(externalDataManagementSystems
                .get(record.edms_id));
        linkDataSet.setExternalCode(record.external_code);

        return linkDataSet;
    }

    private void convertStandardProperties(ExternalData dataSet, DatasetRecord record)
    {
        dataSet.setCode(record.code);
        dataSet.setDataSetType(dataSetTypes.get(record.dsty_id));
        dataSet.setId(record.id);
        dataSet.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.DATA_SET,
                record.code));
        dataSet.setId(record.id);
        dataSet.setDataProducerCode(record.data_producer_code);
        dataSet.setDataStore(dataStores.get(record.dast_id));
        dataSet.setDerived(record.is_derived);
        dataSet.setOrderInContainer(record.ctnr_order);
        dataSet.setProductionDate(record.production_timestamp);
        dataSet.setRegistrationDate(record.registration_timestamp);
        dataSet.setRegistrator(getOrCreateActor(record.pers_id_registerer));
        dataSet.setModificationDate(record.modification_timestamp);
        dataSet.setModifier(getOrCreateActor(record.pers_id_modifier));
        dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());

        if (record.ctnr_id != null)
        {
            ContainerDataSet container = new ContainerDataSet();
            container.setId(record.ctnr_id);
            dataSet.setContainer(container);
        }

        if (record.samp_id != null)
        {
            Sample sample = new Sample();
            sample.setId(record.samp_id);
            dataSet.setSample(sample);
        }

        Experiment experiment = new Experiment();
        experiment.setId(record.expe_id);
        dataSet.setExperiment(experiment);
    }

    private Boolean resolve(String booleanRepresentative)
    {
        if (booleanRepresentative == null)
        {
            return null;
        }
        return BooleanOrUnknown.tryToResolve(BooleanOrUnknown.valueOf(booleanRepresentative));
    }

    private void loadSmallConnectedTables()
    {
        dataSetTypes.clear();
        for (DataSetTypeRecord code : query.getDatasetTypes(databaseInstanceId))
        {
            dataSetTypes.put(code.id, createDataSetType(code));
        }

        fileFormatTypes.clear();
        for (CodeRecord code : query.getFileFormatTypes(databaseInstanceId))
        {
            fileFormatTypes.put(code.id, createFileFormatType(code));
        }

        locatorTypes.clear();
        for (CodeRecord code : query.getLocatorTypes())
        {
            locatorTypes.put(code.id, createLocatorType(code));
        }

        dataStores.clear();
        for (DataStoreRecord code : query.getDataStores(databaseInstanceId))
        {
            dataStores.put(code.id, createDataStore(code));
        }

        externalDataManagementSystems.clear();
        for (ExternalDataManagementSystemRecord edms : query
                .getExternalDataManagementSystems(databaseInstanceId))
        {
            externalDataManagementSystems.put(edms.id, createExternalDataManagementSystem(edms));
        }
    }

    private static void setCode(Code<?> codeHolder, CodeRecord codeRecord)
    {
        codeHolder.setCode(codeRecord.code);
    }

    private static DataStore createDataStore(DataStoreRecord codeRecord)
    {
        DataStore result = new DataStore();
        setCode(result, codeRecord);
        result.setHostUrl(codeRecord.download_url);
        String downloadUrl = DataStoreTranslator.translateDownloadUrl(codeRecord.download_url);
        result.setDownloadUrl(downloadUrl);
        return result;
    }

    private static ExternalDataManagementSystem createExternalDataManagementSystem(
            ExternalDataManagementSystemRecord edmsRecord)
    {
        ExternalDataManagementSystem result = new ExternalDataManagementSystem();

        result.setId(edmsRecord.id);
        result.setCode(edmsRecord.code);
        result.setLabel(edmsRecord.label);
        result.setUrlTemplate(edmsRecord.url_template);
        result.setOpenBIS(edmsRecord.is_openbis);

        return result;
    }

    private static LocatorType createLocatorType(CodeRecord codeRecord)
    {
        LocatorType result = new LocatorType();
        setCode(result, codeRecord);
        return result;
    }

    private static FileFormatType createFileFormatType(CodeRecord codeRecord)
    {
        FileFormatType result = new FileFormatType();
        setCode(result, codeRecord);
        return result;
    }

    private DataSetType createDataSetType(DataSetTypeRecord record)
    {
        DataSetType result = new DataSetType();
        setCode(result, record);
        result.setDatabaseInstance(databaseInstance);
        result.setDataSetKind(DataSetKind.valueOf(record.data_set_kind));
        return result;
    }

    @Override
    public Map<Long, GenericEntityPropertyRecord> fetchProperties(List<Long> ids,
            String propertyTypeCode)
    {
        DataIterator<GenericEntityPropertyRecord> queryResult =
                query.getEntityPropertyGenericValues(new LongOpenHashSet(ids), propertyTypeCode);

        Map<Long, GenericEntityPropertyRecord> result =
                new HashMap<Long, GenericEntityPropertyRecord>();
        for (GenericEntityPropertyRecord record : queryResult)
        {
            result.put(record.entity_id, record);
        }

        return result;
    }

    @Override
    public IDatasetLocationNode listLocationsByDatasetCode(String datasetCode)
    {
        DataIterator<DatasetLocationNodeRecord> queryResult =
                query.listLocationsByDatasetCode(datasetCode);

        Map<Long, DatasetLocationNode> nodeMap = new HashMap<Long, DatasetLocationNode>();
        DatasetLocationNode rootNode = null;

        while (queryResult.hasNext())
        {
            DatasetLocationNodeRecord record = queryResult.next();

            DatasetLocation location = new DatasetLocation();
            location.setDatasetCode(record.code);
            location.setDataSetLocation(record.location);
            location.setDataStoreCode(record.data_store_code);
            location.setDataStoreUrl(record.data_store_url);

            DatasetLocationNode node = new DatasetLocationNode(location);

            if (record.ctnr_id != null)
            {
                DatasetLocationNode parentNode = nodeMap.get(record.ctnr_id);
                if (parentNode != null)
                {
                    parentNode.addContained(node);
                }
            }

            nodeMap.put(record.id, node);

            if (rootNode == null)
            {
                rootNode = node;
            }
        }

        return rootNode;
    }
}
