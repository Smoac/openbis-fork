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
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;

/**
 * @author Franz-Josef Elmer
 */
class RowCalculator<T>
{
    private static final String INITIAL_SCRIPT =
            "from " + StandardFunctions.class.getCanonicalName() + " import *\n"
                    + "def int(x):return toInt(x)\n" + "def float(x):return toFloat(x)\n";

    private final Evaluator evaluator;

    private final Row<T> row;

    public RowCalculator(Set<IColumnDefinition<T>> availableColumns, String expression)
    {
        this(availableColumns, expression, Collections.<ParameterWithValue> emptySet());
    }

    public RowCalculator(Set<IColumnDefinition<T>> availableColumns, String expression,
            Set<ParameterWithValue> parameters)
    {
        evaluator =
                new Evaluator(substitudeParameters(expression, parameters), Math.class,
                        INITIAL_SCRIPT);
        row = new Row<T>(availableColumns);
        evaluator.set("row", row);
    }

    public void setRowData(GridRowModel<T> rowData)
    {
        row.setRowData(rowData);
    }

    public PrimitiveValue getTypedResult()
    {
        Object value = evaluator.eval();
        if (value == null)
        {
            return PrimitiveValue.NULL;
        }
        if (value instanceof Long)
        {
            return new PrimitiveValue((Long) value);
        } else if (value instanceof Double)
        {
            return new PrimitiveValue((Double) value);
        } else
        {
            return new PrimitiveValue(value.toString());
        }
    }

    public boolean evalToBoolean() throws EvaluatorException
    {
        return evaluator.evalToBoolean();
    }

    public int evalToInt() throws EvaluatorException
    {
        return evaluator.evalToInt();
    }

    public BigInteger evalToBigInt() throws EvaluatorException
    {
        return evaluator.evalToBigInt();
    }

    public double evalToDouble() throws EvaluatorException
    {
        return evaluator.evalToDouble();
    }

    public String evalAsString() throws EvaluatorException
    {
        return evaluator.evalAsString();
    }

    private String substitudeParameters(String originalExpression,
            Set<ParameterWithValue> parameters)
    {
        String expression = originalExpression;
        for (ParameterWithValue pw : parameters)
        {
            String substParameter = "${" + pw.getParameter() + "}";
            String quotedParameter = Pattern.quote(substParameter);
            String quotedReplacement = Matcher.quoteReplacement(pw.getValue());
            expression = expression.replaceAll(quotedParameter, quotedReplacement);
        }
        return expression;
    }
}
