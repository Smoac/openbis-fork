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

package eu.basysbio.cisd.dss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for {@link TimeSeriesDataSetInfoExtractor}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = TimeSeriesDataSetInfoExtractor.class)
public class TimeSeriesDataSetInfoExtractorTest extends AssertJUnit
{

    private static final String VAL1 = "val1";

    private static final String VAL2 = "val2";

    @Test
    public void testGetPropertyValuePropertyNotDefined() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        boolean exceptionThrown = false;
        try
        {
            TimeSeriesDataSetInfoExtractor.getPropertyValue(property, map, true);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("BiID not defined", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetPropertyValuePropertyDefinedButEmpty() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        map.put(property, set);
        boolean exceptionThrown = false;
        try
        {
            TimeSeriesDataSetInfoExtractor.getPropertyValue(property, map, true);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("BiID not defined", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetPropertyValuePropertyDefinedOnce() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        set.add(VAL1);
        map.put(property, set);
        assertEquals(VAL1, TimeSeriesDataSetInfoExtractor.getPropertyValue(property, map, true));
    }

    @Test
    public void testGetPropertyValuePropertyDefinedMoreThanOnce() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        set.add(VAL1);
        set.add(VAL2);
        map.put(property, set);
        String result = TimeSeriesDataSetInfoExtractor.getPropertyValue(property, map, true);
        assertTrue((VAL1 + ", " + VAL2).equals(result) || (VAL2 + ", " + VAL1).equals(result));
    }
    
    @Test
    public void testGetPropertyValuePropertyDefinedMoreThanOnceButOnlyOneExpected() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        set.add(VAL1);
        set.add(VAL2);
        map.put(property, set);
        boolean exceptionThrown = false;
        try{
        	TimeSeriesDataSetInfoExtractor.getPropertyValue(property, map, false);
        }catch (UserFailureException e) 
        {
        	exceptionThrown=true;
        	assertEquals("Inconsistent header values of 'BiID'. " +
        			"Expected the same value in all the columns, found: [val1, val2].", e.getMessage());
		}
        assertTrue(exceptionThrown);
    }
}
