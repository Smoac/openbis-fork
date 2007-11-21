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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link CollectionUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class CollectionUtilsTest
{

    @Test
    public final void testAbbreviateWithArray()
    {
        try
        {
            CollectionUtils.abbreviate((Collection<?>) null, 0, false);
            fail("Given list can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        String[] s = StringUtilities.getStrings(5);
        CollectionStyle collectionStyle = CollectionStyle.DEFAULT_COLLECTION_STYLE;
        String string =
                collectionStyle.getCollectionStart() + StringUtils.join(s, collectionStyle.getCollectionSeparator())
                        + collectionStyle.getCollectionEnd();
        assertEquals(string, CollectionUtils.abbreviate(s, -1, false));
        assertEquals(string, CollectionUtils.abbreviate(s, -10, false));
        assertEquals(string, CollectionUtils.abbreviate(s, 5, false));
        assertEquals(string, CollectionUtils.abbreviate(s, 10, false));
        // With ellipses
        s = new String[]
            { "1", "2", "3", "4", "5" };
        assertEquals("[1, 2, 3, ...]", CollectionUtils.abbreviate(s, 3, false));
        assertEquals("[1, ...]", CollectionUtils.abbreviate(s, 1, false));
        assertEquals("[]", CollectionUtils.abbreviate(s, 0, false));
        // With show left
        assertEquals("[]", CollectionUtils.abbreviate(s, 0));
        assertEquals("[1, ... (4 left)]", CollectionUtils.abbreviate(s, 1));
        assertEquals("[1, 2, ... (3 left)]", CollectionUtils.abbreviate(s, 2));
        assertEquals(CollectionUtils.abbreviate(s, 10, false), CollectionUtils.abbreviate(s, 10));
    }

    @Test
    public final void testIsEmpty()
    {
        try
        {
            CollectionUtils.isEmpty(null);
            fail("Given iterable can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        final List<String> list = new ArrayList<String>();
        assertEquals(true, CollectionUtils.isEmpty(list));
        list.add("");
        assertEquals(true, CollectionUtils.isEmpty(list));
        list.add(" ");
        assertEquals(false, CollectionUtils.isEmpty(list));
        list.clear();
        list.add("x");
        assertEquals(false, CollectionUtils.isEmpty(list));
    }
}
