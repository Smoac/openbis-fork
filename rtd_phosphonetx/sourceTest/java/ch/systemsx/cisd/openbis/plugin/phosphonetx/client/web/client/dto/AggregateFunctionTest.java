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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AggregateFunctionTest extends AssertJUnit
{
    @Test
    public void testMean()
    {
        assertEquals(42.0, AggregateFunction.MEAN.aggregate(new double[] {42}));
        assertEquals(42.5, AggregateFunction.MEAN.aggregate(new double[] {42, 43}));
        assertEquals(44.0, AggregateFunction.MEAN.aggregate(new double[] {42, 42, 48}));
    }
    
    @Test
    public void testMedian()
    {
        assertEquals(42.0, AggregateFunction.MEDIAN.aggregate(new double[] {42}));
        assertEquals(42.5, AggregateFunction.MEDIAN.aggregate(new double[] {42, 43}));
        assertEquals(43.0, AggregateFunction.MEDIAN.aggregate(new double[] {42, 43, 50}));
        assertEquals(46.5, AggregateFunction.MEDIAN.aggregate(new double[] {42, 43, 50, 59}));
    }
    
    @Test
    public void testCount()
    {
        assertEquals(1.0, AggregateFunction.COUNT.aggregate(new double[] {42}));
        assertEquals(2.0, AggregateFunction.COUNT.aggregate(new double[] {42, 43}));
    }
    
}
