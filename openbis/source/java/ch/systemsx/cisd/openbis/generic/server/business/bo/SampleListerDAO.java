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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.openbis.generic.client.web.server.translator.DatabaseInstanceTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingFullQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleSetListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SampleSetListingQueryFallback;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SampleSetListingQueryStandard;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * The DAO for {@link SampleLister} objects.
 * 
 * @author Bernd Rinn
 */
public final class SampleListerDAO implements ISampleListerDAO
{
    private final boolean enabled;

    private final ISampleListingFullQuery query;

    private final ISampleSetListingQuery idSetQuery;

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    public SampleListerDAO(final boolean enabled, final boolean supportsSetQuery,
            final DataSource dataSource, final DatabaseInstancePE databaseInstance)
    {
        this.enabled = enabled;
        if (enabled)
        {
            this.query = QueryTool.getQuery(dataSource, ISampleListingFullQuery.class);
            if (supportsSetQuery)
            {
                this.idSetQuery = new SampleSetListingQueryStandard(query);
            } else
            {
                this.idSetQuery = new SampleSetListingQueryFallback(query);
            }
            this.databaseInstanceId = databaseInstance.getId();
            this.databaseInstance = DatabaseInstanceTranslator.translate(databaseInstance);
        } else
        {
            this.query = null;
            this.idSetQuery = null;
            this.databaseInstanceId = 0;
            this.databaseInstance = null;
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public long getDatabaseInstanceId()
    {
        return databaseInstanceId;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public ISampleListingQuery getQuery()
    {
        return query;
    }

    public ISampleSetListingQuery getIdSetQuery()
    {
        return idSetQuery;
    }

}
