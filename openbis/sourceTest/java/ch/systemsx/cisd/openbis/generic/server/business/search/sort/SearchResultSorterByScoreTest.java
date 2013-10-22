package ch.systemsx.cisd.openbis.generic.server.business.search.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;

/**
 * Test Class for SearchResultSorterByScore.
 * 
 * @author pkupczyk
 * @author juanf
 */
public class SearchResultSorterByScoreTest extends AssertJUnit
{

    private class EntitySearchResult implements IEntitySearchResult
    {
        private final String code;

        private final String typeCode;

        private final Map<String, String> properties;

        public EntitySearchResult(
                String code,
                String typeCode,
                Map<String, String> properties)
        {
            this.code = code;
            this.typeCode = typeCode;
            this.properties = properties;
        }

        public String getCode()
        {
            return code;
        }

        public String getTypeCode()
        {
            return typeCode;
        }

        public Map<String, String> getProperties()
        {
            return properties;
        }

    }

    @Test
    public void testCode()
    {
        //
        // Entities Setup
        //
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createSample("CODE_2", "TYPE_2"));
        entities.add(createSample("CODE_1", "TYPE_1"));
        entities.add(createSample("CODE_3", "TYPE_3"));
        entities.add(createSample("CODE", "TYPE"));

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        List<DetailedSearchCriterion> criterions = new ArrayList<DetailedSearchCriterion>();
        criteria.setCriteria(criterions);

        // Test hit only code
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "CODE"));

        //
        // Run algorithm
        //
        SearchResultSorterByScore sorter = new SearchResultSorterByScore();
        sorter.sort(entities, criteria);

        //
        // Verify results
        //
        assertEquals("CODE", entities.get(0).getCode());
        assertEquals("CODE_1", entities.get(1).getCode());
        assertEquals("CODE_2", entities.get(2).getCode());
        assertEquals("CODE_3", entities.get(3).getCode());
    }

    @Test
    public void testTypeCode()
    {
        //
        // Entities Setup
        //
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createSample("CODE_2", "TYPE_2"));
        entities.add(createSample("CODE_1", "TYPE_1"));
        entities.add(createSample("CODE_3", "TYPE_3"));
        entities.add(createSample("CODE", "TYPE"));

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        List<DetailedSearchCriterion> criterions = new ArrayList<DetailedSearchCriterion>();
        criteria.setCriteria(criterions);

        // Test hit only code
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "TYPE"));

        //
        // Run algorithm
        //
        SearchResultSorterByScore sorter = new SearchResultSorterByScore();
        sorter.sort(entities, criteria);

        //
        // Verify results
        //
        assertEquals("CODE", entities.get(0).getCode());
        assertEquals("CODE_1", entities.get(1).getCode());
        assertEquals("CODE_2", entities.get(2).getCode());
        assertEquals("CODE_3", entities.get(3).getCode());
    }

    @Test
    public void testProperties()
    {
        //
        // Entities Setup
        //
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createSample("CODE_2", "TYPE_2", "ABC_1", "ABC_2"));
        entities.add(createSample("CODE_1", "TYPE_1", "ABC_1"));
        entities.add(createSample("CODE_3", "TYPE_3", "ABC_1", "ABC_2", "ABC_3"));
        entities.add(createSample("CODE_4", "TYPE_4", "ABC"));

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        List<DetailedSearchCriterion> criterions = new ArrayList<DetailedSearchCriterion>();
        criteria.setCriteria(criterions);

        // Test hit only properties, partial and exact
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "ABC"));

        //
        // Run algorithm
        //
        SearchResultSorterByScore sorter = new SearchResultSorterByScore();
        sorter.sort(entities, criteria);

        //
        // Verify results
        //
        assertEquals("CODE_4", entities.get(0).getCode());
        assertEquals("CODE_3", entities.get(1).getCode());
        assertEquals("CODE_2", entities.get(2).getCode());
        assertEquals("CODE_1", entities.get(3).getCode());
    }

    @Test
    public void testAll()
    {
        //
        // Entities Setup
        //
        List<EntitySearchResult> entities = new ArrayList<EntitySearchResult>();
        entities.add(createSample("CODE_2", "TYPE_2", "ABC_1", "ABC_2"));
        entities.add(createSample("CODE_1", "TYPE_1", "ABC_1"));
        entities.add(createSample("CODE_3", "TYPE_3", "ABC_1", "ABC_2", "ABC_3"));
        entities.add(createSample("CODE_4", "TYPE_4", "TYU"));
        entities.add(createSample("CODE_5", "TYPE_5", "XYZ"));
        entities.add(createSample("CODE_6", "TYPE_6", "666"));

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();

        List<DetailedSearchCriterion> criterions = new ArrayList<DetailedSearchCriterion>();
        // Test hit only type
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "TYPE_6"));
        // Test hit only properties
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "ABC"));
        // Test hit only code
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "CODE_4"));
        // Test hit property and code
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "CODE_5"));
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "XYZ"));
        criteria.setCriteria(criterions);

        //
        // Run algorithm
        //
        SearchResultSorterByScore sorter = new SearchResultSorterByScore();
        sorter.sort(entities, criteria);

        //
        // Verify results
        //
        assertEquals("CODE_5", entities.get(0).getCode());
        assertEquals("CODE_4", entities.get(1).getCode());
        assertEquals("CODE_6", entities.get(2).getCode());
        assertEquals("CODE_3", entities.get(3).getCode());
        assertEquals("CODE_2", entities.get(4).getCode());
        assertEquals("CODE_1", entities.get(5).getCode());
    }

    private EntitySearchResult createSample(String code, String typeCode, String... propertyValues)
    {
        Map<String, String> properties = new HashMap<String, String>();
        int propertyIndex = 1;
        for (String propertyValue : propertyValues)
        {
            properties.put("PROP_" + propertyIndex, propertyValue);
            propertyIndex++;
        }

        return new EntitySearchResult(code, typeCode, properties);
    }
}
