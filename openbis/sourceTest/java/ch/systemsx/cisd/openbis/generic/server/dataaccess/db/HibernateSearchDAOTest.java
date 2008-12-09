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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;

/**
 * Test cases for corresponding {@link HibernateSearchDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "hibernateSearch" })
@Friend(toClasses = HibernateSearchDAO.class)
public final class HibernateSearchDAOTest extends AbstractDAOTest
{
    @SuppressWarnings("unused")
    @DataProvider(name = "registratorTerm")
    private final static Object[][] getRegistratorTerm()
    {
        return new Object[][]
            {
                { "Doe" },
                { "d?e" },
                { "*oe" } };
    }

    @Test
    public final void testSearchEntitiesByTermFailed()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        boolean fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(MaterialPE.class, "");
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test(dataProvider = "registratorTerm")
    public final void testSearchEntitiesByRegistrator(final String term)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final String lastName = "Doe";
        final List<SearchHit> hits = hibernateSearchDAO.searchEntitiesByTerm(SamplePE.class, term);
        assertTrue(hits.size() > 0);
        for (SearchHit searchHit : hits)
        {
            SamplePE samplePE = ((SamplePE) searchHit.getEntity());
            assertEquals(lastName, samplePE.getRegistrator().getLastName());
            assertEquals("registrator: Last Name", searchHit.getFieldDescription());
        }
    }

    @Test
    public final void testSearchEntitiesByTermForExperiment()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        String query = "exp";
        final List<SearchHit> hits =
                hibernateSearchDAO.searchEntitiesByTerm(ExperimentPE.class, query);
        assertEquals(2, hits.size());
        for (SearchHit searchHit : hits)
        {
            ExperimentPE entity = ((ExperimentPE) searchHit.getEntity());
            AssertionUtil.assertContainsInsensitive(query, entity.getCode());
            assertEquals("code", searchHit.getFieldDescription());
        }
    }

    @Test
    public final void testSearchEntitiesByTermForMaterial()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        String propertyValue = "adenovirus";
        final List<SearchHit> hits =
                hibernateSearchDAO.searchEntitiesByTerm(MaterialPE.class, propertyValue);
        assertEquals(2, hits.size());
        // { "materialProperties.value", "materialProperties.vocabularyTerm.code" },
        for (SearchHit searchHit : hits)
        {
            MaterialPE material = (MaterialPE) searchHit.getEntity();
            ensureContains(material.getProperties(), propertyValue);
        }
    }

    @DataProvider(name = "queryEscaping")
    protected Object[][] getQueriesToTest()
    {
        return new Object[][]
            {
                { "abc", "abc" },
                { "code:CP registrator:Joe", "code\\:CP registrator\\:Joe" },
                { "::", "\\:\\:" } };
    }

    @Test(dataProvider = "queryEscaping")
    public final void testDisableAdvancedSearch(String unescapedQuery, String escapedQuery)
    {
        String query = HibernateSearchDAO.disableFieldQuery(unescapedQuery);
        assertEquals(escapedQuery, query);
    }

    private static void ensureContains(Set<MaterialPropertyPE> properties, String propertyValue)
    {
        boolean ok = false;
        for (MaterialPropertyPE prop : properties)
        {
            ok = ok || containsInsensitve(prop.tryGetUntypedValue(), propertyValue);
        }
        assertTrue("No property contains text " + propertyValue, ok);
    }

    private static boolean containsInsensitve(String text, String substring)
    {
        return text.toUpperCase().contains(substring.toUpperCase());
    }
}
