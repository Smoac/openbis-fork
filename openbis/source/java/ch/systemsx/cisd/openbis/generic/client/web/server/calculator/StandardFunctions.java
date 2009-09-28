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

/**
 * Set of standard functions used in jython expressions. 
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 *
 * @author Franz-Josef Elmer
 */
public final class StandardFunctions
{
    private static final Double DOUBLE_DEFAULT_VALUE = new Double(-Double.MAX_VALUE);
    private static final Integer INTEGER_DEFAULT_VALUE = new Integer(Integer.MIN_VALUE);

    public static Integer toInteger(Object value)
    {
        return toInteger(value, INTEGER_DEFAULT_VALUE);
    }
    
    public static Integer toInteger(Object value, Integer defaultValue)
    {
        if (value instanceof Number)
        {
            Number number = (Number) value;
            return number.intValue();
        }
        return isBlank(value) ? defaultValue : new Integer(value.toString());
    }

    public static Double toFloat(Object value)
    {
        return toFloat(value, DOUBLE_DEFAULT_VALUE);
    }
    
    public static Double toFloat(Object value, Double defaultValue)
    {
        if (value instanceof Number)
        {
            Number number = (Number) value;
            return number.doubleValue();
        }
        return isBlank(value) ? defaultValue : new Double(value.toString());
    }
    
    public static Object ifThenElse(Boolean condition, Object thenValue, Object elseValue)
    {
        return condition != null && condition.booleanValue() ? thenValue : elseValue;
    }
    
    private static boolean isBlank(Object value)
    {
        return value == null || value.toString().trim().length() == 0;
    }
    
    private StandardFunctions()
    {
    }
    
}