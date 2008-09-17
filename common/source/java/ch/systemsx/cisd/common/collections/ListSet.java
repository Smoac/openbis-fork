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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;

/**
 * A set of named lists.
 * <p>
 * Naming: every list has an <i>unique</i> name but not necessarily an <i>unique</i> label.
 * </p>
 * 
 * @author Christian Ribeaud
 */
// TODO 2008-09-05, Christian Ribeaud: Write an Unit test for this class.
public final class ListSet
{
    /**
     * The lists.
     * <p>
     * Each list is identified by a {@link Column} which contains the name and the label of the
     * corresponding list.<br />
     * <code>values</code> of this map are &lt;row index&gt;=&lt;table value&gt; pairs.
     * </p>
     */
    private final Map<Column, Map<Integer, String>> lists =
            new HashMap<Column, Map<Integer, String>>();

    /**
     * A map containing <i>columns</i> keyed by <i>names</i>.
     */
    private final Map<String, Column> columnByName = new LinkedHashMap<String, Column>();

    private final void addColumn(final Column newColumn)
    {
        final Column column = columnByName.get(newColumn.name);
        if (column != null && column.label.equals(newColumn.label) == false)
        {
            throw new IllegalArgumentException(String.format(
                    "Different labels detected for the same column name '%s'. "
                            + "The new one is '%s', the previous one was '%s'.", column.name,
                    newColumn.label, column.label));
        }
        columnByName.put(newColumn.name, newColumn);
    }

    private final Column getColumn(final String listName)
    {
        final Column column = columnByName.get(listName);
        if (column == null)
        {
            throw new IllegalArgumentException(String.format("List name '%s' not found.", listName));
        }
        return column;
    }

    private final static Map<Integer, String> createIntMap()
    {
        return new HashMap<Integer, String>();
    }

    private final Map<Integer, String> getIntMap(final Column column)
    {
        final Map<Integer, String> list = lists.get(column);
        if (list == null)
        {
            throw new IllegalArgumentException(String.format("No list found for column '%s'.",
                    column));
        }
        return list;
    }

    /**
     * Adds the specified value to the specified list at given <var>rowIndex</var>.
     * 
     * @param listName can not be <code>null</code>.
     * @param value can not be <code>null</code>.
     */
    public final void addToList(final String listName, final String listLabel, final String value,
            final int rowIndex)
    {
        assert listName != null : "Unspecified list name.";
        assert value != null : "Unspecified value";
        assert rowIndex > -1 : "Invalid row index";
        final Column column = new Column(listName, listLabel);
        addColumn(column);
        Map<Integer, String> list = lists.get(column);
        if (list == null)
        {
            list = createIntMap();
            lists.put(column, list);
        }
        if (list.containsKey(rowIndex))
        {
            throw new IllegalArgumentException(String.format("List '%s' already contains a "
                    + "value for row index %d", listName, rowIndex));
        }
        list.put(rowIndex, value);
    }

    /** Returns the value at given <var>rowIndex</var> from the list found with given <var>listName</var>. */
    public final String tryGetValueAt(final String listName, final int rowIndex)
    {
        assert listName != null : "Unspecified list name";
        assert rowIndex > -1 : "Invalid row index";
        return getIntMap(getColumn(listName)).get(rowIndex);
    }

    /**
     * For given <var>listName</var> returns corresponding label.
     */
    public final String getLabel(final String listName)
    {
        return getColumn(listName).label;
    }

    /**
     * Returns a set of all list names.
     */
    public final Set<String> getListNames()
    {
        return Collections.unmodifiableSet(columnByName.keySet());
    }

    /**
     * Merges this list set with given one.
     */
    public final void mergeList(final ListSet listSet)
    {
        final Set<String> listNames = listSet.getListNames();
        for (final String listName : listNames)
        {
            final Column column = listSet.getColumn(listName);
            final Map<Integer, String> list = listSet.getIntMap(column);
            for (final Map.Entry<Integer, String> entry : list.entrySet())
            {
                addToList(listName, column.label, entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * Returns the list of given name <var>listName</var> as {@link List}.
     * 
     * @return a list of size <var>rowCount</var>. If no value could be found for a given row
     *         index, is filled with <code>null</code>.
     */
    public final List<String> getList(final String listName, final int rowCount)
    {
        final List<String> result =
                new ArrayList<String>(Collections.<String> nCopies(rowCount, null));
        final Map<Integer, String> list = getIntMap(getColumn(listName));
        for (final Map.Entry<Integer, String> entry : list.entrySet())
        {
            result.set(entry.getKey(), entry.getValue());
        }
        return result;
    }

    //
    // Helper classes
    //

    private final static class Column
    {
        final String name;

        final String label;

        Column(final String name, final String label)
        {
            assert name != null : "Unspecified name";
            this.name = name;
            this.label = label;
        }

        //
        // Object
        //

        @Override
        public final boolean equals(final Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof Column == false)
            {
                return false;
            }
            final Column that = (Column) obj;
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(name, that.name);
            return builder.isEquals();
        }

        @Override
        public final int hashCode()
        {
            final HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(name);
            return builder.toHashCode();
        }

        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }
}