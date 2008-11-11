/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link FieldComparator} class.
 * 
 * @author Christian Ribeaud
 */
public final class FieldComparatorTest
{

    private final List<Bean> createBeanList()
    {
        final List<Bean> list = new ArrayList<Bean>();
        list.add(new Bean(new Object(), "c"));
        list.add(new Bean(new Object(), "b"));
        list.add(new Bean(new Object(), "a"));
        return list;
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new FieldComparator<Bean>(null);
        } catch (final AssertionError error)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testCompare()
    {
        final FieldComparator<Bean> fieldComparator = new FieldComparator<Bean>("value");
        final List<Bean> beans = createBeanList();
        assertEquals("c", beans.get(0).value);
        Collections.sort(beans, fieldComparator);
        assertEquals("a", beans.get(0).value);
    }

    @Test
    public final void testCompareFailed()
    {
        FieldComparator<Bean> fieldComparator = new FieldComparator<Bean>("doesNotExist");
        final List<Bean> beans = createBeanList();
        try
        {
            Collections.sort(beans, fieldComparator);
            fail("'" + IllegalArgumentException.class + "' expected.");
        } catch (final IllegalArgumentException ex)
        {
            // Nothing to do here.
        }
        fieldComparator = new FieldComparator<Bean>("object");
        try
        {
            Collections.sort(beans, fieldComparator);
            fail("'" + IllegalArgumentException.class + "' expected.");
        } catch (final IllegalArgumentException ex)
        {
            // Nothing to do here.
        }
    }

    //
    // Helper classes
    //

    private static final class Bean
    {
        private final String value;

        @SuppressWarnings("unused")
        private final Object object;

        Bean(final Object object, final String value)
        {
            this.object = object;
            this.value = value;
        }
    }
}
