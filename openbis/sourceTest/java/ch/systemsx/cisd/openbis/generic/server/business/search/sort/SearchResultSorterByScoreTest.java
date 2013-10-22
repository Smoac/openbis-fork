package ch.systemsx.cisd.openbis.generic.server.business.search.sort;

import static ch.systemsx.cisd.openbis.generic.server.business.search.sort.SearchResultSorterTestHelper.assertEntities;
import static ch.systemsx.cisd.openbis.generic.server.business.search.sort.SearchResultSorterTestHelper.createEntity;
import static ch.systemsx.cisd.openbis.generic.server.business.search.sort.SearchResultSorterTestHelper.getAnyFieldCriterion;
import static ch.systemsx.cisd.openbis.generic.server.business.search.sort.SearchResultSorterTestHelper.sort;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.search.sort.SearchResultSorterTestHelper.EntitySearchResult;

/**
 * Test Class for SearchResultSorterByScore.
 * 
 * @author pkupczyk
 * @author juanf
 */
public class SearchResultSorterByScoreTest extends AssertJUnit
{

    @Test
    public void testCriteriaAnyCode()
    {
        // Entities Setup
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createEntity("CODE_2", "TYPE_2"));
        entities.add(createEntity("CODE_1", "TYPE_1"));
        entities.add(createEntity("CODE_3", "TYPE_3"));
        entities.add(createEntity("CODE", "TYPE"));

        // Test hit only code
        sort(entities, getAnyFieldCriterion("CODE"));

        // Verify results
        assertEntities(entities, "CODE", "CODE_1", "CODE_2", "CODE_3");
    }

    @Test
    public void testCriteriaAnyTypeCode()
    {
        // Entities Setup
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createEntity("CODE_2", "TYPE_2"));
        entities.add(createEntity("CODE_1", "TYPE_1"));
        entities.add(createEntity("CODE_3", "TYPE_3"));
        entities.add(createEntity("CODE", "TYPE"));

        // Test hit only type code
        sort(entities, getAnyFieldCriterion("TYPE"));

        // Verify results
        assertEntities(entities, "CODE", "CODE_1", "CODE_2", "CODE_3");
    }

    @Test
    public void testCriteriaAnyProperties()
    {
        // Entities Setup
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createEntity("CODE_2", "TYPE_2", "ABC_1", "ABC_2"));
        entities.add(createEntity("CODE_1", "TYPE_1", "ABC_1"));
        entities.add(createEntity("CODE_3", "TYPE_3", "ABC_1", "ABC_2", "ABC_3"));
        entities.add(createEntity("CODE_4", "TYPE_4", "ABC"));

        // Test hit only properties, partial and exact
        sort(entities, getAnyFieldCriterion("ABC"));

        // Verify results
        assertEntities(entities, "CODE_4", "CODE_3", "CODE_2", "CODE_1");
    }

    @Test
    public void testCriteriaAnyAll()
    {
        // Entities Setup
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createEntity("CODE_2", "TYPE_2", "ABC_1", "ABC_2"));
        entities.add(createEntity("CODE_1", "TYPE_1", "ABC_1"));
        entities.add(createEntity("CODE_3", "TYPE_3", "ABC_1", "ABC_2", "ABC_3"));
        entities.add(createEntity("CODE_4", "TYPE_4", "TYU"));
        entities.add(createEntity("CODE_5", "TYPE_5", "XYZ"));
        entities.add(createEntity("CODE_6", "TYPE_6", "666"));

        sort(entities,
                getAnyFieldCriterion("TYPE_6"), // Test hit only type
                getAnyFieldCriterion("ABC"), // Test hit only properties
                getAnyFieldCriterion("CODE_4"), // Test hit only code
                getAnyFieldCriterion("CODE_5"), // Test hit property and code
                getAnyFieldCriterion("XYZ"));

        // Verify results
        assertEntities(entities, "CODE_5", "CODE_4", "CODE_6", "CODE_3", "CODE_2", "CODE_1");
    }
}
