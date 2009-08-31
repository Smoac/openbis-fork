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

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;

/**
 * The DAO for business objects implementing {@link IDatasetLister}. Note: Even though this class is
 * public its constructors and instance methods have to be package protected.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = IDatasetListingFullQuery.class)
public final class DatasetListerDAO
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static DatasetListerDAO createDAO(IDAOFactory daoFactory)
    {
        DatabaseConfigurationContext context = DatabaseContextUtils.getDatabaseContext(daoFactory);
        final boolean supportsSetQuery = DatabaseContextUtils.isSupportingSetQueries(context);
        DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        return new DatasetListerDAO(supportsSetQuery, context.getDataSource(), homeDatabaseInstance);
    }

    private final IDatasetListingFullQuery query;

    private final IDatasetSetListingQuery idSetQuery;

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    DatasetListerDAO(final boolean supportsSetQuery, final DataSource dataSource,
            final DatabaseInstancePE databaseInstance)
    {
        this.query = QueryTool.getQuery(dataSource, IDatasetListingFullQuery.class);
        if (supportsSetQuery)
        {
            this.idSetQuery = new DatasetSetListingQueryStandard(query);
        } else
        {
            this.idSetQuery = null;//new DatasetSetListingQueryFallback(query);
        }
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

    IDatasetListingQuery getQuery()
    {
        return query;
    }

    IDatasetSetListingQuery getIdSetQuery()
    {
        return idSetQuery;
    }

}
