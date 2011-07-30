/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.Map;

import javax.sql.DataSource;

import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSourceQueryService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSourceQueryService implements IDataSourceQueryService
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSourceQueryService.class);

    private DataSourceProvider getDataSourceProvider()
    {
        return ServiceProvider.getDataSourceProvider();
    }

    public DataSet<Map<String, Object>> select(String dataSourceName, String query,
            Object... parameters)
    {
        final DataSource dataSource = getDataSourceProvider().getDataSource(dataSourceName);
        try
        {
            return QueryTool.select(dataSource, query, parameters);
        } catch (InvalidQueryException ex)
        {
            operationLog.error(ex.getCause().getMessage());
            throw ex;
        }
    }

    public DataSet<Map<String, Object>> select(String dataSourceName, String query)
            throws IllegalArgumentException
    {
        return select(dataSourceName, query, new Object[0]);
    }

    public int update(String dataSourceName, String query, Object... parameters)
            throws IllegalArgumentException
    {
        final DataSource dataSource = getDataSourceProvider().getDataSource(dataSourceName);
        try
        {
            return QueryTool.update(dataSource, query, parameters);
        } catch (InvalidQueryException ex)
        {
            operationLog.error(ex.getCause().getMessage());
            throw ex;
        }
    }

    public int update(String dataSourceName, String query) throws IllegalArgumentException
    {
        return update(dataSourceName, query, new Object[0]);
    }

    public long insert(String dataSourceName, String query, Object... parameters)
            throws IllegalArgumentException
    {
        final DataSource dataSource = getDataSourceProvider().getDataSource(dataSourceName);
        try
        {
            return QueryTool.insert(dataSource, query, parameters);
        } catch (InvalidQueryException ex)
        {
            operationLog.error(ex.getCause().getMessage());
            throw ex;
        }
    }

    public long insert(String dataSourceName, String query) throws IllegalArgumentException
    {
        return insert(dataSourceName, query, new Object[0]);
    }

    public Map<String, Object> insertMultiKeys(String dataSourceName, String[] generatedIdColumns,
            String query, Object... parameters) throws IllegalArgumentException
    {
        final DataSource dataSource = getDataSourceProvider().getDataSource(dataSourceName);
        try
        {
            return QueryTool.insertMultiKeys(dataSource, generatedIdColumns, query, parameters);
        } catch (InvalidQueryException ex)
        {
            operationLog.error(ex.getCause().getMessage());
            throw ex;
        }
    }

    public Map<String, Object> insertMultiKeys(String dataSourceName, String[] generatedIdColumns,
            String query) throws IllegalArgumentException
    {
        return insertMultiKeys(dataSourceName, generatedIdColumns, query, new Object[0]);
    }

}
