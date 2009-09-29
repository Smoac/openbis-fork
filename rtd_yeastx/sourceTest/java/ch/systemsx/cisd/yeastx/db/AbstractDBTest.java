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

package ch.systemsx.cisd.yeastx.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.testng.annotations.BeforeClass;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Abstract test case for database related unit testing.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractDBTest
{
    static
    {
        LogInitializer.init();
        DBUtils.init(getDatabaseContext());
    }

    protected DataSource datasource;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws SQLException
    {
        datasource = getDatabaseContext().getDataSource();
    }

    private static DatabaseConfigurationContext getDatabaseContext()
    {
        final DatabaseConfigurationContext context = DBUtils.createDefaultDBContext();
        context.setDatabaseKind("dbtest");
        context.setCreateFromScratch(true);
        return context;
    }

}
