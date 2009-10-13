/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
public class RowTest extends AssertJUnit
{
    private static final String PROPERTY_KEY = "key";

    private static final String PROPERTY_VALUE = "value";

    private static final String PROPERTY_VALUE2 = "value2";

    private static final class Data
    {
        private final double value;

        Data(double value)
        {
            this.value = value;
        }

        public double getValue()
        {
            return value;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj == this || (obj instanceof Data && ((Data) obj).value == value);
        }

        @Override
        public int hashCode()
        {
            return (int) Double.doubleToRawLongBits(value);
        }

        @Override
        public String toString()
        {
            return Double.toString(value);
        }

    }

    private Mockery context;

    private IColumnDefinition<Data> def1;

    private IColumnDefinition<Data> def2;

    private Row<Data> row;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        def1 = context.mock(IColumnDefinition.class, "def1");
        def2 = context.mock(IColumnDefinition.class, "def2");
        context.checking(new Expectations()
            {
                {
                    allowing(def1).getIdentifier();
                    will(returnValue("def1"));

                    allowing(def2).getIdentifier();
                    will(returnValue("def2"));
                }
            });
        row =
                new Row<Data>(new LinkedHashSet<IColumnDefinition<Data>>(Arrays
                        .<IColumnDefinition<Data>> asList(def1, def2)));
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCol()
    {
        final GridRowModel<Data> data1 = createData();
        final GridRowModel<Data> data2 = createData(4711.25);
        context.checking(new Expectations()
            {
                {
                    one(def1).getComparableValue(data1);
                    will(returnValue(getVal(data1)));

                    one(def2).getComparableValue(data2);
                    will(returnValue(getVal(data2)));
                }
            });

        row.setRowData(data1);

        assertEquals(getVal(data1), ((Number) row.col("def1")).doubleValue());

        row.setRowData(data2);

        assertEquals(getVal(data2), ((Number) row.col("def2")).doubleValue());

        context.assertIsSatisfied();
    }

    private double getVal(final GridRowModel<Data> data1)
    {
        return data1.getOriginalObject().getValue();
    }

    @Test
    public void testColForUnknownID()
    {
        try
        {
            row.col("unknown");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Undefined column: unknown", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testColDefs()
    {
        prepareColDefs(null, PROPERTY_VALUE);

        row.colDefs(PROPERTY_KEY);
        List<ColumnDefinition> defs = row.colDefs(PROPERTY_KEY); // second call tests caching

        context.checking(new Expectations()
            {
                {
                    one(def2).tryToGetProperty(PROPERTY_KEY);
                    will(returnValue(PROPERTY_VALUE));
                }
            });

        assertEquals(1, defs.size());
        assertEquals("def2", defs.get(0).id());
        assertEquals(PROPERTY_VALUE, defs.get(0).property(PROPERTY_KEY));
        assertUnmodifiable(defs);

        context.assertIsSatisfied();
    }

    @Test
    public void testColDefsWithNullArgument()
    {
        List<ColumnDefinition> defs = row.colDefs(null);

        assertEquals(2, defs.size());
        assertUnmodifiable(defs);

        context.assertIsSatisfied();
    }

    @Test
    public void testCols()
    {
        final GridRowModel<Data> data = createData();
        prepareColDefs(null, PROPERTY_VALUE);
        context.checking(new Expectations()
            {
                {
                    one(def2).tryToGetProperty(PROPERTY_KEY);
                    will(returnValue(PROPERTY_VALUE));

                    one(def2).getComparableValue(data);
                    will(returnValue(getVal(data)));
                }
            });
        row.setRowData(data);

        List<Object> values = row.cols(PROPERTY_KEY, PROPERTY_VALUE);

        assertEquals(1, values.size());
        assertEquals(42.25, ((Double) values.get(0)).doubleValue());
        assertUnmodifiable(values);

        context.assertIsSatisfied();
    }

    private GridRowModel<Data> createData()
    {
        return createData(42.25);
    }

    private GridRowModel<Data> createData(double value)
    {
        Data originalObject = new Data(value);
        return new GridRowModel<Data>(originalObject, new HashMap<String, String>());
    }

    @Test
    public void testColsNoMatchingPropertyKey()
    {
        prepareColDefs(null, null);
        row.setRowData(createData());

        List<Object> values = row.cols(PROPERTY_KEY, "unknown");

        assertEquals(0, values.size());
        assertUnmodifiable(values);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsNoMatchingPropertyValue()
    {
        prepareColDefs(null, PROPERTY_VALUE);
        context.checking(new Expectations()
            {
                {
                    one(def2).tryToGetProperty(PROPERTY_KEY);
                    will(returnValue(PROPERTY_VALUE));
                }
            });
        row.setRowData(createData());

        List<Object> values = row.cols(PROPERTY_KEY, "unknown");

        assertEquals(0, values.size());
        assertUnmodifiable(values);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsGroupedByNoMatchingPropertyKey()
    {
        prepareColDefs(null, null);

        List<ColumnGroup> groups = row.colsGroupedBy(PROPERTY_KEY);

        assertEquals(0, groups.size());
        assertUnmodifiable(groups);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsGroupedByWithOneGroup()
    {
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE);
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE);
        final GridRowModel<Data> data = createData();
        context.checking(new Expectations()
            {
                {
                    one(def1).getComparableValue(data);
                    will(returnValue(getVal(data)));

                    one(def2).getComparableValue(data);
                    will(returnValue(2 * getVal(data)));
                }
            });

        row.setRowData(data);
        List<ColumnGroup> groups = row.colsGroupedBy(PROPERTY_KEY);

        assertEquals(1, groups.size());
        assertEquals(PROPERTY_VALUE, groups.get(0).propertyValue());
        assertEquals(2, groups.get(0).values().size());
        assertEquals(new Double(getVal(data)), groups.get(0).values().get(0));
        assertEquals(new Double(2 * getVal(data)), groups.get(0).values().get(1));
        assertUnmodifiable(groups);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsGroupedByWithTwoGroups()
    {
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE2);
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE2);
        final GridRowModel<Data> data = createData();
        context.checking(new Expectations()
            {
                {
                    one(def1).getComparableValue(data);
                    will(returnValue(getVal(data)));

                    one(def2).getComparableValue(data);
                    will(returnValue(2 * getVal(data)));
                }
            });

        row.setRowData(data);
        List<ColumnGroup> groups = row.colsGroupedBy(PROPERTY_KEY);

        assertEquals(2, groups.size());
        assertEquals(PROPERTY_VALUE, groups.get(0).propertyValue());
        assertEquals(1, groups.get(0).values().size());
        assertEquals(new Double(getVal(data)), groups.get(0).values().get(0));
        assertEquals(PROPERTY_VALUE2, groups.get(1).propertyValue());
        assertEquals(1, groups.get(1).values().size());
        assertEquals(new Double(2 * getVal(data)), groups.get(1).values().get(0));
        assertUnmodifiable(groups);

        context.assertIsSatisfied();
    }

    private void prepareColDefs(final String propertyValueOfFirstDefinition,
            final String propertyValueOfSecondDefinition)
    {
        context.checking(new Expectations()
            {
                {
                    one(def1).tryToGetProperty(PROPERTY_KEY);
                    will(returnValue(propertyValueOfFirstDefinition));

                    one(def2).tryToGetProperty(PROPERTY_KEY);
                    will(returnValue(propertyValueOfSecondDefinition));
                }
            });
    }

    private void assertUnmodifiable(List<?> list)
    {
        try
        {
            list.clear();
            fail("unmodifable list expected");
        } catch (UnsupportedOperationException ex)
        {
            // ignored
        }
    }
}
