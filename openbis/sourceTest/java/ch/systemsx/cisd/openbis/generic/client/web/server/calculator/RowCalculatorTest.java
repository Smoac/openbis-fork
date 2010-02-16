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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
public class RowCalculatorTest extends AssertJUnit
{
    private Set<IColumnDefinition<Data>> availableColumns;

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

    @BeforeMethod
    public void setUp()
    {
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
    }

    @Test
    public void testEvalToDoubleWithStrangeParameterName()
    {
        Set<ParameterWithValue> parameters = createParameters("f.*r", "42");
        RowCalculator<Data> calculator = createCalculator(parameters, "row.col('VALUE') * ${f.*r}");

        calculator.setRowData(createData(2.5));
        assertEquals(105.0, calculator.evalToDouble());

        calculator.setRowData(createData(10));
        assertEquals(420.0, calculator.evalToDouble());
    }

    @Test
    public void testEvalToDoubleWithTwoParameters()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "10");
        parameters.addAll(createParameters("y", "1"));
        RowCalculator<Data> calculator =
                createCalculator(parameters, "${x} * row.col('VALUE') + ${y}");

        calculator.setRowData(createData(2.5));
        assertEquals(26.0, calculator.evalToDouble());

        calculator.setRowData(createData(10));
        assertEquals(101.0, calculator.evalToDouble());
    }

    @Test
    public void testEvalToBoolean()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "42");
        RowCalculator<Data> calculator = createCalculator(parameters, "row.col('VALUE') < ${x}");

        calculator.setRowData(createData(2.5));
        assertEquals(true, calculator.evalToBoolean());

        calculator.setRowData(createData(43));
        assertEquals(false, calculator.evalToBoolean());
    }

    @Test
    public void testEvalToInt()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "42");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "int(row.col('VALUE') * ${x})");

        calculator.setRowData(createData(2.5));
        assertEquals(105, calculator.evalToInt());

        calculator.setRowData(createData(10));
        assertEquals(420, calculator.evalToInt());
    }

    @Test
    public void testEvalToBigInt()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "10");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "${x} ** int(row.col('VALUE'))");

        calculator.setRowData(createData(10));
        assertEquals(new BigInteger("10000000000"), calculator.evalToBigInt());
    }

    @Test
    public void testEvalAsString()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "42");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "str(${x} + row.col('VALUE'))");

        calculator.setRowData(createData(10));
        assertEquals("52.0", calculator.evalAsString());

        calculator.setRowData(createData(-10));
        assertEquals("32.0", calculator.evalAsString());
    }

    @Test
    public void testMathFunctionAndStandardFunction()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "choose(row.col('VALUE') < PI, cos(${x}), ${x})");

        calculator.setRowData(createData(3.14));
        assertEquals(1.0, calculator.evalToDouble());

        calculator.setRowData(createData(3.15));
        assertEquals(0.0, calculator.evalToDouble());
    }

    @Test
    public void testIntFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "map(int, [${x},'  ',2,2.5,'3'])");

        assertEquals("[0, -2147483648, 2, 2, 3]", calculator.evalAsString());

    }

    @Test
    public void testFloatFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "map(float, [${x},'  ',2,2.5,'3'])");

        assertEquals("[0.0, -1.7976931348623157E308, 2.0, 2.5, 3.0]", calculator.evalAsString());

    }

    @Test
    public void testMinFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "min([${x},None,'  ',-2,'-3'])");

        assertEquals(-3.0, calculator.evalToDouble());

    }

    @Test
    public void testMaxFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "max([${x},None,'  ',-2,'-3'])");

        assertEquals(0.0, calculator.evalToDouble());

    }
    
    @Test
    public void testMinOrDefaultFunction()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "minOrDefault([${x},None,'  ',-2,'-3'])");
        
        try {
            calculator.evalToDouble();
            fail("EvaluatorException expected");
        } catch (EvaluatorException e){
            // do nothing -- this is expected
        }
        
        calculator = createCalculator(parameters, "minOrDefault([${x},None,'  ',-2,'-3'], 38.6)");
        assertEquals(-3.0, calculator.evalToDouble());
        
        calculator = createCalculator(parameters, "minOrDefault([], 38.6)");
        assertEquals(38.6, calculator.evalToDouble());

    }

    @Test
    public void testMaxOrDefaultFunction()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator<Data> calculator =
                createCalculator(parameters, "maxOrDefault([${x},None,'  ',-2,'-3'])");
        try {
            calculator.evalToDouble();
            fail("EvaluatorException expected");
        } catch (EvaluatorException e){
            // do nothing -- this is expected
        }
        
        calculator = createCalculator(parameters, "maxOrDefault([${x},None,'  ',-2,'-3'], 38.6)");
        assertEquals(0.0, calculator.evalToDouble());
        
        calculator = createCalculator(parameters, "maxOrDefault([], 38.6)");
        assertEquals(38.6, calculator.evalToDouble());
    }

    private RowCalculator<Data> createCalculator(Set<ParameterWithValue> parameters,
            String expression)
    {
        return new RowCalculator<Data>(availableColumns, expression, parameters);
    }

    private Set<ParameterWithValue> createParameters(String name, String value)
    {
        ParameterWithValue parameter = new ParameterWithValue();
        parameter.setParameter(name);
        parameter.setValue(value);
        return new LinkedHashSet<ParameterWithValue>(Arrays.asList(parameter));
    }

    private GridRowModel<Data> createData(double value)
    {
        Data data = new Data();
        data.setValue(value);
        return GridRowModel.createWithoutCustomColumns(data);
    }
}
