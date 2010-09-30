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
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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

    private final static String DATABASE_KEY = "1";

    @Test
    public void testCreateQueries() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE qg =
                createQuery("qg", "generic query", "select * from blabla", true, getSystemPerson(),
                        QueryType.GENERIC, null, DATABASE_KEY);
        queryDAO.createQuery(qg);
        QueryPE qs =
                createQuery("qs", "sample query", "select * from blabla", true, getSystemPerson(),
                        QueryType.SAMPLE, null, DATABASE_KEY);
        queryDAO.createQuery(qs);
        QueryPE qst =
                createQuery("qst", "sample query for CELL_PLATE", "select * from blabla", true,
                        getSystemPerson(), QueryType.SAMPLE, "CELL_PLATE", DATABASE_KEY);
        queryDAO.createQuery(qst);
        QueryPE qe =
                createQuery("qe", "experiment query", "select * from blabla", true,
                        getSystemPerson(), QueryType.EXPERIMENT, null, DATABASE_KEY);
        queryDAO.createQuery(qe);
        QueryPE qet =
                createQuery("qet", "experiment query for SIRNA_HCS", "select * from blabla", true,
                        getSystemPerson(), QueryType.EXPERIMENT, "SIRNA_HCS", DATABASE_KEY);
        queryDAO.createQuery(qet);
        QueryPE qm =
                createQuery("qm", "material query", "select * from blabla", true,
                        getSystemPerson(), QueryType.MATERIAL, null, DATABASE_KEY);
        queryDAO.createQuery(qm);
        QueryPE qmt =
                createQuery("qmt", "material query for BACTERIUM", "select * from blabla", true,
                        getSystemPerson(), QueryType.MATERIAL, "BACTERIUM", DATABASE_KEY);
        queryDAO.createQuery(qmt);
        QueryPE qd =
                createQuery("qd", "data_set query", "select * from blabla", true,
                        getSystemPerson(), QueryType.DATA_SET, null, DATABASE_KEY);
        queryDAO.createQuery(qd);
        QueryPE qdt =
                createQuery("qdt", "data_set query for UNKNOWN", "select * from blabla", true,
                        getSystemPerson(), QueryType.DATA_SET, "UNKNOWN", DATABASE_KEY);
        queryDAO.createQuery(qdt);

        List<QueryPE> queries = queryDAO.listQueries(QueryType.GENERIC);
        assertEquals(1, queries.size());
        assertQueryEquality(qg, queries.get(0));

        queries = queryDAO.listQueries(QueryType.SAMPLE);
        assertEquals(2, queries.size());
        assertQueryEquality(qs, queries.get(0));
        assertQueryEquality(qst, queries.get(1));

        queries = queryDAO.listQueries(QueryType.EXPERIMENT);
        assertEquals(2, queries.size());
        assertQueryEquality(qe, queries.get(0));
        assertQueryEquality(qet, queries.get(1));

        queries = queryDAO.listQueries(QueryType.DATA_SET);
        assertEquals(2, queries.size());
        assertQueryEquality(qd, queries.get(0));
        assertQueryEquality(qdt, queries.get(1));

        queries = queryDAO.listQueries(QueryType.MATERIAL);
        assertEquals(2, queries.size());
        assertQueryEquality(qm, queries.get(0));
        assertQueryEquality(qmt, queries.get(1));
    }

    private void assertQueryEquality(QueryPE expectedQuery, QueryPE actualQuery)
    {
        assertEquals(expectedQuery.getName(), actualQuery.getName());
        assertEquals(expectedQuery.getDescription(), actualQuery.getDescription());
        assertEquals(expectedQuery.getExpression(), actualQuery.getExpression());
        assertEquals(expectedQuery.isPublic(), actualQuery.isPublic());
        assertEquals(expectedQuery.getRegistrator(), actualQuery.getRegistrator());
        assertEquals(expectedQuery.getQueryType(), actualQuery.getQueryType());
        assertEquals(expectedQuery.getEntityTypeCode(), actualQuery.getEntityTypeCode());
        assertEquals(getTestPerson().getDatabaseInstance(), actualQuery.getDatabaseInstance());
    }

    @Test
    public void testCreateGenericQueryFailsWithEntityTypeSpecified() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE query =
                createQuery("qg", "test query", "select * from blabla", true, getSystemPerson(),
                        QueryType.GENERIC, "fake", DATABASE_KEY);
        try
        {
            queryDAO.createQuery(query);
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Query (Name: qg) failed because "
                    + "entity_type has to be null for GENERIC queries.", e.getMessage());
        }
    }

    @Test
    public void testCreateSampleQueryFailsWithFakeEntityType() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE query =
                createQuery("qs", "test query", "select * from blabla", true, getSystemPerson(),
                        QueryType.SAMPLE, "FAKE_SAMPLE", DATABASE_KEY);
        try
        {
            queryDAO.createQuery(query);
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Query (Name: qs) failed because "
                    + "SAMPLE Type (Code: FAKE_SAMPLE) does not exist.", e.getMessage());
        }
    }

    @Test
    public void testCreateMaterialQueryFailsWithFakeEntityType() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE query =
                createQuery("qm", "test query", "select * from blabla", true, getSystemPerson(),
                        QueryType.MATERIAL, "FAKE_MATERIAL", DATABASE_KEY);
        try
        {
            queryDAO.createQuery(query);
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Query (Name: qm) failed because "
                    + "MATERIAL Type (Code: FAKE_MATERIAL) does not exist.", e.getMessage());
        }
    }

    @Test
    public void testCreateDataSetQueryFailsWithFakeEntityType() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE query =
                createQuery("qd", "test query", "select * from blabla", true, getSystemPerson(),
                        QueryType.DATA_SET, "FAKE_DATA_SET", DATABASE_KEY);
        try
        {
            queryDAO.createQuery(query);
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Query (Name: qd) failed because "
                    + "DATA_SET Type (Code: FAKE_DATA_SET) does not exist.", e.getMessage());
        }
    }

    @Test
    public void testCreateExperimentQueryFailsWithFakeEntityType() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE query =
                createQuery("qe", "test query", "select * from blabla", true, getSystemPerson(),
                        QueryType.EXPERIMENT, "FAKE_EXP", DATABASE_KEY);
        try
        {
            queryDAO.createQuery(query);
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Query (Name: qe) failed because "
                    + "EXPERIMENT Type (Code: FAKE_EXP) does not exist.", e.getMessage());
        }
    }

    private QueryPE createQuery(String name, String description, String expression,
            boolean isPublic, PersonPE registrator, QueryType type, String entityTypeCodeOrNull,
            String databaseKey)
    {
        QueryPE query = new QueryPE();
        query.setName(name);
        query.setDescription(description);
        query.setExpression(expression);
        query.setPublic(isPublic);
        query.setRegistrator(registrator);
        query.setQueryType(type);
        query.setEntityTypeCode(entityTypeCodeOrNull);
        query.setQueryDatabaseKey(databaseKey);
        return query;
    }
}
