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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "db")
public class QueryDAOTest extends AbstractDAOTest
{
    @Test
    public void testCreateQuery() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE query =
                createQuery("q1", "test query", "select * from blabla", true, getSystemPerson(),
                        QueryType.GENERIC);

        queryDAO.createQuery(query);

        List<QueryPE> entities = queryDAO.listQueries();
        assertEquals(1, entities.size());
        assertEquals(query.getName(), entities.get(0).getName());
        assertEquals(query.getDescription(), entities.get(0).getDescription());
        assertEquals(query.getExpression(), entities.get(0).getExpression());
        assertEquals(query.isPublic(), entities.get(0).isPublic());
        assertEquals(query.getRegistrator(), entities.get(0).getRegistrator());
        assertEquals(getTestPerson().getDatabaseInstance(), entities.get(0).getDatabaseInstance());
    }

    private QueryPE createQuery(String name, String description, String expression,
            boolean isPublic, PersonPE registrator, QueryType type)
    {
        QueryPE query = new QueryPE();
        query.setName(name);
        query.setDescription(description);
        query.setExpression(expression);
        query.setPublic(isPublic);
        query.setRegistrator(registrator);
        query.setQueryType(type);
        return query;
    }
}
