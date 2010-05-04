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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Properties;

import javax.sql.DataSource;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Creates a {@link DataSource} using {@link DatabaseConfigurationContext} and given properties. The
 * database is migrated to the version specified by {@link #VERSION_HOLDER_CLASS_KEY} property
 * if specified.
 * 
 * @author Izabela Adamczyk
 */
public class DefaultDataSourceFactory implements IDataSourceFactory
{

    static final String VERSION_HOLDER_CLASS_KEY = "version-holder-class";

    public DataSource create(Properties dbProps)
    {
        DatabaseConfigurationContext context =
                BeanUtils.createBean(DatabaseConfigurationContext.class, dbProps);
        if (context.getBasicDatabaseName() == null)
        {
            throw new ConfigurationFailureException("db basic name not specified in " + dbProps);
        }
        if (context.getDatabaseEngineCode() == null)
        {
            throw new ConfigurationFailureException("db engine code not specified in " + dbProps);
        }
        String versionClass = dbProps.getProperty(VERSION_HOLDER_CLASS_KEY);
        if (versionClass != null)
        {
            IDatabaseVersionHolder versionHolder =
                ClassUtils.create(IDatabaseVersionHolder.class, versionClass);
            String version = versionHolder.getDatabaseVersion();
            DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, version);
        }
        return context.getDataSource();
    }
}
