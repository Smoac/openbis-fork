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

import java.sql.Connection;

import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.AbstractDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertySetListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * The DAO for business objects implementing {@link ISampleLister}. Note: Even though this class is
 * public its constructors and instance methods have to be package protected.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { ISampleListingQuery.class })
public final class SampleListerDAO extends AbstractDAO
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static SampleListerDAO create(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        ISampleListingQuery query = QueryTool.getQuery(connection, ISampleListingQuery.class);
        return create(daoFactory, query);
    }

    @Private
    static SampleListerDAO create(IDAOFactory daoFactory, ISampleListingQuery query)
    {
        DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        return new SampleListerDAO(query, homeDatabaseInstance);
    }

    private final ISampleListingQuery query;

    private final IEntityPropertySetListingQuery propertySetQuery;

    SampleListerDAO(ISampleListingQuery query, final DatabaseInstancePE databaseInstance)
    {
        super(databaseInstance);
        this.query = query;
        this.propertySetQuery = asEntityPropertySetListingQuery(query);
    }

    ISampleListingQuery getQuery()
    {
        return query;
    }

    IEntityPropertySetListingQuery getPropertySetQuery()
    {
        return propertySetQuery;
    }

    private static IEntityPropertySetListingQuery asEntityPropertySetListingQuery(
            final ISampleListingQuery query)
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

}
