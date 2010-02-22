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

package ch.systemsx.cisd.bds;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.storage.INodeFilter;
import ch.systemsx.cisd.bds.storage.NodeFilters;

/**
 * Storage utility methods.
 * 
 * @author Franz-Josef Elmer
 */
public class Utilities
{
    /**
     * Returns a subdirectory from the specified directory. If it does not exist it will be created.
     * 
     * @throws DataStructureException if there is already a node named <code>name</code> but which
     *             isn't a directory.
     */
    public static IDirectory getOrCreateSubDirectory(final IDirectory directory, final String name)
    {
        final INode node = directory.tryGetNode(name);
        if (node == null)
        {
            return directory.makeDirectory(name);
        }
        if (node instanceof IDirectory)
        {
            return (IDirectory) node;
        }
        throw new DataStructureException("There is already a node named '" + name
                + "' but which isn't a directory (" + node + ").");
    }

    /**
     * Returns a subdirectory from the specified directory.
     * 
     * @param directory Parent directory of the requested directory.
     * @param name Name of the requested directory.
     * @throws DataStructureException if requested directory not found.
     */
    public final static IDirectory getSubDirectory(final IDirectory directory, final String name)
            throws DataStructureException
    {
        final INode node = directory.tryGetNode(name);
        if (node == null)
        {
            throw new DataStructureException(String.format(
                    "No directory named '%s' found in directory '%s'.", name, directory));
        }
        if (node instanceof IDirectory == false)
        {
            throw new DataStructureException("Is not a directory: " + node);
        }
        return (IDirectory) node;
    }

    /**
     * Convenient short cut for <code>{@link #getString(IDirectory, String)}.trim()</code>.
     */
    public static String getTrimmedString(final IDirectory directory, final String name)
    {
        return getString(directory, name).trim();
    }

    /**
     * Returns the string content of a file from the specified directory.
     * 
     * @param directory Directory of the requested file.
     * @param name Name of the file.
     * @return never <code>null</code> but could return an empty string.
     * @throws DataStructureException if the requested file does not exist.
     */
    public static String getString(final IDirectory directory, final String name)
    {
        final INode node = tryGetFileNode(directory, name);
        final IFile file = (IFile) node;
        return file.getStringContent();
    }

    /**
     * Returns the string content of a file from the specified directory. Doesn't change line
     * terminating characters to '\n'.
     * 
     * @param directory Directory of the requested file.
     * @param name Name of the file.
     * @return never <code>null</code> but could return an empty string.
     * @throws DataStructureException if the requested file does not exist.
     */
    public static String getExactString(final IDirectory directory, final String name)
    {
        final INode node = tryGetFileNode(directory, name);
        final IFile file = (IFile) node;
        return file.getExactStringContent();
    }

    /**
     * Returns the string content of a file from the specified directory as list of
     * <code>String</code> objects.
     * 
     * @param directory Directory of the requested file.
     * @param name Name of the file.
     * @return never <code>null</code> but could return an empty list.
     * @throws DataStructureException if the requested file does not exist.
     */
    public static List<String> getStringList(final IDirectory directory, final String name)
    {
        final INode node = tryGetFileNode(directory, name);
        final IFile file = (IFile) node;
        return file.getStringContentList();
    }

    private final static INode tryGetFileNode(final IDirectory directory, final String name)
    {
        assert directory != null : String.format("Given directory can not be null.");
        assert name != null : String.format("Given name can not be null.");
        final INode node = directory.tryGetNode(name);
        if (node == null)
        {
            throw new DataStructureException("File '" + name + "' missing in '" + directory + "'.");
        }
        if (node instanceof IFile == false)
        {
            throw new DataStructureException(node + " is not a file.");
        }
        return node;
    }

    private Utilities()
    {
    }

    /**
     * Return the string content of a file from the given <var>directory</var> as boolean (<code>TRUE</code>
     * or <code>FALSE</code>).
     * 
     * @throws DataStructureException If the value of <var>name</var> is not a boolean.
     */
    public static Boolean getBoolean(final IDirectory directory, final String name)
            throws DataStructureException
    {
        // No assertion here as 'getString(IDirectory, String)' already does it.
        final String value = getTrimmedString(directory, name);
        try
        {
            return Boolean.fromString(value);
        } catch (final IllegalArgumentException ex)
        {
            throw new DataStructureException("Value of '" + name
                    + "' version file is not a boolean (TRUE or FALSE): '" + value + "'.");
        }
    }

    /** For given <code>IDirectory</code> returns the number value corresponding to given <var>name</var>. */
    public final static int getNumber(final IDirectory directory, final String name)
            throws DataStructureException
    {
        // No assertion here as 'getString(IDirectory, String)' already does it.
        final String value = getTrimmedString(directory, name);
        try
        {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ex)
        {
            throw new DataStructureException("Value of '" + name
                    + "' version file is not a number: '" + value + "'.");
        }
    }

    /**
     * For given <code>IDirectory</code> returns the date value corresponding to given <var>name</var>.
     * 
     * @return the parsed date or <code>null</code> if the value is empty.
     */
    public final static Date tryGetDate(final IDirectory directory, final String name)
            throws DataStructureException
    {
        // No assertion here as 'getString(IDirectory, String)' already does it.
        final String value = getTrimmedString(directory, name);
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        try
        {
            return ch.systemsx.cisd.common.Constants.DATE_FORMAT.get().parse(value);
        } catch (final ParseException ex)
        {
            throw new DataStructureException("Value of '" + name
                    + "' version file is not a date: '" + value + "'.");
        }
    }

    /**
     * Recursively lists nodes in given <var>directory</var>.
     */
    public final static List<String> listNodes(final IDirectory diretory,
            final INodeFilter nodeFilterOrNull)
    {
        assert diretory != null : "Given node can not be null.";
        final INodeFilter nodeFilter;
        if (nodeFilterOrNull == null)
        {
            nodeFilter = NodeFilters.TRUE_NODE_FILTER;
        } else
        {
            nodeFilter = nodeFilterOrNull;
        }
        final List<String> nodes = new LinkedList<String>();
        innerListNodes(nodes, diretory, nodeFilter, "");
        return nodes;
    }

    private final static void innerListNodes(final List<String> nodes, final IDirectory directory,
            final INodeFilter nodeFilter, final String prepend)
    {
        assert prepend != null : "Prepend is never null.";
        for (final INode child : directory)
        {
            if (child instanceof IFile && nodeFilter.accept(child))
            {
                nodes.add(prepend + child.getName());
            } else if (child instanceof IDirectory)
            {
                innerListNodes(nodes, (IDirectory) child, nodeFilter, prepend + child.getName()
                        + Constants.PATH_SEPARATOR);
            }
        }
    }

    //
    // Helper classes
    //

    /**
     * A boolean object that uses <code>TRUE</code> and <code>FALSE</code> as text
     * representation but accepts also <code>true</code> and <code>false</code> when converting
     * from strings.
     */
    public static enum Boolean
    {
        TRUE, FALSE;

        /** Converts this object to corresponding <code>boolean</code>. */
        public boolean toBoolean()
        {
            return this == TRUE ? true : false;
        }

        /** Converts given <code>boolean</code> to this enumeration item. */
        public static Boolean fromBoolean(final boolean bool)
        {
            return bool ? TRUE : FALSE;
        }

        /**
         * Converts a string value to a {@link Boolean}. Accepts either <code>true</code>,
         * <code>TRUE</code>, <code>false</code> or <code>FALSE</code>.
         * 
         * @throws IllegalArgumentException if <var>value</var> is not one of the values listed
         *             above.
         */
        public static Boolean fromString(final String value)
        {
            if ("true".equalsIgnoreCase(value))
            {
                return TRUE;
            }
            if ("false".equalsIgnoreCase(value))
            {
                return FALSE;
            }
            throw new IllegalArgumentException("" + value);
        }
    }
}