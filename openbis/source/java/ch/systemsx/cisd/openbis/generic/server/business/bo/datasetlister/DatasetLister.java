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

import java.util.List;

import net.lemnik.eodsql.DataIterator;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * @author Tomasz Pylak
 */
public class DatasetLister implements IDatasetLister
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetLister.class);

    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    //
    // Working interfaces
    //

    private final IDatasetListingQuery query;

    private final IDatasetSetListingQuery setQuery;

    private final IEntityPropertiesEnricher propertiesEnricherOrNull;

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
            IEntityPropertiesEnricher propertiesEnricherOrNull)
    {
        assert databaseInstance != null;
        assert query != null;
        assert setQuery != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.setQuery = setQuery;
        this.propertiesEnricherOrNull = propertiesEnricherOrNull;
    }

    public List<ExternalData> listByExperimentTechId(TechId experimentId)
    {
        loadSmallConnectedTables();
        DataIterator<DatasetRecord> datasets = query.getDatasetsForExperiment(experimentId.getId());

        return null;
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
        for (CodeRecord code : query.getLocatorTypes(databaseInstanceId))
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
