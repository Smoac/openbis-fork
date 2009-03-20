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
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetSearchHitDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
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
        assertEquals(5, hits.size());
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
        String query = LuceneQueryBuilder.disableFieldQuery(unescapedQuery);
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

    // ----------------- test serach for datasets

    private static DataSetSearchCriteria createOrDatasetQuery(DataSetSearchCriterion... criteria)
    {
        return createDatasetQuery(SearchCriteriaConnection.MATCH_ANY, criteria);
    }

    private static DataSetSearchCriteria createAndDatasetQuery(DataSetSearchCriterion... criteria)
    {
        return createDatasetQuery(SearchCriteriaConnection.MATCH_ALL, criteria);
    }

    private static DataSetSearchCriteria createDatasetQuery(SearchCriteriaConnection connection,
            DataSetSearchCriterion[] criteria)
    {
        DataSetSearchCriteria result = new DataSetSearchCriteria();
        result.setConnection(connection);
        result.setCriteria(Arrays.asList(criteria));
        return result;
    }

    private static DataSetSearchCriterion mkCriterion(DataSetSearchField field, String value)
    {
        return new DataSetSearchCriterion(field, value);
    }

    private List<String> fetchPropertyTypeCodes()
    {
        List<PropertyTypePE> propertyTypes = daoFactory.getPropertyTypeDAO().listAllPropertyTypes();
        List<String> codes = new ArrayList<String>();
        for (PropertyTypePE prop : propertyTypes)
        {
            codes.add(prop.getCode());
        }
        return codes;
    }

    private List<DataSetSearchHitDTO> searchForDatasets(DataSetSearchCriteria criteria)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        return hibernateSearchDAO.searchForDataSets(criteria);
    }

    // NOTE: such a check depends strongly on the test database content. Use it only when the better
    // way to check the results is much harder.
    private void assertCorrectDatasetsFound(DataSetSearchCriteria criteria, DSLoc... expectedLocations)
    {
        List<DataSetSearchHitDTO> dataSets = searchForDatasets(criteria);
        AssertJUnit.assertEquals(expectedLocations.length, dataSets.size());
        for (DataSetSearchHitDTO dataSet : dataSets)
        {
            assertContains(expectedLocations, dataSet.getDataSet().getLocation());
        }
    }

    private static void assertContains(DSLoc[] expectedLocations, String location)
    {
        for (DSLoc loc : expectedLocations)
        {
            if (loc.getLocation().equals(location))
            {
                return;
            }
        }
        fail("Dataset location " + location + " not found in the search result");
    }

    // enumerates existing dataset locations in the database
    private static enum DSLoc
    {
        LOC1("a/3"), LOC2("a/2"), LOC3("a/1"), LOC4("xxx/yyy/zzz"), LOC5("analysis/result");

        private final String location;

        private DSLoc(String location)
        {
            this.location = location;
        }

        public String getLocation()
        {
            return location;
        }
    }

    private static DataSetSearchField createAnySearchField(List<String> propertyTypes)
    {
        return DataSetSearchField.createAnyField(propertyTypes, propertyTypes);
    }

    @Test
    public final void testSearchForDataSetsAnyField()
    {
        List<String> propertyTypes = fetchPropertyTypeCodes();
        DataSetSearchCriterion criterion1 =
                mkCriterion(createAnySearchField(propertyTypes), "stuff");
        DataSetSearchCriteria criteria = createAndDatasetQuery(criterion1);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC3);
    }

    @Test
    public final void testSearchForDataSetsAnyProperty()
    {
        List<String> propertyTypes = fetchPropertyTypeCodes();
        DataSetSearchCriterion criterion1 =
                mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes), "male");
        DataSetSearchCriterion criterion2 =
                mkCriterion(DataSetSearchField.createAnySampleProperty(propertyTypes), "fly");

        DataSetSearchCriteria criteria = createOrDatasetQuery(criterion1, criterion2);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC4, DSLoc.LOC5);
    }

    @Test
    public final void testSearchForDataSetsComplexGeneric()
    {
        List<String> propertyTypes = fetchPropertyTypeCodes();
        DataSetSearchCriterion criterion1 =
                mkCriterion(createAnySearchField(propertyTypes), "3VCP1");
        String propertyValue = "\"simple experiment\"";
        DataSetSearchCriterion criterion2 =
                mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes),
                        propertyValue);
        DataSetSearchCriterion criterion3 =
                mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
                        "NEMO");
        DataSetSearchCriterion criterion4 =
                mkCriterion(DataSetSearchField.createExperimentProperty("USER.GENDER"), "MALE");

        DataSetSearchCriteria criteria =
                createAndDatasetQuery(criterion1, criterion2, criterion3, criterion4);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC4, DSLoc.LOC5);
    }
}
