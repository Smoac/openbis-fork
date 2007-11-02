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

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * A <code>Geometry</code> is composed of 2 dimensions:
 * <ul>
 * <li>rows</li>
 * <li>columns</li>
 * </ul>
 * <p>
 * This class is not <code>abstract</code> but {@link #getGeometryDirectoryName()} must be overridden by subclasses in
 * order to work properly.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class Geometry implements IStorable
{
    static final String NOT_POSITIVE = "Given number '%d' must be > 0.";

    final static String ROWS = "rows";

    final static String COLUMNS = "columns";

    private final int rows;

    private final int columns;

    protected Geometry(final int rows, final int columns)
    {
        assert columns > 0 : String.format(NOT_POSITIVE, columns);
        this.columns = columns;
        assert rows > 0 : String.format(NOT_POSITIVE, rows);
        this.rows = rows;
    }

    /** Return the number of columns this <code>Geometry</code> is composed of. */
    public final int getColumns()
    {
        return columns;
    }

    /** Return the number of rows this <code>Geometry</code> is composed of. */
    public final int getRows()
    {
        return rows;
    }

    private final String toString(int number)
    {
        return Integer.toString(number);
    }

    /**
     * Loads the geometry from the specified directory.
     */
    final static Geometry loadFrom(final IDirectory directory, final String geometryDirectoryName)
    {
        final IDirectory geometryFolder = Utilities.getSubDirectory(directory, geometryDirectoryName);
        return new Geometry(Utilities.getNumber(geometryFolder, ROWS), Utilities.getNumber(geometryFolder, COLUMNS));
    }

    //
    // IStorable
    //

    public final void saveTo(final IDirectory directory)
    {
        final IDirectory geometryDirectory = directory.makeDirectory(getGeometryDirectoryName());
        geometryDirectory.addKeyValuePair(ROWS, toString(getRows()));
        geometryDirectory.addKeyValuePair(COLUMNS, toString(getColumns()));
    }

    /**
     * Returns the directory name where this <code>Geometry</code> is saved.
     * <p>
     * Currently this method is not supported and must be implemented.
     * </p>
     */
    protected String getGeometryDirectoryName()
    {
        throw new UnsupportedOperationException();
    }

    //
    // Object
    //

    @Override
    public final boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Geometry == false)
        {
            return false;
        }
        final Geometry geometry = (Geometry) obj;
        return geometry.rows == rows && geometry.columns == columns;
    }

    @Override
    public final int hashCode()
    {

        return 17 * 37 + getRows() + getColumns();
    }

    @Override
    public final String toString()
    {
        return getRows() + "x" + getColumns();
    }
}