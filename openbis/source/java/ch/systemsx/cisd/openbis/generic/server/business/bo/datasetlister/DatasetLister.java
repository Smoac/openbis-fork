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
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.common.utilities.ReflectingStringEscaper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, DatasetRelationRecord.class, DataStoreRecord.class,
            IDatasetListingQuery.class })
public class DatasetLister extends AbstractLister implements IDatasetLister
{
    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    private final String baseIndexURL;

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

    public static IDatasetLister create(IDAOFactory daoFactory, String baseIndexURL)
    {
        DatasetListerDAO dao = DatasetListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);

        return create(dao, referencedEntityDAO, baseIndexURL);
    }

    static IDatasetLister create(DatasetListerDAO dao, SecondaryEntityDAO referencedEntityDAO,
            String baseIndexURL)
    {
        IDatasetListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new DatasetLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                propertiesEnricher, referencedEntityDAO, baseIndexURL);
    }

    // For unit tests
    DatasetLister(final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final IDatasetListingQuery query, IEntityPropertiesEnricher propertiesEnricher,
            SecondaryEntityDAO referencedEntityDAO, String baseIndexURL)
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
    }

    public List<ExternalData> listBySampleTechId(TechId sampleId, boolean showOnlyDirectlyConnected)
    {
        if (showOnlyDirectlyConnected)
        {
            return enrichDatasets(query.getDatasetsForSample(sampleId.getId()));
        } else
        {
            // first get directly connected datasets, then go layer by layer into children datasets
            LongSet results = new LongOpenHashSet();
            LongSet currentLayer =
                    new LongOpenHashSet(query.getDatasetIdsForSample(sampleId.getId()));
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

    public List<ExternalData> listBySampleIds(Collection<Long> sampleIds)
    {
        LongSet ids = new LongOpenHashSet();
        for (Long id : sampleIds)
        {
            ids.add(id);
        }
        return enrichDatasets(query.getDatasetsForSamples(ids));
    }

    public List<ExternalData> listByExperimentTechIds(Collection<TechId> experimentIds)
    {
        LongSet ids = new LongOpenHashSet();
        for (TechId techId : experimentIds)
        {
            ids.add(techId.getId());
        }
        return enrichDatasets(query.getDatasetsForExperiment(ids));
    }

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

    public Map<Sample, List<ExternalData>> listAllDataSetsFor(List<Sample> samples)
    {
        TableMap<Long, Sample> samplesByID =
                new TableMap<Long, Sample>(samples, new IKeyExtractor<Long, Sample>()
                    {
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

    public List<ExternalData> listByChildTechId(TechId childDatasetId)
    {
        return enrichDatasets(query.getParentDatasetsForChild(childDatasetId.getId()));
    }

    public List<ExternalData> listByParentTechIds(Collection<Long> parentDatasetIds)
    {
        return enrichDatasets(query
                .getChildDatasetsForParents(new LongOpenHashSet(parentDatasetIds)));
    }

    public List<ExternalData> listByDatasetIds(Collection<Long> datasetIds)
    {
        return enrichDatasets(query.getDatasets(new LongOpenHashSet(datasetIds)));
    }

    public List<ExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria)
    {
        Long sampleTypeId =
                referencedEntityDAO.getSampleTypeIdForSampleTypeCode(criteria
                        .getConnectedSampleTypeCode());
        return enrichDatasets(query.getNewDataSetsForSampleType(sampleTypeId,
                criteria.getLastSeenDataSetId()));
    }

    public List<ExternalData> listByArchiverCriteria(String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        loadSmallConnectedTables();
        final Long dataStoreId = extractDataStoreId(dataStoreCode);
        final Date lastRegistrationDate = extractLastRegistrationDate(criteria);
        final String dataSetTypeCodeOrNull = criteria.tryGetDataSetTypeCode();
        if (dataSetTypeCodeOrNull == null)
        {
            return enrichDatasets(query.getAvailableDataSetsRegisteredBefore(dataStoreId,
                    lastRegistrationDate));
        } else
        {
            Long dataSetTypeId = extractDataSetTypeId(dataSetTypeCodeOrNull);
            return enrichDatasets(query.getAvailableDataSetsRegisteredBeforeWithDataSetType(
                    dataStoreId, lastRegistrationDate, dataSetTypeId));
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
        return asList(datasetMap);
    }

    // assumes that the connection to the sample has been already established and sample has the
    // id set. Should be called after enrichWithParents to ensure that parents invalidation field
    // will be properly set.
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
                enrichWithInvalidation(dataset, sample);
            } else
            {
                enrichWithInvalidation(dataset, dataset.getExperiment());
            }
        }
    }

    private void enrichWithInvalidation(ExternalData dataset, Sample sample)
    {
        Invalidation invalidation = sample.getInvalidation();
        dataset.setInvalidation(invalidation);
    }

    private void enrichWithInvalidation(ExternalData dataset, Experiment experiment)
    {
        Invalidation invalidation = experiment.getInvalidation();
        dataset.setInvalidation(invalidation);
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
                public ExternalData get(long id)
                {
                    return resultMap.get(id);
                }
            });
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
            datasets.put(record.id, createPrimaryDataset(record));
        }
        return datasets;
    }

    private ExternalData createPrimaryDataset(DatasetRecord record)
    {
        ExternalData dataset = createBasicDataset(record);
        dataset.setId(record.id);
        dataset.setComplete(resolve(record.is_complete));
        dataset.setDataProducerCode(record.data_producer_code);
        dataset.setDataStore(dataStores.get(record.dast_id));
        dataset.setDerived(record.is_derived);
        dataset.setStatus(DataSetArchivingStatus.valueOf(record.status));

        dataset.setFileFormatType(fileFormatTypes.get(record.ffty_id));
        dataset.setLocation(record.location);
        dataset.setLocatorType(locatorTypes.get(record.loty_id));
        dataset.setProductionDate(record.production_timestamp);
        dataset.setRegistrationDate(record.registration_timestamp);
        dataset.setRegistrator(getOrCreateRegistrator(record.pers_id_registerer));
        dataset.setDataSetProperties(new ArrayList<IEntityProperty>());

        ReflectingStringEscaper.escapeShallow(dataset, "dataProducerCode", "location");

        if (record.samp_id != null)
        {
            Sample sample = new Sample();
            sample.setId(record.samp_id);
            dataset.setSample(sample);
        }

        Experiment experiment = new Experiment();
        experiment.setId(record.expe_id);
        dataset.setExperiment(experiment);

        return dataset;
    }

    private Boolean resolve(String booleanRepresentative)
    {
        if (booleanRepresentative == null)
        {
            return null;
        }
        return BooleanOrUnknown.tryToResolve(BooleanOrUnknown.valueOf(booleanRepresentative));
    }

    private ExternalData createBasicDataset(DatasetRecord record)
    {
        ExternalData dataset = new ExternalData();
        dataset.setCode(record.code);
        dataset.setDataSetType(dataSetTypes.get(record.dsty_id));
        dataset.setId(record.id);
        dataset.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.DATA_SET,
                record.code));

        ReflectingStringEscaper.escapeShallow(dataset, "code");
        return dataset;
    }

    private void loadSmallConnectedTables()
    {
        dataSetTypes.clear();
        for (CodeRecord code : query.getDatasetTypes(databaseInstanceId))
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
    }

    private static void setCode(Code<?> codeHolder, CodeRecord codeRecord)
    {
        codeHolder.setCode(codeRecord.code);
        ReflectingStringEscaper.escapeShallow(codeHolder, "code");
    }

    private static DataStore createDataStore(DataStoreRecord codeRecord)
    {
        DataStore result = new DataStore();
        setCode(result, codeRecord);
        String downloadUrl = DataStoreTranslator.translateDownloadUrl(codeRecord.download_url);
        result.setDownloadUrl(downloadUrl);
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

    private DataSetType createDataSetType(CodeRecord codeRecord)
    {
        DataSetType result = new DataSetType();
        setCode(result, codeRecord);
        result.setDatabaseInstance(databaseInstance);
        return result;
    }

}
