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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.ExposablePropertyPaceholderConfigurer;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.QUERY_PLUGIN_SERVER)
public class QueryServer extends AbstractServer<IQueryServer> implements
        IQueryServer
{
    @Resource(name = "propertyConfigurer")
    private ExposablePropertyPaceholderConfigurer configurer;
    
    private DatabaseDefinition databaseDefinition;
    
    public QueryServer()
    {
    }
    
    QueryServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
    }

    public IQueryServer createLogger(boolean invocationSuccessful, long elapsedTime)
    {
        return new QueryServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }
    
    public String tryToGetQueryDatabaseLabel()
    {
        DatabaseDefinition definition = tryToGetDatabaseDefinition();
        return definition == null ? null : definition.getLabel();
    }

    public TableModel queryDatabase(String sessionToken, String sqlQuery)
    {
        checkSession(sessionToken);
        DatabaseDefinition definition = tryToGetDatabaseDefinition();
        if (definition == null)
        {
            throw new UnsupportedOperationException("Undefined query database");
        }
        DataSource dataSource = definition.getConfigurationContext().getDataSource();
        DataSet<Map<String, Object>> result = QueryTool.select(dataSource, sqlQuery);
        for (Map<String, Object> row : result)
        {
            System.out.println(row);
        }
        result.close();
        List<TableModelColumnHeader> headers = new ArrayList<TableModelColumnHeader>();
        List<TableModelRow> rows = new ArrayList<TableModelRow>();
        return new TableModel(headers, rows);
    }
    
    private DatabaseDefinition tryToGetDatabaseDefinition()
    {
        if (databaseDefinition == null)
        {
            ExtendedProperties databaseProperties =
                    ExtendedProperties.getSubset(configurer.getResolvedProps(), "query-database",
                            true);
            if (databaseProperties.isEmpty() == false)
            {
                DatabaseConfigurationContext configurationContext =
                        BeanUtils
                                .createBean(DatabaseConfigurationContext.class, databaseProperties);
                databaseDefinition =
                        new DatabaseDefinition(databaseProperties.getProperty("label"),
                                configurationContext);
            }
        }
        return databaseDefinition;
    }

}
