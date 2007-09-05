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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Some convenience methods/utilities around {@link Collection}.
 * 
 * @author Christian Ribeaud
 */
public final class CollectionUtils
{

    public final static String DEFAULT_COLLECTION_START = "[";

    public final static String DEFAULT_COLLECTION_END = "]";

    public final static String DEFAULT_COLLECTION_SEPARATOR = ", ";

    private static String collectionStart = DEFAULT_COLLECTION_START;

    private static String collectionEnd = DEFAULT_COLLECTION_END;

    private static String collectionSeparator = DEFAULT_COLLECTION_SEPARATOR;

    private CollectionUtils()
    {
        // Can not be instantiated
    }

    public static final void setCollectionEnd(String collectionEnd)
    {
        CollectionUtils.collectionEnd = collectionEnd;
    }

    public static final void setCollectionSeparator(String collectionSeparator)
    {
        CollectionUtils.collectionSeparator = collectionSeparator;
    }

    public static final void setCollectionStart(String collectionStart)
    {
        CollectionUtils.collectionStart = collectionStart;
    }

    public static final String getCollectionEnd()
    {
        return collectionEnd;
    }

    public static final String getCollectionSeparator()
    {
        return collectionSeparator;
    }

    public static final String getCollectionStart()
    {
        return collectionStart;
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * <p>
     * By default it shows the number of items left.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static String abbreviate(Object[] objects, int maxLength)
    {
        return abbreviate(objects, maxLength, true);
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * <p>
     * By default it shows the number of items left.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static String abbreviate(Collection<?> collection, int maxLength)
    {
        return abbreviate(collection, maxLength, true);
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * 
     * <pre>
     * CollectionUtils.abbreviate(new String[] { "1", "2", "3", "4", "5" }, 3, false) = "[1, 2, 3, ...]"
     * CollectionUtils.abbreviate(new String[] { "1", "2", "3", "4", "5" }, 3, true) = "[1, 2, 3, ... (2 left)]"
     * </pre>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     * @param showLeft whether the number of items left should be displayed at the end of the output. This is only
     *            relevant if you limit the number of items displayed.
     */
    public final static String abbreviate(Object[] objects, int maxLength, boolean showLeft)
    {
        return abbreviate(Arrays.asList(objects), maxLength, showLeft);
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     * @param showLeft whether the number of items left should be displayed at the end of the output. This is only
     *            relevant if you limit the number of items displayed.
     */
    public final static String abbreviate(Collection<?> collection, int maxLength, boolean showLeft)
    {
        assert collection != null;
        StringBuilder builder = new StringBuilder(collectionStart);
        Iterator<?> iterator = collection.iterator();
        for (int i = 0; iterator.hasNext() && (i < maxLength || maxLength < 0); i++)
        {
            if (i > 0)
            {
                builder.append(collectionSeparator);
            }
            builder.append(String.valueOf(iterator.next()));
        }
        int size = collection.size();
        if (maxLength > 0 && maxLength < size)
        {
            builder.append(collectionSeparator);
            builder.append("...");
            if (showLeft)
            {
                builder.append(" (").append(size - maxLength).append(" left)");
            }
        }
        builder.append(collectionEnd);
        return builder.toString();
    }
}