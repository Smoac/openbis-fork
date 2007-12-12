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
    private CollectionUtils()
    {
        // Can not be instantiated
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * <p>
     * By default it shows the number of items left, {@link CollectionStyle#DEFAULT_COLLECTION_STYLE} and
     * {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength)
    {
        return abbreviate(objects, maxLength, true);
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * <p>
     * By default it shows the number of items left, {@link CollectionStyle#DEFAULT_COLLECTION_STYLE} and
     * {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength)
    {
        return abbreviate(collection, maxLength, true);
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * <p>
     * By default it shows the number of items left and {@link ToStringDefaultConverter} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength, final CollectionStyle style)
    {
        return abbreviate(objects, maxLength, true, ToStringDefaultConverter.getInstance(), style);
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * <p>
     * By default it shows the number of items left and {@link ToStringDefaultConverter} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final CollectionStyle style)
    {
        return abbreviate(collection, maxLength, true, ToStringDefaultConverter.getInstance(), style);
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * <p>
     * By default {@link CollectionStyle#DEFAULT_COLLECTION_STYLE} and {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength, final boolean showLeft)
    {
        return abbreviate(objects, maxLength, showLeft, ToStringDefaultConverter.getInstance());
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * <p>
     * By default {@link CollectionStyle#DEFAULT_COLLECTION_STYLE} and {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final boolean showLeft)
    {
        return abbreviate(collection, maxLength, showLeft, ToStringDefaultConverter.getInstance());
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * <p>
     * By default {@link CollectionStyle#DEFAULT_COLLECTION_STYLE} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength, final boolean showLeft,
            final IToStringConverter<? super T> converter)
    {
        return abbreviate(objects, maxLength, showLeft, converter, CollectionStyle.DEFAULT_COLLECTION_STYLE);
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * <p>
     * By default {@link CollectionStyle#DEFAULT_COLLECTION_STYLE} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final boolean showLeft, final IToStringConverter<? super T> converter)
    {
        return abbreviate(collection, maxLength, showLeft, converter, CollectionStyle.DEFAULT_COLLECTION_STYLE);
    }

    /**
     * Abbreviates a given array of <code>Object</code> using ellipses.
     * 
     * <pre>
     * CollectionUtils.abbreviate(new String[] { &quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot; }, 3, false) = &quot;[1, 2, 3, ...]&quot;
     * CollectionUtils.abbreviate(new String[] { &quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot; }, 3, true) = &quot;[1, 2, 3, ... (2 left)]&quot;
     * </pre>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     * @param showLeft whether the number of items left should be displayed at the end of the output. This is only
     *            relevant if you limit the number of items displayed.
     * @param style the style that should be applied to the output.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength, final boolean showLeft,
            final IToStringConverter<? super T> converter, final CollectionStyle style)
    {
        return abbreviate(Arrays.asList(objects), maxLength, showLeft, converter, style);
    }

    /**
     * Abbreviates a given <code>Collection</code> using ellipses.
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be
     *            displayed.
     * @param showLeft whether the number of items left should be displayed at the end of the output. This is only
     *            relevant if you limit the number of items displayed.
     * @param style the style that should be applied to the output.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final boolean showLeft, final IToStringConverter<? super T> converter, final CollectionStyle style)
    {
        assert collection != null : "Given collection can not be null.";
        assert converter != null : "Given converter can not be null.";
        assert style != null : "Given style can not be null.";
        final StringBuilder builder = new StringBuilder(style.getCollectionStart());
        final Iterator<T> iterator = collection.iterator();
        for (int i = 0; iterator.hasNext() && (i < maxLength || maxLength < 0); i++)
        {
            if (i > 0)
            {
                builder.append(style.getCollectionSeparator());
            }
            builder.append(converter.toString(iterator.next()));
        }
        int size = collection.size();
        if (maxLength > 0 && maxLength < size)
        {
            builder.append(style.getCollectionSeparator());
            builder.append("...");
            if (showLeft)
            {
                builder.append(" (").append(size - maxLength).append(" left)");
            }
        }
        builder.append(style.getCollectionEnd());
        return builder.toString();
    }
}