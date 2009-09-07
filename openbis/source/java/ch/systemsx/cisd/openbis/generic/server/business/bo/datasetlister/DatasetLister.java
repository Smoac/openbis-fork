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

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
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

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, DatasetRelationRecord.class, IDatasetListingQuery.class })
public class DatasetLister implements IDatasetLister
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

    private final IDatasetSetListingQuery setQuery;

    private final IEntityPropertiesEnricher propertiesEnricher;

    private final SecondaryEntityDAO referencedEntityDAO;

    //
    // Working data structures
    //

    private final Long2ObjectMap<DataSetType> dataSetTypes =
            new Long2ObjectOpenHashMap<DataSetType>();

    private final Long2ObjectMap<DataStore> dataStores = new Long2ObjectOpenHashMap<DataStore>();

    private final Long2ObjectMap<FileFormatType> fileFormatTypes =
            new Long2ObjectOpenHashMap<FileFormatType>();

    private final Long2ObjectMap<LocatorType> locatorTypes =
            new Long2ObjectOpenHashMap<LocatorType>();

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
        IDatasetSetListingQuery setQuery = dao.getIdSetQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new DatasetLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                setQuery, propertiesEnricher, referencedEntityDAO, baseIndexURL);
    }

    // For unit tests
    DatasetLister(final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final IDatasetListingQuery query, final IDatasetSetListingQuery setQuery,
            IEntityPropertiesEnricher propertiesEnricher, SecondaryEntityDAO referencedEntityDAO,
            String baseIndexURL)
    {
        assert databaseInstance != null;
        assert query != null;
        assert setQuery != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.setQuery = setQuery;
        this.propertiesEnricher = propertiesEnricher;
        this.referencedEntityDAO = referencedEntityDAO;
        this.baseIndexURL = baseIndexURL;
    }

    public List<ExternalData> listBySampleTechId(TechId sampleId)
    {
        return enrichDatasets(query.getDatasetsForSample(sampleId.getId()));
    }

    public List<ExternalData> listByExperimentTechId(TechId experimentId)
    {
        return enrichDatasets(query.getDatasetsForExperiment(experimentId.getId()));
    }

    public List<ExternalData> listByDatasetIds(Collection<Long> datasetIds)
    {
        return enrichDatasets(setQuery.getDatasets(new LongOpenHashSet(datasetIds)));
    }

    private List<ExternalData> enrichDatasets(Iterable<DatasetRecord> datasets)
    {
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(datasets);
        final Long2ObjectMap<ExternalData> datasetMap = createPrimaryDatasets(datasetRecords);
        enrichWithProperties(datasetMap);
        enrichWithParents(datasetMap);
        enrichWithExperiments(datasetMap);
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
            long sampleId = dataset.getSample().getId();
            Sample sample = samples.get(sampleId);
            dataset.setSample(sample);
            enrichWithInvalidation(dataset, sample);
        }
    }

    private void enrichWithInvalidation(ExternalData dataset, Sample sample)
    {
        Invalidation invalidation = sample.getInvalidation();
        dataset.setInvalidation(invalidation);
        ExternalData parent = dataset.getParent();
        if (parent != null)
        {
            parent.setInvalidation(invalidation);
        }
    }

    private static LongSet extractSampleIds(Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet ids = new LongOpenHashSet();
        for (ExternalData dataset : datasetMap.values())
        {
            long sampleId = dataset.getSample().getId();
            ids.add(sampleId);
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
            if (experiment == null)
            {
                experiment = referencedEntityDAO.getExperiment(experimentId);
                experimentMap.put(experimentId, experiment);
            }
            dataset.setExperiment(experiment);
        }
    }

    /**
     * @param datasetMap datasets for which parents have to be resolved.
     * @param datasetCache the original information about datasets,
     */
    private void enrichWithParents(final Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet datasetIds = extractIds(datasetMap);
        Long2ObjectMap<Set<ExternalData>> parentsMap = resolveParents(datasetIds, datasetMap);
        for (ExternalData dataset : datasetMap.values())
        {
            final Set<ExternalData> parent = parentsMap.get(dataset.getId());
            dataset.setParents(parent);
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

    /**
     * Returns a map from a child id to its parent dataset for the specified dataset ids.<br>
     * Uses datasetCache not to resolve datasets which have already been resolved.
     */
    private Long2ObjectMap<Set<ExternalData>> resolveParents(LongSet datasetIds,
            Long2ObjectMap<ExternalData> datasetCache)
    {
        final List<DatasetRelationRecord> datasetParents = asList(setQuery.getDatasetParents(datasetIds));
        final Long2ObjectMap<ExternalData> parentsMap =
                fetchUnknownDatasetParents(datasetParents, datasetCache);

        final Long2ObjectMap<Set<ExternalData>> childToParentMap =
                new Long2ObjectOpenHashMap<Set<ExternalData>>();
        for (DatasetRelationRecord relation : datasetParents)
        {
            long parentId = relation.data_id_parent;
            ExternalData parentDataset = getCachedItem(parentId, parentsMap, datasetCache);
            assert parentDataset != null : "inconsistent parent dataset " + parentId;
            Set<ExternalData> parents = childToParentMap.get(relation.data_id_child);
            if (parents == null)
            {
                parents = new HashSet<ExternalData>();
                childToParentMap.put(relation.data_id_child, parents);
            }
            parents.add(parentDataset);
        }
        return childToParentMap;
    }

    // takes item from the cache. First checks in the first map, but if an item is not present looks
    // in the second map
    private static <T> T getCachedItem(long id, Long2ObjectMap<T> map1, Long2ObjectMap<T> map2)
    {
        T item = map1.get(id);
        if (item == null)
        {
            item = map2.get(id);
        }
        return item;
    }

    /**
     * Returns a map dataset_id -> dataset for all datasets which are parents and are not contained
     * in a cache.
     */
    private Long2ObjectMap<ExternalData> fetchUnknownDatasetParents(
            Iterable<DatasetRelationRecord> datasetParents,
            Long2ObjectMap<ExternalData> datasetCache)
    {
        LongSet parentIds = extractUnknownParentIds(datasetParents, datasetCache);
        Iterable<DatasetRecord> unknownParents = setQuery.getDatasets(parentIds);
        Long2ObjectMap<ExternalData> parentsMap = createBasicDatasets(unknownParents);
        return parentsMap;
    }

    private static LongSet extractUnknownParentIds(Iterable<DatasetRelationRecord> datasetParents,
            Long2ObjectMap<ExternalData> datasetCache)
    {
        LongSet result = new LongOpenHashSet();
        for (DatasetRelationRecord record : datasetParents)
        {
            long parentId = record.data_id_parent;
            if (datasetCache.containsKey(parentId) == false)
            {
                result.add(parentId);
            }
        }
        return result;
    }

    private static LongSet extractIds(Long2ObjectMap<ExternalData> datasetMap)
    {
        LongSet result = new LongOpenHashSet();
        for (ExternalData dataset : datasetMap.values())
        {
            result.add(dataset.getId());
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
        org.apache.commons.collections.CollectionUtils.addAll(result, items.values().iterator());
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

    private Long2ObjectMap<ExternalData> createBasicDatasets(Iterable<DatasetRecord> records)
    {
        Long2ObjectMap<ExternalData> datasets = new Long2ObjectOpenHashMap<ExternalData>();
        for (DatasetRecord record : records)
        {
            datasets.put(record.id, createBasicDataset(record));
        }
        return datasets;
    }

    private ExternalData createPrimaryDataset(DatasetRecord record)
    {
        ExternalData dataset = createBasicDataset(record);
        dataset.setComplete(BooleanOrUnknown.tryToResolve(BooleanOrUnknown
                .valueOf(record.is_complete)));
        dataset.setDataProducerCode(escapeHtml(record.data_producer_code));
        dataset.setDataStore(dataStores.get(record.dast_id));
        dataset.setDerived(record.is_derived);

        dataset.setFileFormatType(fileFormatTypes.get(record.ffty_id));
        dataset.setLocation(escapeHtml(record.location));
        dataset.setLocatorType(locatorTypes.get(record.loty_id));
        dataset.setProductionDate(record.production_timestamp);
        dataset.setRegistrationDate(record.registration_timestamp);
        dataset.setDataSetProperties(new ArrayList<IEntityProperty>());

        Sample sample = new Sample();
        sample.setId(record.samp_id);
        dataset.setSample(sample);

        Experiment experiment = new Experiment();
        experiment.setId(record.expe_id);
        dataset.setExperiment(experiment);

        return dataset;
    }

    private ExternalData createBasicDataset(DatasetRecord record)
    {
        ExternalData dataset = new ExternalData();
        dataset.setCode(escapeHtml(record.code));
        dataset.setDataSetType(dataSetTypes.get(record.dsty_id));
        dataset.setId(record.id);
        dataset.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.DATA_SET,
                record.code));

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
        for (CodeRecord code : query.getDataStores(databaseInstanceId))
        {
            dataStores.put(code.id, createDataStore(code));
        }
    }

    private static void setCode(Code<?> codeHolder, CodeRecord codeRecord)
    {
        codeHolder.setCode(escapeHtml(codeRecord.code));
    }

    private static DataStore createDataStore(CodeRecord codeRecord)
    {
        DataStore result = new DataStore();
        setCode(result, codeRecord);
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
