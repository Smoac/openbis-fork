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

package ch.systemsx.cisd.openbis.systemtest.plugin.query;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class QueryEditingTest extends QuerySystemTestCase
{
    
    @Test
    public void testRegisterEditAndDeleteQueryDefinition()
    {
        logIntoCommonClientService();
        assertEquals(0, queryClientService.listQueries().size());
        
        NewExpression query = createQuery("query1", "select * from sample_types", true);
        queryClientService.registerQuery(query);
        
        List<QueryExpression> queries = queryClientService.listQueries();
        assertEquals(1, queries.size());
        assertQuery(query, queries.get(0));
        QueryExpression actualQuery = queries.get(0);
        assertQuery(query, actualQuery);

        actualQuery.setDescription("hello");
        actualQuery.setName("new query1");
        actualQuery.setExpression("select * from something");
        actualQuery.setPublic(false);
        queryClientService.updateQuery(actualQuery);
        
        queries = queryClientService.listQueries();
        assertEquals(1, queries.size());
        assertEquals(actualQuery.getName(), queries.get(0).getName());
        assertEquals(actualQuery.getDescription(), queries.get(0).getDescription());
        assertEquals(actualQuery.getExpression(), queries.get(0).getExpression());
        assertEquals(actualQuery.isPublic(), queries.get(0).isPublic());
        
        queryClientService.deleteQueries(Arrays.asList(new TechId(queries.get(0).getId())));
        
        assertEquals(0, queryClientService.listQueries().size());
    }
    
    @Test(groups = "broken")
    public void testCreateQueryResult()
    {
        logIntoCommonClientService();

        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("id", "1");
        TableModelReference table =
                queryClientService.createQueryResultsReport(
                        "select * from sample_types where id = ${id}", bindings);
    }
    
    private NewExpression createQuery(String name, String expression, boolean isPublic)
    {
        NewExpression query = new NewExpression();
        query.setName(name);
        query.setDescription("A simple query named '" + name + "'.");
        query.setExpression(expression);
        query.setPublic(isPublic);
        return query;
    }
    
    private void assertQuery(NewExpression expectedQuery, QueryExpression actualQuery)
    {
        assertEquals(expectedQuery.getName(), actualQuery.getName());
        assertEquals(expectedQuery.getDescription(), actualQuery.getDescription());
        assertEquals(expectedQuery.getExpression(), actualQuery.getExpression());
        assertEquals(expectedQuery.isPublic(), actualQuery.isPublic());
    }
}
