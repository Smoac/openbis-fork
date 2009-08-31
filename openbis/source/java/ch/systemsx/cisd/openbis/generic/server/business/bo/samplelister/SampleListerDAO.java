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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import it.unimi.dsi.fastutil.longs.LongSet;

import javax.sql.DataSource;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntitySetPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.PropertiesSetListingQueryFallback;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser.IEntitiesCountProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;

/**
 * The DAO for business objects implementing {@link ISampleLister}. Note: Even though this class is
 * public its constructors and instance methods have to be package protected.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = ISampleListingFullQuery.class)
public final class SampleListerDAO
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static SampleListerDAO createDAO(IDAOFactory daoFactory)
    {
        DatabaseConfigurationContext context = DatabaseContextUtils.getDatabaseContext(daoFactory);
        final boolean supportsSetQuery = DatabaseContextUtils.isSupportingSetQueries(context);
        DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        return new SampleListerDAO(supportsSetQuery, context.getDataSource(), homeDatabaseInstance);
    }

    private final ISampleListingFullQuery query;

    private final ISampleSetListingQuery setQuery;

    private final IEntitySetPropertyListingQuery propertySetQuery;

    private final QueryStrategyChooser strategyChooser;

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    SampleListerDAO(final boolean supportsSetQuery, final DataSource dataSource,
            final DatabaseInstancePE databaseInstance)
    {
        this.query = QueryTool.getQuery(dataSource, ISampleListingFullQuery.class);
        this.strategyChooser = createStrategyChooser(query);
        this.setQuery = createIdSetQuery(supportsSetQuery, query, strategyChooser);
        this.propertySetQuery = createSetPropertyQuery(supportsSetQuery, query, strategyChooser);

        this.databaseInstanceId = databaseInstance.getId();
        this.databaseInstance = DatabaseInstanceTranslator.translate(databaseInstance);
    }

    long getDatabaseInstanceId()
    {
        return databaseInstanceId;
    }

    DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    ISampleListingQuery getQuery()
    {
        return query;
    }

    ISampleSetListingQuery getIdSetQuery()
    {
        return setQuery;
    }

    IEntitySetPropertyListingQuery getPropertySetQuery()
    {
        return propertySetQuery;
    }

    private static QueryStrategyChooser createStrategyChooser(final ISampleListingFullQuery query)
    {
        return new QueryStrategyChooser(new IEntitiesCountProvider()
            {
                public long count()
                {
                    return query.getSampleCount();
                }
            });
    }

    private static IEntitySetPropertyListingQuery createSetPropertyQuery(boolean supportsSetQuery,
            ISampleListingFullQuery query, QueryStrategyChooser strategyChooser)
    {
        if (supportsSetQuery)
        {
            return asEntitySetPropertyListingQuery(query);
        } else
        {
            return new PropertiesSetListingQueryFallback(asEntityPropertyListingQuery(query),
                    strategyChooser);
        }
    }

    private static IEntityPropertyListingQuery asEntityPropertyListingQuery(
            final ISampleListingFullQuery query)
    {
        return new IEntityPropertyListingQuery()
            {
                public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues()
                {
                    return query.getEntityPropertyGenericValues();
                }

                public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
                        long entityId)
                {
                    return query.getEntityPropertyGenericValues(entityId);
                }

                public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues()
                {
                    return query.getEntityPropertyMaterialValues();
                }

                public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
                        long sampleId)
                {
                    return query.getEntityPropertyMaterialValues(sampleId);
                }

                public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues()
                {
                    return query.getEntityPropertyVocabularyTermValues();
                }

                public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
                        long sampleId)
                {
                    return query.getEntityPropertyVocabularyTermValues(sampleId);
                }
            };
    }

    private static IEntitySetPropertyListingQuery asEntitySetPropertyListingQuery(
            final ISampleListingFullQuery query)
    {
        return new IEntitySetPropertyListingQuery()
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

    private static ISampleSetListingQuery createIdSetQuery(boolean supportsSetQuery,
            ISampleListingFullQuery query, QueryStrategyChooser strategyChooser)
    {
        if (supportsSetQuery)
        {
            return asSampleSetListingQuery(query);
        } else
        {
            return new SampleSetListingQueryFallback(query, strategyChooser);
        }
    }

    private static ISampleSetListingQuery asSampleSetListingQuery(
            final ISampleListingFullQuery query)
    {
        return new ISampleSetListingQuery()
            {
                public Iterable<SampleRecord> getSamples(LongSet sampleIds)
                {
                    return query.getSamples(sampleIds);
                }
            };
    }
}
