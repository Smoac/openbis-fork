/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.collections;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Tests for {@link FilteredList}.
 * 
 * @author Christian Ribeaud
 */
public final class FilteredListTest
{

    @Test
    public final void testDecorate()
    {
        try
        {
            FilteredList.decorate(new ArrayList<String>(), null);
            fail("Neither list nor validator can be null");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        try
        {
            FilteredList.decorate(null, new NullValidator());
            fail("Neither list nor validator can be null");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testWithEmptyList()
    {
        List<String> list = FilteredList.decorate(new ArrayList<String>(), new NullValidator());
        list.add(null);
        list.add(null);
        list.add("0");
        list.add(null);
        assert list.size() == 1;
        try
        {
            list.set(1, "1");
            fail("IndexOutOfBoundsException should be thrown.");
        } catch (IndexOutOfBoundsException e)
        {
            // Nothing to do here.
        }
        String old0 = list.set(0, "newO");
        assert list.size() == 1;
        assertEquals("0", old0);
    }

    @Test
    public final void testWithNonEmptyList()
    {
        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add(null);
        list.add("0");
        list.add("1");
        list.add(null);
        list.add(null);
        list.add("2");
        list.add(null);
        assert list.size() == 8;
        list = FilteredList.decorate(list, new NullValidator());
        assert list.size() == 8;
        int count = 0;
        for (final String string : list)
        {
            assertEquals(count++ + "", string);
        }
        assert count == 3;        
    }

    //
    // Helper classes
    //

    private final static class NullValidator implements Validator<String>
    {

        //
        // Validator
        //

        public boolean isValid(String object)
        {
            return object != null;
        }
    }
}