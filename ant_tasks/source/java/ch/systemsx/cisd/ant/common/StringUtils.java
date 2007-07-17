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

package ch.systemsx.cisd.ant.common;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class is a subset of <code>org.apache.commons.lang.StringUtils</code>.
 * 
 * @author Christian Ribeaud
 */
public final class StringUtils
{
    /**
     * The empty String <code>""</code>.
     */
    public static final String EMPTY = "";

    private StringUtils()
    {
        // This class can not be instantiated.
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Iterator</code> into a single String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the iteration are
     * represented by empty strings.
     * </p>
     * 
     * @param iterator the <code>Iterator</code> of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, <code>null</code> if null iterator input
     */
    public final static String join(Iterator<String> iterator, char separator)
    {

        // handle null, zero and one elements before building a buffer
        if (iterator == null)
        {
            return null;
        }
        if (iterator.hasNext() == false)
        {
            return EMPTY;
        }
        Object first = iterator.next();
        if (iterator.hasNext() == false)
        {
            return toString(first);
        }

        // two or more elements
        StringBuffer buf = new StringBuffer(256); // Java default is 16, probably too small
        if (first != null)
        {
            buf.append(first);
        }

        while (iterator.hasNext())
        {
            buf.append(separator);
            Object obj = iterator.next();
            if (obj != null)
            {
                buf.append(obj);
            }
        }

        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Iterator</code> into a single String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A <code>null</code> separator is the same as an empty String
     * ("").
     * </p>
     * 
     * @param iterator the <code>Iterator</code> of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     */
    public final static String join(Iterator<String> iterator, String separator)
    {

        // handle null, zero and one elements before building a buffer
        if (iterator == null)
        {
            return null;
        }
        if (!iterator.hasNext())
        {
            return EMPTY;
        }
        Object first = iterator.next();
        if (!iterator.hasNext())
        {
            return toString(first);
        }

        // two or more elements
        StringBuffer buf = new StringBuffer(256); // Java default is 16, probably too small
        if (first != null)
        {
            buf.append(first);
        }

        while (iterator.hasNext())
        {
            if (separator != null)
            {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null)
            {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Collection</code> into a single String containing the provided
     * elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the iteration are
     * represented by empty strings.
     * </p>
     * 
     * @param collection the <code>Collection</code> of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, <code>null</code> if null iterator input
     */
    public final static String join(Collection<String> collection, char separator)
    {
        if (collection == null)
        {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Collection</code> into a single String containing the provided
     * elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A <code>null</code> separator is the same as an empty String
     * ("").
     * </p>
     * 
     * @param collection the <code>Collection</code> of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     */
    public final static String join(Collection<String> collection, String separator)
    {
        if (collection == null)
        {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    // ToString
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Gets the <code>toString</code> of an <code>Object</code> returning an empty string ("") if <code>null</code>
     * input.
     * </p>
     * 
     * @see String#valueOf(Object)
     * @param obj the Object to <code>toString</code>, may be null
     * @return the passed in Object's toString, or nullStr if <code>null</code> input
     */
    public final static String toString(Object obj)
    {
        return obj == null ? "" : obj.toString();
    }
}
