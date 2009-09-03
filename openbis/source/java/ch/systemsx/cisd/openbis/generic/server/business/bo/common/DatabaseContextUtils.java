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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import java.sql.Connection;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;

/**
 * Utilities for operating on {@link DatabaseConfigurationContext}.
 * 
 * @author Tomasz Pylak
 */
public class DatabaseContextUtils
{

    @SuppressWarnings("deprecation")
    public static Connection getConnection(IDAOFactory daoFactory)
    {
        return daoFactory.getSessionFactory().getCurrentSession().connection();
    }

    /**
     * @return true if the database supports set queries. <br>
     *         Note: H2 does not support set queries ("=ANY()" operator).
     */
    public static boolean isSupportingSetQueries(DatabaseConfigurationContext context)
    {
        return (DatabaseEngine.H2.getCode().equals(context.getDatabaseEngineCode()) == false);
    }

    /**
     * @return associated database configuration context
     * @throws ConfigurationFailureException if it was impossible to get the context
     */
    public static DatabaseConfigurationContext getDatabaseContext(IDAOFactory daoFactory)
    {
        PersistencyResources persistencyResources = daoFactory.getPersistencyResources();
        DatabaseConfigurationContext context = persistencyResources.getContextOrNull();
        if (context == null)
        {
            throw new ConfigurationFailureException("Missing database configuration context.");
        }
        return context;
    }
}
