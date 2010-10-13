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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hibernate.classic.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexerRunnable;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

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

    private final static String LUCENE_INDEX_TEMPLATE_PATH = "./sourceTest/lucene/indices";

    @BeforeClass
    public void setUpIndex()
    {
        restoreSearchIndex();
    }

    // create a fresh copy of the Lucene index
    private static void restoreSearchIndex()
    {
        File targetPath = new File(TestInitializer.LUCENE_INDEX_PATH);
        FileUtilities.deleteRecursively(targetPath);
        targetPath.mkdirs();
        File srcPath = new File(LUCENE_INDEX_TEMPLATE_PATH);
        try
        {
            FileUtils.copyDirectory(srcPath, targetPath);
            new File(srcPath, FullTextIndexerRunnable.FULL_TEXT_INDEX_MARKER_FILENAME)
                    .createNewFile();
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "registratorTerm")
    private final static Object[][] getRegistratorTerm()
    {
        return new Object[][]
            {
                { "john" },
                { "Jo?n" },
                { "*ohn" } };
    }

    @Test
    public final void testSearchEntitiesByTermFailed()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        boolean fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(null, null, createDataProvider(), true, 0);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(SearchableEntity.MATERIAL, "",
                    createDataProvider(), true, 0);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    private final HibernateSearchDataProvider createDataProvider()
    {
        return new HibernateSearchDataProvider(daoFactory);
    }

    @Test(dataProvider = "registratorTerm")
    public final void testSearchEntitiesByRegistrator(final String term)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final String lastName = "John";
        final List<MatchingEntity> hits =
                hibernateSearchDAO.searchEntitiesByTerm(SearchableEntity.SAMPLE, term,
                        createDataProvider(), false, 0);
        assertTrue(hits.size() > 0);
        for (MatchingEntity matchingEntity : hits)
        {
            assertEquals(lastName, matchingEntity.getRegistrator().getFirstName());
            AssertionUtil.assertContains("registrator First Name",
                    matchingEntity.getFieldDescription());
        }
    }

    @DataProvider(name = "experimentQueriestAndModeToTest")
    protected Object[][] getExperimentQueriesAndModeToTest()
    {
        return new Object[][]
            {
                { "exp-*", "exp-", true },
                { "exp-", "exp-", false } };
    }

    @Test(dataProvider = "experimentQueriestAndModeToTest")
    public final void testSearchEntitiesByTermForExperiment(String query, String querySubstring,
            boolean useWildcardMode)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final List<MatchingEntity> hits =
                hibernateSearchDAO.searchEntitiesByTerm(SearchableEntity.EXPERIMENT, query,
                        createDataProvider(), useWildcardMode, 0);
        assertEquals(5, hits.size());
        for (MatchingEntity matchingEntity : hits)
        {
            AssertionUtil.assertContainsInsensitive(querySubstring, matchingEntity.getCode());
            assertEquals("code", matchingEntity.getFieldDescription());
        }
    }

    @Test
    public final void testSearchEntitiesByTermForMaterial()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        String propertyValue = "adenovirus";
        final List<MatchingEntity> hits =
                hibernateSearchDAO.searchEntitiesByTerm(SearchableEntity.MATERIAL, propertyValue,
                        createDataProvider(), true, 0);
        assertEquals(2, hits.size());
        for (MatchingEntity matchingEntity : hits)
        {
            MaterialPE material =
                    daoFactory.getMaterialDAO().getByTechId(new TechId(matchingEntity.getId()));
            ensureContains(material.getProperties(), propertyValue);
        }
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

    private static DetailedSearchCriteria createOrDatasetQuery(DetailedSearchCriterion... criteria)
    {
        return createDatasetQuery(SearchCriteriaConnection.MATCH_ANY, criteria);
    }

    private static DetailedSearchCriteria createAndDatasetQuery(DetailedSearchCriterion... criteria)
    {
        return createDatasetQuery(SearchCriteriaConnection.MATCH_ALL, criteria);
    }

    private static DetailedSearchCriteria createDatasetQuery(SearchCriteriaConnection connection,
            DetailedSearchCriterion[] criteria)
    {
        DetailedSearchCriteria result = new DetailedSearchCriteria();
        result.setConnection(connection);
        result.setCriteria(Arrays.asList(criteria));
        return result;
    }

    private static DetailedSearchCriterion mkCriterion(DetailedSearchField field, String value)
    {
        return new DetailedSearchCriterion(field, value);
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

    /**
     * Returns a list of datasets mathing given criteria.<br>
     * <br>
     * Only eager connections are loaded.
     */
    private List<ExternalDataPE> searchForDatasets(DetailedSearchCriteria criteria)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        List<Long> datasetIds =
                hibernateSearchDAO.searchForEntityIds(criteria,
                        DtoConverters.convertEntityKind(EntityKind.DATA_SET));
        final List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        for (Long datasetId : datasetIds)
        {
            result.add(getDataSetById(datasetId));
        }
        return result;
    }

    // NOTE: depends on proper implementation of ExternalDataDAO.
    //
    // HibernateSearhDAO returns only dataset ids, which are too strongly connected with database
    // content and should not be directly tested in asserts. Using ExternalDataDAO we can check
    // if dataset with given id is in fact in the DB and use dataset attributes in asserts instead
    // of using ids.
    private ExternalDataPE getDataSetById(Long datasetId)
    {
        return daoFactory.getExternalDataDAO().getByTechId(new TechId(datasetId));
    }

    // NOTE: such a check depends strongly on the test database content. Use it only when the better
    // way to check the results is much harder.
    private void assertCorrectDatasetsFound(DetailedSearchCriteria criteria,
            DSLoc... expectedLocations)
    {
        List<ExternalDataPE> dataSets = searchForDatasets(criteria);
        AssertJUnit.assertEquals(expectedLocations.length, dataSets.size());
        for (ExternalDataPE dataSet : dataSets)
        {
            assertContains(expectedLocations, dataSet.getLocation());
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
        LOC1("a/3"), LOC2("a/2"), LOC3("a/1"), LOC4("xxx/yyy/zzz"), LOC5("analysis/result"), LOC6(
                "xml/result-12");

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

    private static DetailedSearchField createAnySearchField(List<String> propertyTypes)
    {
        return DetailedSearchField.createAnyField(propertyTypes);
    }

    private DetailedSearchCriterion createAnyFieldCriterion()
    {
        List<String> propertyTypes = fetchPropertyTypeCodes();
        return mkCriterion(createAnySearchField(propertyTypes), "*3*");
    }

    private DetailedSearchCriterion createSimpleFieldCriterion()
    {
        return mkCriterion(
                DetailedSearchField.createAttributeField(DataSetAttributeSearchFieldKind.FILE_TYPE),
                "TIFF");
    }

    @Test
    public final void testSearchForDataSetsAnyField()
    {
        DetailedSearchCriterion criterion = createAnyFieldCriterion();
        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC4, DSLoc.LOC5,
                DSLoc.LOC6);
    }

    @Test
    public final void testSearchForDataSetsSimpleField()
    {
        DetailedSearchCriterion criterion = createSimpleFieldCriterion();
        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC3, DSLoc.LOC4);
    }

    @Test
    public final void testSearchForDataSetsComplexAndQuery()
    {
        DetailedSearchCriterion criterion1 = createAnyFieldCriterion();
        DetailedSearchCriterion criterion2 = createSimpleFieldCriterion();
        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion1, criterion2);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC4);
    }

    @Test
    public final void testSearchForDataSetsComplexOrQuery()
    {
        DetailedSearchCriterion criterion1 = createAnyFieldCriterion();
        DetailedSearchCriterion criterion2 = createSimpleFieldCriterion();
        DetailedSearchCriteria criteria = createOrDatasetQuery(criterion1, criterion2);
        assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC3, DSLoc.LOC4,
                DSLoc.LOC5, DSLoc.LOC6);
    }

    // @Test
    // public final void testSearchForDataSetsComplexGeneric()
    // {
    // List<String> propertyTypes = fetchPropertyTypeCodes();
    // DataSetSearchCriterion criterion1 =
    // mkCriterion(createAnySearchField(propertyTypes), "3VCP1");
    // String propertyValue = "\"simple experiment\"";
    // DataSetSearchCriterion criterion2 =
    // mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes),
    // propertyValue);
    // DataSetSearchCriterion criterion3 =
    // mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
    // "NEMO");
    // DataSetSearchCriterion criterion4 =
    // mkCriterion(DataSetSearchField.createExperimentProperty("GENDER"), "MALE");
    //
    // DataSetSearchCriteria criteria =
    // createAndDatasetQuery(criterion1, criterion2, criterion3, criterion4);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // TODO 2009-08-17, Piotr Buczek: write tests for related data sets with similar use cases

    // @Test
    // public final void testSearchForDataSetsSimpleField()
    // {
    // DataSetSearchCriterion criterion =
    // mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
    // "NEMO");
    // DataSetSearchCriteria criteria = createAndDatasetQuery(criterion);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC3, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // @Test
    // public final void testSearchForDataSetsSpecificSampleProperty()
    // {
    // String propertyValue = "stuff";
    // DataSetSearchCriterion criterion =
    // mkCriterion(DataSetSearchField.createSampleProperty("COMMENT"), propertyValue);
    // DataSetSearchCriteria criteria = createAndDatasetQuery(criterion);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC3);
    // }

    // @Test(groups = "broken")
    // public final void testSearchForDataSetsAnyProperty()
    // {
    // List<String> propertyTypes = fetchPropertyTypeCodes();
    // DataSetSearchCriterion criterion1 =
    // mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes), "male");
    // DataSetSearchCriterion criterion2 =
    // mkCriterion(DataSetSearchField.createAnySampleProperty(propertyTypes), "fly");
    //
    // DataSetSearchCriteria criteria = createOrDatasetQuery(criterion1, criterion2);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // @Test
    // public final void testSearchForDataSetsComplexGeneric()
    // {
    // List<String> propertyTypes = fetchPropertyTypeCodes();
    // DataSetSearchCriterion criterion1 =
    // mkCriterion(createAnySearchField(propertyTypes), "3VCP1");
    // String propertyValue = "\"simple experiment\"";
    // DataSetSearchCriterion criterion2 =
    // mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes),
    // propertyValue);
    // DataSetSearchCriterion criterion3 =
    // mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
    // "NEMO");
    // DataSetSearchCriterion criterion4 =
    // mkCriterion(DataSetSearchField.createExperimentProperty("GENDER"), "MALE");
    //
    // DataSetSearchCriteria criteria =
    // createAndDatasetQuery(criterion1, criterion2, criterion3, criterion4);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // @Test
    // /*
    // * Checks if the dataset search index is properly updated after properties for a connected
    // * sample have changed.
    // */
    // public final void testSearchForDataSetsAfterSamplePropertiesUpdate()
    // throws InterruptedException
    // {
    // String propertyCode = "COMMENT";
    // DataSetSearchCriterion criterion =
    // mkCriterion(DataSetSearchField.createSampleProperty(propertyCode), "stuff");
    //
    // DataSetSearchCriteria criteria = createAndDatasetQuery(criterion);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC3);
    //
    // SamplePE sample = findSample("CP-TEST-3", "CISD");
    //
    // String newValue = "Bonanza";
    // changeSampleProperty(sample, propertyCode, newValue);
    //
    // flushSearchIndices();
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC2, DSLoc.LOC3);
    // restoreSearchIndex();
    // }

    @Test
    /*
     * Checks if the dataset search index is properly updated after properties of a dataset have
     * changed.
     */
    public final void testSearchForDataSetsAfterPropertiesUpdate() throws InterruptedException
    {
        String propertyCode = "COMMENT";
        DetailedSearchCriterion criterion1 =
                mkCriterion(DetailedSearchField.createPropertyField(propertyCode), "no comment");
        DetailedSearchCriterion criterion2 = createSimpleFieldCriterion();

        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion1, criterion2);

        assertCorrectDatasetsFound(criteria, DSLoc.LOC3, DSLoc.LOC4);

        // This data set has "no comment" value as a COMMENT property and TIFF file type.
        // We change it and check if it is removed from results.
        ExternalDataPE externalData = findExternalData("20081105092159111-1"); // LOC3
        String newValue = "sth";
        changeExternalDataProperty(externalData, propertyCode, newValue);
        flushSearchIndices();
        assertCorrectDatasetsFound(criteria, DSLoc.LOC4);
        restoreSearchIndex();
    }

    private void flushSearchIndices()
    {
        Session currentSession = sessionFactory.getCurrentSession();
        FullTextSession fullTextSession = Search.getFullTextSession(currentSession);
        fullTextSession.flushToIndexes();
    }

    private void flushSession()
    {
        sessionFactory.getCurrentSession().flush();
    }

    private void changeExternalDataProperty(ExternalDataPE externalData, String propertyCode,
            String newValue)
    {
        EntityPropertyPE property = findProperty(externalData, propertyCode);

        removeProperty(externalData, property);
        flushSession();

        DataSetPropertyPE newProperty = new DataSetPropertyPE();
        copyPropertyWithNewValue(newValue, property, newProperty);
        addProperty(externalData, newProperty);
        flushSession();
    }

    private void copyPropertyWithNewValue(String newValue, EntityPropertyPE oldProperty,
            EntityPropertyPE newProperty)
    {
        newProperty.setEntityTypePropertyType(oldProperty.getEntityTypePropertyType());
        newProperty.setRegistrator(oldProperty.getRegistrator());
        newProperty.setValue(newValue);
    }

    private static <T extends EntityPropertyPE> void addProperty(
            IEntityPropertiesHolder propertiesHolder, T newProperty)
    {
        Set<EntityPropertyPE> properties = getCopiedProperties(propertiesHolder);
        properties.add(newProperty);
        propertiesHolder.setProperties(properties);
    }

    private static Set<EntityPropertyPE> removeProperty(IEntityPropertiesHolder propertiesHolder,
            EntityPropertyPE property)
    {
        Set<EntityPropertyPE> properties = getCopiedProperties(propertiesHolder);
        boolean removed = properties.remove(property);
        assert removed : "property could not be removed";
        propertiesHolder.setProperties(properties);
        return properties;
    }

    private static Set<EntityPropertyPE> getCopiedProperties(
            IEntityPropertiesHolder propertiesHolder)
    {
        return new HashSet<EntityPropertyPE>(propertiesHolder.getProperties());
    }

    private static EntityPropertyPE findProperty(IEntityPropertiesHolder propertiesHolder,
            String propertyCode)
    {
        for (EntityPropertyPE prop : propertiesHolder.getProperties())
        {
            if (prop.getEntityTypePropertyType().getPropertyType().getCode().equals(propertyCode))
            {
                return prop;
            }
        }
        fail("property not found: " + propertyCode);
        return null; // never happens
    }

}
