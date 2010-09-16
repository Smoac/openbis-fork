/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DBUtils
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "007";

    private static final String DATABASE_PROPERTIES_PREFIX = "database.";
    
    public static DatabaseConfigurationContext createAndInitDBContext(Properties properties)
    {
        final DatabaseConfigurationContext dbContext = createDBContext(properties);
        DBUtils.init(dbContext);
        return dbContext;
    }

    public static DatabaseConfigurationContext createDBContext(Properties properties)
    {
        final Properties dbProps =
                ExtendedProperties.getSubset(properties, DATABASE_PROPERTIES_PREFIX, true);
        DatabaseConfigurationContext context =
                BeanUtils.createBean(DatabaseConfigurationContext.class, dbProps);
        if (context.getBasicDatabaseName() == null)
        {
            throw new EnvironmentFailureException("db basic name not specified in " + dbProps);
        }
        if (context.getDatabaseEngineCode() == null)
        {
            throw new EnvironmentFailureException("db engine code not specified in " + dbProps);
        }
        final DatabaseConfigurationContext dbContext = context;
        return dbContext;
    }

    /**
     * Checks the database specified by <var>context</var> and migrates it to the current version if
     * necessary.
     */
    public static void init(DatabaseConfigurationContext context)
    {
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);
    }
    
    private DBUtils()
    {
    }

}
