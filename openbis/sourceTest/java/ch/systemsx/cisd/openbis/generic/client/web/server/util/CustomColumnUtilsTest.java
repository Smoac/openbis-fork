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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CustomColumnUtilsTest extends AssertJUnit
{
    // Simple data classs to use in the tests
    private static final class Data
    {
        private double value;

        public void setValue(double value)
        {
            this.value = value;
        }

        public double getValue()
        {
            return value;
        }
    }

    Set<IColumnDefinition<Data>> availableColumns;

    GridCustomColumn customColumn;

    List<GridCustomColumn> customColumns;

    @BeforeMethod
    public void setUp()
    {
        // Define a column to use to get data for the custom columns
        availableColumns = new LinkedHashSet<IColumnDefinition<Data>>();
        availableColumns.add(new AbstractColumnDefinition<Data>("header", 100, true, false)
            {

                public String getIdentifier()
                {
                    return "VALUE";
                }

                @Override
                protected String tryGetValue(Data entity)
                {
                    return Double.toString(entity.getValue());
                }

                @Override
                public Comparable<?> tryGetComparableValue(GridRowModel<Data> rowModel)
                {
                    return rowModel.getOriginalObject().getValue();
                }
            });

        // Define a custom column
        customColumn = new GridCustomColumn();
        customColumn.setCode("TEST");

        // We need to associate the column with someone for error messages
        Person registrator = new Person();
        registrator.setFirstName("Jane");
        registrator.setLastName("Doe");
        registrator.setEmail("jane.doe@nowhere.com");
        customColumn.setRegistrator(registrator);

        customColumns = new ArrayList<GridCustomColumn>();
        customColumns.add(customColumn);
    }

    @Test
    public void testCorrectExpression()
    {
        customColumn.setExpression("row.col('VALUE') + 10");

        ArrayList<GridRowModel<Data>> processedList =
                GridExpressionUtils.evalCustomColumns(createData(57, 34), customColumns,
                        availableColumns, false);

        assertEquals(2, processedList.size());
        assertEquals(57.0, processedList.get(0).getOriginalObject().getValue());
        assertEquals(34.0, processedList.get(1).getOriginalObject().getValue());

        assertEquals(67.0, processedList.get(0).findColumnValue("TEST").getComparableValue());
        assertEquals(44.0, processedList.get(1).findColumnValue("TEST").getComparableValue());

    }

    @Test
    public void testShortErrorMessage()
    {
        customColumn.setCode("TEST");

        // incorrect expression
        customColumn.setExpression("junk + 10");

        ArrayList<GridRowModel<Data>> processedList =
                GridExpressionUtils.evalCustomColumns(createData(57, 34), customColumns,
                        availableColumns, false);

        assertEquals(2, processedList.size());
        assertEquals(
                "Error. Please contact 'Jane Doe <jane.doe@nowhere.com>', who defined this column.",
                processedList.get(0).findColumnValue("TEST").getComparableValue());
        assertEquals(
                "Error. Please contact 'Jane Doe <jane.doe@nowhere.com>', who defined this column.",
                processedList.get(1).findColumnValue("TEST").getComparableValue());
    }

    @Test
    public void testLongErrorMessage()
    {
        // incorrect expression
        customColumn.setExpression("junk + 10");

        ArrayList<GridRowModel<Data>> processedList =
                GridExpressionUtils.evalCustomColumns(createData(57, 34), customColumns,
                        availableColumns, true);

        assertEquals(2, processedList.size());
        assertEquals("Error: (Error evaluating 'junk + 10': NameError: junk).", processedList
                .get(0).findColumnValue("TEST").getComparableValue());
        assertEquals("Error: (Error evaluating 'junk + 10': NameError: junk).", processedList
                .get(1).findColumnValue("TEST").getComparableValue());
    }

    private List<Data> createData(double... values)
    {
        List<Data> list = new ArrayList<Data>();
        for (double value : values)
        {
            Data data = new Data();
            data.setValue(value);
            list.add(data);
        }
        return list;
    }

}
