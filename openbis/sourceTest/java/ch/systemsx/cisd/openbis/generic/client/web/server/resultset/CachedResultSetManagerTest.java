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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager.TokenBasedResultSetKeyGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CachedResultSetManager.ICustomColumnsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRendererTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * Test cases for corresponding {@link CachedResultSetManager} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = CachedResultSetManager.class)
public final class CachedResultSetManagerTest
{
    static private final String SESSION_TOKEN = "SESSION_TOKEN";

    private IResultSetConfig<String, String> resultSetConfig;

    private IOriginalDataProvider<String> originalDataProvider;

    private IResultSetManager<String> resultSetManager;

    private Mockery context;

    private final void allowResultSetCreation(final Expectations expectations)
    {
        final SortInfo<String> sortInfo = new SortInfo<String>();
        sortInfo.setSortDir(SortDir.NONE);
        sortInfo.setSortField(null);

        expectations.one(resultSetConfig).getOffset();
        expectations.will(Expectations.returnValue(1));

        expectations.one(resultSetConfig).getLimit();
        expectations.will(Expectations.returnValue(2));

        expectations.one(resultSetConfig).getSortInfo();
        expectations.will(Expectations.returnValue(sortInfo));

        expectations.allowing(resultSetConfig).getFilters();
        GridFilters<String> filters = GridFilters.createEmptyFilter();
        expectations.will(Expectations.returnValue(filters));
    }

    private final static GridRowModels<Sample> createSampleList()
    {
        final int size = 3;
        final List<Sample> list = new ArrayList<Sample>(size);
        for (int i = 0; i < size; i++)
        {
            final Sample sample = new Sample();
            sample.setCode("code" + i);
            list.add(sample);
        }
        return createGridRowModels(list);
    }

    public static <T> GridRowModels<T> createGridRowModels(List<T> entities)
    {
        ArrayList<GridCustomColumnInfo> customColumnsMetadata =
                new ArrayList<GridCustomColumnInfo>();
        ArrayList<ColumnDistinctValues> columnDistinctValues =
                new ArrayList<ColumnDistinctValues>();
        GridRowModels<T> rowModels =
                new GridRowModels<T>(TSVRendererTest.asRowModel(entities), customColumnsMetadata,
                        columnDistinctValues);
        return rowModels;
    }

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        resultSetConfig = context.mock(IResultSetConfig.class);
        originalDataProvider = context.mock(IOriginalDataProvider.class);
        resultSetManager = createResultSetManager();
    }

    private final static IResultSetManager<String> createResultSetManager()
    {
        final CachedResultSetManager<String> resultSetManager =
                new CachedResultSetManager<String>(new TokenBasedResultSetKeyGenerator(),
                        new ICustomColumnsProvider()
                            {
                                public List<GridCustomColumn> getGridCustomColumn(
                                        String sessionToken, String gridDisplayId)
                                {
                                    return new ArrayList<GridCustomColumn>();
                                }

                            });
        resultSetManager.results.put("1", createSampleList());
        return resultSetManager;
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetResultFailed()
    {
        boolean fail = true;
        try
        {
            resultSetManager.getResultSet(null, null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetLimit()
    {
        assertEquals(0, CachedResultSetManager.getLimit(0, -1, 0));
        assertEquals(1, CachedResultSetManager.getLimit(1, -1, 0));
        assertEquals(2, CachedResultSetManager.getLimit(2, -1, 0));
        assertEquals(1, CachedResultSetManager.getLimit(2, -1, 1));
        assertEquals(0, CachedResultSetManager.getLimit(2, 0, 1));
        assertEquals(1, CachedResultSetManager.getLimit(2, 1, 1));
        assertEquals(1, CachedResultSetManager.getLimit(2, 2, 1));
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetOffset()
    {
        assertEquals(0, CachedResultSetManager.getOffset(0, 1));
        assertEquals(0, CachedResultSetManager.getOffset(1, 1));
        assertEquals(1, CachedResultSetManager.getOffset(2, 1));
        assertEquals(0, CachedResultSetManager.getOffset(2, -1));
        assertEquals(2, CachedResultSetManager.getOffset(3, 4));
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetResultWithNull()
    {
        context.checking(new Expectations()
            {
                {
                    one(resultSetConfig).getCacheConfig();
                    will(returnValue(ResultSetFetchConfig.createComputeAndCache()));

                    allowing(resultSetConfig).getAvailableColumns();
                    will(returnValue(null));

                    one(resultSetConfig).tryGetGridDisplayId();
                    will(returnValue(null));

                    one(originalDataProvider).getOriginalData();
                    will(returnValue(Collections.emptyList()));

                    allowResultSetCreation(this);
                }
            });
        final IResultSet<String, String> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, resultSetConfig, originalDataProvider);
        assertEquals(0, resultSet.getList().size());
        assertEquals(0, resultSet.getTotalLength());
        assertTrue(StringUtils.isNotEmpty(resultSet.getResultSetKey()));
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetResultWithAlreadyCachedData()
    {
        context.checking(new Expectations()
            {
                {
                    one(resultSetConfig).getCacheConfig();
                    will(returnValue(ResultSetFetchConfig.createFetchFromCache("1")));

                    allowing(resultSetConfig).getAvailableColumns();
                    will(returnValue(null));

                    allowResultSetCreation(this);
                }
            });
        final IResultSet<String, ?> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, resultSetConfig, originalDataProvider);
        assertEquals(2, resultSet.getList().size());
        assertEquals(3, resultSet.getTotalLength());
        assertEquals("1", resultSet.getResultSetKey());
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetResultWithoutCachedData()
    {
        final String value = "value";
        context.checking(new Expectations()
            {
                {
                    one(resultSetConfig).getCacheConfig();
                    will(returnValue(ResultSetFetchConfig.createComputeAndCache()));

                    allowing(resultSetConfig).getAvailableColumns();
                    will(returnValue(null));

                    one(resultSetConfig).tryGetGridDisplayId();
                    will(returnValue(null));

                    one(originalDataProvider).getOriginalData();
                    will(returnValue(Collections.singletonList(value)));

                    allowResultSetCreation(this);
                }
            });
        final IResultSet<String, ?> resultSet =
                resultSetManager.getResultSet(SESSION_TOKEN, resultSetConfig, originalDataProvider);
        assertEquals(1, resultSet.getList().size());
        assertEquals(1, resultSet.getTotalLength());
        assertNotNull(resultSet.getResultSetKey());
        assertEquals(value, resultSet.getList().get(0).getOriginalObject());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRemoveData()
    {
        boolean fail = true;
        try
        {
            resultSetManager.removeResultSet(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public void testCalculateColumnDistinctValues()
    {
        String separator = " ";
        List<GridColumnFilterInfo<String>> filterList =
                createFilterList(createColDef("c1", separator, 0), createColDef("c2", separator, 1));
        List<GridRowModel<String>> rows = new ArrayList<GridRowModel<String>>();
        for (int i = 0; i < CachedResultSetManager.MAX_DISTINCT_COLUMN_VALUES_SIZE * 2; i++)
        {
            rows.add(GridRowModel.createWithoutCustomColumns(i + separator + (i % 2)));
        }
        List<ColumnDistinctValues> result =
                CachedResultSetManager.calculateColumnDistinctValues(rows, GridFilters
                        .createColumnFilter(filterList));

        assertEquals(1, result.size());
        ColumnDistinctValues distinctValues = result.get(0);
        assertEquals("c2", distinctValues.getColumnIdentifier());
        List<String> values = distinctValues.getDistinctValues();
        assertEquals(2, values.size());
        assertEquals("0", values.get(0));
        assertEquals("1", values.get(1));
    }

    private static List<GridColumnFilterInfo<String>> createFilterList(
            IColumnDefinition<String> c1, IColumnDefinition<String> c2)
    {
        List<GridColumnFilterInfo<String>> result = new ArrayList<GridColumnFilterInfo<String>>();
        result.add(new GridColumnFilterInfo<String>(c1, null));
        result.add(new GridColumnFilterInfo<String>(c2, null));
        return result;
    }

    private static IColumnDefinition<String> createColDef(final String identifier,
            final String separator, final int tokenIndex)
    {
        return new IColumnDefinition<String>()
            {
                public String getValue(GridRowModel<String> rowModel)
                {
                    return rowModel.getOriginalObject().split(separator)[tokenIndex];
                }

                public String getIdentifier()
                {
                    return identifier;
                }

                public Comparable<?> getComparableValue(GridRowModel<String> rowModel)
                {
                    return getValue(rowModel);
                }

                public String getHeader()
                {
                    return null; // unused
                }

                public String tryToGetProperty(String key)
                {
                    return null; // unused
                }

            };
    }

}
