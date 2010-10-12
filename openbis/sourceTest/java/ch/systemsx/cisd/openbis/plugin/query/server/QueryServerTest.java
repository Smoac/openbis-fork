/*
 * Copyright 2008 ETH Zuerich, CISD
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

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;

/**
 * Test cases for corresponding {@link QueryServer} class.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses = QueryServer.class)
public final class QueryServerTest extends AbstractServerTestCase
{

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private IQueryDatabaseDefinitionProvider dbDefinitionProvider;

    private final IQueryServer createServer()
    {
        return new QueryServer(sessionManager, daoFactory, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin, dbDefinitionProvider);
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        dbDefinitionProvider = context.mock(IQueryDatabaseDefinitionProvider.class);
    }

    @Test
    public void testListQueriesSimple()
    {
        prepareGetSession();

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getQueryDAO();
                    will(returnValue(queryDAO));

                    for (QueryType type : QueryType.values())
                    {
                        one(queryDAO).listQueries(type);
                        will(returnValue(new ArrayList<QueryPE>()));
                    }
                    one(queryDAO).listQueries(null);
                    will(returnValue(new ArrayList<QueryPE>()));
                }
            });

        IQueryServer queryServer = createServer();
        for (QueryType type : QueryType.values())
        {
            queryServer.listQueries(SESSION_TOKEN, type, null);
        }
        queryServer.listQueries(SESSION_TOKEN, null, null);
        context.assertIsSatisfied();
    }

}
