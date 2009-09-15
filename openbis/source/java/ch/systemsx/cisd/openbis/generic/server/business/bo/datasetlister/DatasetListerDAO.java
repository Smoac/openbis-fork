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

import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.Connection;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.AbstractDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertySetListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.PropertiesSetListingQueryFallback;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser.IEntitiesCountProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * The DAO for business objects implementing {@link IDatasetLister}. Note: Even though this class is
 * public its constructors and instance methods have to be package protected.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { IDatasetListingFullQuery.class, IEntityPropertyListingQuery.class,
            DatasetRelationRecord.class })
public final class DatasetListerDAO extends AbstractDAO
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static DatasetListerDAO create(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        IDatasetListingFullQuery query =
                QueryTool.getQuery(connection, IDatasetListingFullQuery.class);
        return create(daoFactory, query);
    }

    @Private
    // only for tests
    static DatasetListerDAO create(IDAOFactory daoFactory, IDatasetListingFullQuery query)
    {
        DatabaseConfigurationContext context = DatabaseContextUtils.getDatabaseContext(daoFactory);
        final boolean supportsSetQuery = DatabaseContextUtils.isSupportingSetQueries(context);
        DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        return new DatasetListerDAO(supportsSetQuery, query, homeDatabaseInstance);
    }

    private final IDatasetListingFullQuery query;

    private final IDatasetSetListingQuery setQuery;

    private final IEntityPropertySetListingQuery propertySetQuery;

    private final QueryStrategyChooser strategyChooser;

    DatasetListerDAO(final boolean supportsSetQuery, IDatasetListingFullQuery query,
            final DatabaseInstancePE databaseInstance)
    {
        super(databaseInstance);
        this.query = query;
        this.strategyChooser = createStrategyChooser(query);
        this.setQuery =
                createIdSetQuery(supportsSetQuery, query, strategyChooser, getDatabaseInstanceId());
        this.propertySetQuery =
                createSetPropertyQuery(supportsSetQuery, query, strategyChooser,
                        getDatabaseInstanceId());
    }

    IDatasetListingQuery getQuery()
    {
        return query;
    }

    IDatasetSetListingQuery getIdSetQuery()
    {
        return setQuery;
    }

    IEntityPropertySetListingQuery getPropertySetQuery()
    {
        return propertySetQuery;
    }

    private static QueryStrategyChooser createStrategyChooser(final IDatasetListingFullQuery query)
    {
        return new QueryStrategyChooser(new IEntitiesCountProvider()
            {
                public long count()
                {
                    return query.getDatasetCount();
                }
            });
    }

    private static IEntityPropertySetListingQuery createSetPropertyQuery(boolean supportsSetQuery,
            IDatasetListingFullQuery query, QueryStrategyChooser strategyChooser,
            long databaseInstanceId)
    {
        if (supportsSetQuery)
        {
            return asEntitySetPropertyListingQuery(query);
        } else
        {
            return new PropertiesSetListingQueryFallback(asEntityPropertyListingQuery(query,
                    databaseInstanceId), strategyChooser);
        }
    }

    private static IEntityPropertyListingQuery asEntityPropertyListingQuery(
            final IDatasetListingFullQuery query, final long databaseInstanceId)
    {
        return new IEntityPropertyListingQuery()
            {
                public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues()
                {
                    return query.getAllEntityPropertyGenericValues(databaseInstanceId);
                }

                public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
                        long entityId)
                {
                    return query.getEntityPropertyGenericValues(entityId);
                }

                public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues()
                {
                    return query.getAllEntityPropertyMaterialValues(databaseInstanceId);
                }

                public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
                        long sampleId)
                {
                    return query.getEntityPropertyMaterialValues(sampleId);
                }

                public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues()
                {
                    return query.getAllEntityPropertyVocabularyTermValues(databaseInstanceId);
                }

                public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
                        long sampleId)
                {
                    return query.getEntityPropertyVocabularyTermValues(sampleId);
                }
            };
    }

    private static IEntityPropertySetListingQuery asEntitySetPropertyListingQuery(
            final IDatasetListingFullQuery query)
    {
        return new IEntityPropertySetListingQuery()
            {
                public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
                        LongSet entityIDs)
                {
                    return query.getEntityPropertyGenericValues(entityIDs);
                }

                public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
                        LongSet entityIDs)
                {
                    return query.getEntityPropertyMaterialValues(entityIDs);
                }

                public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
                        LongSet entityIDs)
                {
                    return query.getEntityPropertyVocabularyTermValues(entityIDs);
                }
            };
    }

    private static IDatasetSetListingQuery createIdSetQuery(boolean supportsSetQuery,
            IDatasetListingFullQuery query, QueryStrategyChooser strategyChooser,
            long databaseInstanceId)
    {
        if (supportsSetQuery)
        {
            return asDatasetSetListingQuery(query);
        } else
        {
            return new DatasetSetListingQueryFallback(query, strategyChooser, databaseInstanceId);
        }
    }

    private static IDatasetSetListingQuery asDatasetSetListingQuery(
            final IDatasetListingFullQuery query)
    {
        return new IDatasetSetListingQuery()
            {
                public Iterable<DatasetRecord> getDatasets(LongSet sampleIds)
                {
                    return query.getDatasets(sampleIds);
                }

                public Iterable<DatasetRelationRecord> getDatasetRelationsWithParents(
                        LongSet entityIds)
                {
                    return query.getDatasetParents(entityIds);
                }

                public DataIterator<Long> getDatasetChildrenIds(LongSet entityIds)
                {
                    return query.getDatasetChildrenIds(entityIds);
                }
            };
    }

}
