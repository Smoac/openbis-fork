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

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Utility class creating a {@link SampleListerDAO}.
 *
 * @author Franz-Josef Elmer
 */
public class SampleListerDAOFactory
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static SampleListerDAO createSampleListerDAO(IDAOFactory daoFactory)
    {
        PersistencyResources persistencyResources = daoFactory.getPersistencyResources();
        DatabaseConfigurationContext context = persistencyResources.getContextOrNull();
        if (context == null)
        {
            throw new ConfigurationFailureException("Missing database configuration context.");
        }
        // H2 does not support set queries ("=ANY()" operator)
        final boolean supportsSetQuery =
                (DatabaseEngine.H2.getCode().equals(context.getDatabaseEngineCode()) == false);
        DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        return new SampleListerDAO(true, supportsSetQuery, context.getDataSource(),
                homeDatabaseInstance);
    }
}
