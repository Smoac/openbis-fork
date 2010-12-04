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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.util.Arrays;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Aggregate functions.
 * 
 * @author Franz-Josef Elmer
 */
public enum AggregateFunction implements ISerializable
{
    MEAN("mean")
    {
        @Override
        public double aggregate(double[] values)
        {
            return SUM.aggregate(values) / values.length;
        }
    },
    MEDIAN("median")
    {
        @Override
        public double aggregate(double[] values)
        {
            Arrays.sort(values);
            int i = values.length / 2;
            return values.length % 2 == 0 ? (values[i - 1] + values[i]) / 2 : values[i];
        }
    },
    SUM("sum")
    {
        @Override
        public double aggregate(double[] values)
        {
            double sum = 0;
            for (double value : values)
            {
                sum += value;
            }
            return sum;
        }
    },
    MIN("minimum")
    {
        @Override
        public double aggregate(double[] values)
        {
            double min = Double.MAX_VALUE;
            for (double value : values)
            {
                min = Math.min(min, value);
            }
            return min;
        }
    },
    MAX("maximum")
    {
        @Override
        public double aggregate(double[] values)
        {
            double max = -Double.MAX_VALUE;
            for (double value : values)
            {
                max = Math.max(max, value);
            }
            return max;
        }
    },
    COUNT("count")
    {
        @Override
        public double aggregate(double[] values)
        {
            return values.length;
        }
    },
    ;

    private final String label;

    private AggregateFunction(String label)
    {
        this.label = label;
    }

    public final String getLabel()
    {
        return label;
    }

    /**
     * Aggregates the specified array of numbers to one number.
     * 
     * @param values an array with at least one value.
     */
    public abstract double aggregate(double[] values);
}
