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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Set of standard functions used in jython expressions. 
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 *
 * @author Franz-Josef Elmer
 */
public final class StandardFunctions
{
    static final Double DOUBLE_DEFAULT_VALUE = new Double(-Double.MAX_VALUE);
    static final Integer INTEGER_DEFAULT_VALUE = new Integer(Integer.MIN_VALUE);

    /**
     * Returns the specified value as an integer. Returns the smallest integer if <code>value</code>
     * is <code>null</code> or its trimmed <code>toString()</code> representation is an empty
     * string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as an
     *             integer.
     */
    public static Integer toInt(Object value)
    {
        return toInt(value, INTEGER_DEFAULT_VALUE);
    }

    /**
     * Returns the specified value as an integer. Returns <code>defaultValue</code> if
     * <code>value</code> is <code>null</code> or its trimmed <code>toString()</code> representation
     * is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as an
     *             integer.
     */
    public static Integer toInt(Object value, Integer defaultValue)
    {
        if (value instanceof Number)
        {
            Number number = (Number) value;
            return number.intValue();
        }
        return isBlank(value) ? defaultValue : new Integer(value.toString());
    }

    /**
     * Returns the specified value as a floating-point number. Returns the smallest floating-point
     * number if <code>value</code> is <code>null</code> or its trimmed <code>toString()</code>
     * representation is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as a
     *             floating-point number.
     */
    public static Double toFloat(Object value)
    {
        return toFloat(value, DOUBLE_DEFAULT_VALUE);
    }

    /**
     * Returns the specified value as a floating-point number. Returns <code>defaultValue</code> if
     * <code>value</code> is <code>null</code> or its trimmed <code>toString()</code> representation
     * is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as a
     *             floating-point number.
     */
    public static Double toFloat(Object value, Double defaultValue)
    {
        if (value instanceof Number)
        {
            Number number = (Number) value;
            return number.doubleValue();
        }
        return isBlank(value) ? defaultValue : new Double(value.toString());
    }
    
    /**
     * Returns <code>thenValue</code> if <code>condition == true</code> otherwise
     * <code>elseValue</code> is returned.
     * 
     * @return <code>elseValue</code> if <code>condition == null</code>.
     */
    public static Object choose(Boolean condition, Object thenValue, Object elseValue)
    {
        return condition != null && condition.booleanValue() ? thenValue : elseValue;
    }

    /**
     * Calculates the mean of the specified values. 
     * Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double avg(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "avg");
        double sum = 0.0;
        for (Double value : array)
        {
            sum += value;
        }
        return sum / array.size();
    }
    
    /**
     * Calculates the median of the specified values. 
     * Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double median(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "median");
        Collections.sort(array);
        int i = array.size() / 2;
        return array.size() % 2 == 0 ? (array.get(i - 1) + array.get(i)) / 2 : array.get(i);
    }
    
    /**
     * Calculates the minimum of the specified values. 
     * Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double min(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "min");
        Collections.sort(array);
        return array.get(0);
    }
    
    /**
     * Calculates the maximum of the specified values. 
     * Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double max(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "max");
        Collections.sort(array);
        return array.get(array.size() - 1);
    }
    
    private static boolean isBlank(Object value)
    {
        return value == null || value.toString().trim().length() == 0;
    }
    
    private static List<Double> toDoubleArray(List<Object> values)
    {
        ArrayList<Double> list = new ArrayList<Double>();
        for (Object value : values)
        {
            if (value == null)
            {
                continue;
            }
            if (value instanceof Number)
            {
                Number number = (Number) value;
                list.add(number.doubleValue());
            } else
            {
                String stringValue = value.toString();
                if (StringUtils.isBlank(stringValue) == false)
                {
                    list.add(new Double(stringValue));
                }
            }
        }
        return list;
    }
    
    private static void assertNotEmpty(List<Double> values, String functionName)
    {
        if (values.isEmpty())
        {
            throw new IllegalArgumentException("Argument of function '" + functionName
                    + "' is an empty array.");
        }
    }
    
    private StandardFunctions()
    {
    }
    
}