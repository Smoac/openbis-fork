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

package ch.systemsx.cisd.openbis.plugin.query.client.web.server;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientService;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;

/**
 * @author Piotr Buczek
 */
@Component(value = ResourceNames.QUERY_PLUGIN_SERVICE)
public class QueryClientService extends AbstractClientService implements IQueryClientService
{

    @Resource(name = ResourceNames.QUERY_PLUGIN_SERVER)
    private IQueryServer queryServer;

    public QueryClientService()
    {
    }

    @Private
    QueryClientService(final IQueryServer queryServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.queryServer = queryServer;
    }

    @Override
    protected IServer getServer()
    {
        return queryServer;
    }

    //
    // IQueryClientService
    //

    public String tryToGetQueryDatabaseLabel()
    {
        try
        {
            final String sessionToken = getSessionToken();
            return queryServer.tryToGetQueryDatabaseLabel(sessionToken);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public TableModelReference createQueryResultsReport(String sqlQuery)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final TableModel tableModel = queryServer.queryDatabase(sessionToken, sqlQuery);
            String resultSetKey = saveInCache(tableModel.getRows());
            return new TableModelReference(resultSetKey, tableModel.getHeader());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }
}
