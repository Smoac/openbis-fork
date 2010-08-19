/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

/**
 * A {@link TickUnitSource} derived from {@link org.jfree.chart.axis.StandardTickUnitSource}.
 * <p>
 * Whereas StandardTickUnitSource always generates labels that use scientific notation, this class
 * uses standard numerical notation and switched to scientific notation only if the number of digits
 * exceeds a configurable value.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataTickUnitSource implements TickUnitSource, Serializable
{
    final private static long serialVersionUID = 1L;

    /** Constant for log(10.0). */
    final private static double LOG_10_VALUE = Math.log(10.0);

    final private int largeScientificNotationTransition = 6;

    final private int smallScientificNotationTransition = 3;

    /**
     * Returns a tick unit that is larger than the supplied unit.
     * 
     * @param unit the unit (<code>null</code> not permitted).
     * @return A tick unit that is larger than the supplied unit.
     */
    public TickUnit getLargerTickUnit(TickUnit unit)
    {
        double x = unit.getSize();
        return getCeilingTickUnit(x);
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal to (in size) the
     * specified unit.
     * 
     * @param unit the unit (<code>null</code> not permitted).
     * @return A unit from the collection.
     */
    public TickUnit getCeilingTickUnit(TickUnit unit)
    {
        return getLargerTickUnit(unit);
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal to the specified size.
     * 
     * @param size the size.
     * @return A unit from the collection.
     */
    public TickUnit getCeilingTickUnit(double size)
    {
        // Use number of decimal digits to determine what the tick size and display format should
        // be.
        final double numberOfDigits = Math.log(size) / LOG_10_VALUE;
        final double higher = Math.ceil(numberOfDigits);
        final DecimalFormat numberFormat = getNumberFormat(Math.abs(higher), size >= 1.0);
        final double fullTickIncrement = Math.pow(10, higher);
        final double halfTickIncrement = fullTickIncrement / 2;
        final double tickIncrement =
                (size <= halfTickIncrement) ? halfTickIncrement : fullTickIncrement;
        return new NumberTickUnit(tickIncrement, numberFormat);
    }

    /**
     * Use the precision to determine if we should return standard or scientific notation.
     * 
     * @param precision The desired precision of the numbers to display.
     * @param greaterThan1 True if the numbers to be shown are greater then 1
     */
    private DecimalFormat getNumberFormat(double precision, boolean greaterThan1)
    {
        DecimalFormat numberFormat;
        if (greaterThan1)
        {
            if (precision > largeScientificNotationTransition)
            {
                numberFormat = new DecimalFormat("0.0##E0");
            } else
            {
                numberFormat = getStandardNumberFormat((int) precision, greaterThan1);
            }
        } else
        {
            if (precision > smallScientificNotationTransition)
            {
                numberFormat = new DecimalFormat("0.0##E0");
            } else
            {
                numberFormat = getStandardNumberFormat((int) precision, greaterThan1);
            }
        }
        return numberFormat;
    }

    /**
     * Return a DecimalFormat that uses standard notation (not scientific notation).
     * 
     * @param precision The desired precision of the numbers to display.
     * @param greaterThan1 True if the numbers to be shown are greater then 1
     */
    private DecimalFormat getStandardNumberFormat(int precision, boolean greaterThan1)
    {
        StringBuilder sb = new StringBuilder();
        if (greaterThan1)
        {
            for (int i = 1; i < precision; ++i)
            {
                sb.append("#");
            }
            sb.append("0");
        } else
        {
            sb.append("0.0");
            for (int i = 0; i < precision - 1; ++i)
            {
                sb.append("0");
            }
        }

        return new DecimalFormat(sb.toString());

    }
}
