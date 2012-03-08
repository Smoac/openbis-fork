/*
 * Copyright 2012 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.server.dataaccess.DataStoreServerBasedDataSourceProvider.DATA_STORE_SERVERS_KEY;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class DataStoreServerBasedDataSourceProviderTest extends AssertJUnit
{
    private Mockery context;

    private IDAOFactory daoFactory;

    private DataStoreServerBasedDataSourceProvider dataSourceProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        dataSourceProvider = new DataStoreServerBasedDataSourceProvider(daoFactory);
        Properties props = new Properties();
        props.setProperty(DATA_STORE_SERVERS_KEY, "dss1, dss2[tech1], dss2[tech2]");
        props.setProperty("dss1.database-driver", "org.postgresql.Driver");
        props.setProperty("dss1.database-url", "jdbc:postgresql://localhost/db1");
        props.setProperty("dss2[tech1].database-driver", "org.postgresql.Driver");
        props.setProperty("dss2[tech1].database-url", "jdbc:postgresql://localhost/db21");
        props.setProperty("dss2[tech2].database-driver", "org.postgresql.Driver");
        props.setProperty("dss2[tech2].database-url", "jdbc:postgresql://localhost/db22");
        dataSourceProvider.init(props);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSourceByDataStoreServerCode()
    {
        assertURL("jdbc:postgresql://localhost/db1",
                dataSourceProvider.getDataSourceByDataStoreServerCode("dss1", "my-tech"));
        assertURL("jdbc:postgresql://localhost/db21",
                dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "tech1"));
        assertURL("jdbc:postgresql://localhost/db22",
                dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "tech2"));

        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSourceByDataStoreServerCodeNotFound()
    {
        try
        {
            dataSourceProvider.getDataSourceByDataStoreServerCode("dss2", "my-tech");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "No data source configured for Data Store Server 'dss2' and technology 'my-tech'.",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    void assertURL(String expectedURL, DataSource dataSource)
    {
        assertNotNull(dataSource);
        if (dataSource instanceof BasicDataSource)
        {
            BasicDataSource basicDataSource = (BasicDataSource) dataSource;
            assertEquals(expectedURL, basicDataSource.getUrl());
        }
    }
}
