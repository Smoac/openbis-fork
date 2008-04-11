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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * Test cases for the {@link CollectionUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class CollectionUtilsTest
{

    @Test
    public final void testAbbreviateWithEllipsesAndNoLeft()
    {
        final String[] s = new String[]
            { "1", "2", "3", "4", "5" };
        assertEquals("[1, 2, 3, ...]", CollectionUtils.abbreviate(s, 3, false));
        assertEquals("[1, ...]", CollectionUtils.abbreviate(s, 1, false));
        assertEquals("[]", CollectionUtils.abbreviate(s, 0, false));
    }

    @Test
    public final void testAbbreviateWithLeft()
    {
        final String[] s = new String[]
            { "1", "2", "3", "4", "5" };
        assertEquals("[]", CollectionUtils.abbreviate(s, 0));
        assertEquals("[1, ... (4 left)]", CollectionUtils.abbreviate(s, 1));
        assertEquals("[1, 2, ... (3 left)]", CollectionUtils.abbreviate(s, 2));
        assertEquals(CollectionUtils.abbreviate(s, 10, false), CollectionUtils.abbreviate(s, 10));
    }

    @Test
    public final void testAbbreviateWithArray()
    {
        final String[] s = StringUtilities.getStrings(5);
        final CollectionStyle collectionStyle = CollectionStyle.DEFAULT;
        String string =
                collectionStyle.getCollectionStart()
                        + StringUtils.join(s, collectionStyle.getCollectionSeparator())
                        + collectionStyle.getCollectionEnd();
        assertEquals(string, CollectionUtils.abbreviate(s, -1, false));
        assertEquals(string, CollectionUtils.abbreviate(s, -10, false));
        assertEquals(string, CollectionUtils.abbreviate(s, 5, false));
        assertEquals(string, CollectionUtils.abbreviate(s, 10, false));
    }

    @Test
    public final void testAbbreviateWithError()
    {
        final Object[] objects = new Object[0];
        try
        {
            CollectionUtils.abbreviate((Collection<?>) null, 0, false);
            fail("Given list can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        try
        {
            CollectionUtils.abbreviate(objects, 0, false, ToStringDefaultConverter.getInstance(),
                    null);
            fail("Given CollectionStyle can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        try
        {
            CollectionUtils.abbreviate(objects, 0, null);
            fail("Given CollectionStyle can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        try
        {
            CollectionUtils.abbreviate((Collection<?>) null, 0, false, null);
            fail("IToStringConverter can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
    }
}
