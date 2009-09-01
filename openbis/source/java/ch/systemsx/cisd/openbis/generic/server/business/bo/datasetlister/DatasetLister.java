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

import java.util.ArrayList;
import java.util.List;

import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class })
public class DatasetLister implements IDatasetLister
{
    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    //
    // Working interfaces
    //

    private final IDatasetListingQuery query;

    // private final IDatasetSetListingQuery setQuery;

    private final IEntityPropertiesEnricher propertiesEnricher;

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

    public static DatasetLister create(DatasetListerDAO dao)
    {
        IDatasetListingQuery query = dao.getQuery();
        IDatasetSetListingQuery setQuery = dao.getIdSetQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new DatasetLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                setQuery, propertiesEnricher);
    }

    // For unit tests
    DatasetLister(final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final IDatasetListingQuery query, final IDatasetSetListingQuery setQuery,
            IEntityPropertiesEnricher propertiesEnricher)
    {
        assert databaseInstance != null;
        assert query != null;
        assert setQuery != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        // this.setQuery = setQuery;
        this.propertiesEnricher = propertiesEnricher;
    }

    public List<ExternalData> listByExperimentTechId(TechId experimentId)
    {
        loadSmallConnectedTables();
        DataIterator<DatasetRecord> datasets = query.getDatasetsForExperiment(experimentId.getId());
        final Long2ObjectMap<ExternalData> resultMap = createDatasets(datasets);
        propertiesEnricher.enrich(resultMap.keySet(), new IEntityPropertiesHolderResolver()
            {
                public ExternalData get(long id)
                {
                    return resultMap.get(id);
                }
            });

        return asList(resultMap);
    }

    private static <T> List<T> asList(Long2ObjectMap<T> items)
    {
        List<T> result = new ArrayList<T>();
        org.apache.commons.collections.CollectionUtils.addAll(result, items.values().iterator());
        return result;
    }

    private Long2ObjectMap<ExternalData> createDatasets(DataIterator<DatasetRecord> records)
    {
        Long2ObjectMap<ExternalData> datasets = new Long2ObjectOpenHashMap<ExternalData>();
        for (DatasetRecord record : records)
        {
            datasets.put(record.id, createDataset(record));
        }
        return datasets;
    }

    private ExternalData createDataset(DatasetRecord record)
    {
        ExternalData dataset = new ExternalData();
        dataset.setCode(record.code);
        dataset.setComplete(BooleanOrUnknown.tryToResolve(BooleanOrUnknown
                .valueOf(record.is_complete)));
        dataset.setDataProducerCode(record.data_producer_code);
        dataset.setDataSetType(dataSetTypes.get(record.dsty_id));
        dataset.setDataStore(dataStores.get(record.dast_id));
        dataset.setDerived(record.is_derived);

        dataset.setFileFormatType(fileFormatTypes.get(record.ffty_id));
        dataset.setId(record.id);
        dataset.setLocation(record.location);
        dataset.setLocatorType(locatorTypes.get(record.loty_id));
        dataset.setProductionDate(record.production_timestamp);
        dataset.setRegistrationDate(record.registration_timestamp);
        dataset.setDataSetProperties(new ArrayList<IEntityProperty>());
        Sample sample = new Sample();
        SampleType sampleType = new SampleType();
        sampleType.setCode("sampleType");
        sampleType.setDatabaseInstance(databaseInstance);
        sample.setSampleType(sampleType);
        dataset.setSample(sample);
        // dataset.setInvalidation(record.); // from sample
        // dataset.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
        // EntityKind.DATA_SET, record.code));
        // dataset.setParent(record.);
        // dataset.setExperiment(record.);
        // dataset.setRegistrator(record.);
        // dataset.setSample(record.);

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

    private static DataStore createDataStore(CodeRecord codeRecord)
    {
        DataStore result = new DataStore();
        result.setCode(codeRecord.code);
        return result;
    }

    private static LocatorType createLocatorType(CodeRecord codeRecord)
    {
        LocatorType result = new LocatorType();
        result.setCode(codeRecord.code);
        return result;
    }

    private static FileFormatType createFileFormatType(CodeRecord codeRecord)
    {
        FileFormatType result = new FileFormatType();
        result.setCode(codeRecord.code);
        return result;
    }

    private DataSetType createDataSetType(CodeRecord codeRecord)
    {
        DataSetType result = new DataSetType();
        result.setCode(codeRecord.code);
        result.setDatabaseInstance(databaseInstance);
        return result;
    }
}
