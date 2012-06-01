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
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSourceQueryService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSourceQueryService implements IDataSourceQueryService
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSourceQueryService.class);

    private IDataSourceProvider getDataSourceProvider()
    {
        return ServiceProvider.getDataSourceProvider();
    }

    @Override
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

    @Override
    public DataSet<Map<String, Object>> select(String dataSourceName, String query)
            throws IllegalArgumentException
    {
        return select(dataSourceName, query, new Object[0]);
    }

}
