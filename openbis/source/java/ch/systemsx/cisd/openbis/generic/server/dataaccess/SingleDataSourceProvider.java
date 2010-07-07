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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import javax.sql.DataSource;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * {@link DataSource} provider which always returns the same data source.
 *
 * @author Franz-Josef Elmer
 */
public class SingleDataSourceProvider implements IDataSourceProvider
{
    private DataSource dataSource;

    /**
     * Creates a new instance based on the data source of the specified context.
     */
    public SingleDataSourceProvider(DatabaseConfigurationContext context)
    {
        dataSource = context.getDataSource();
    }

    public DataSource getDataSourceByDataSetCode(String dataSetCode, String technology)
    {
        return dataSource;
    }

    public DataSource getDataSourceByDataStoreServerCode(String dssCode, String technology)
    {
        return dataSource;
    }

}
