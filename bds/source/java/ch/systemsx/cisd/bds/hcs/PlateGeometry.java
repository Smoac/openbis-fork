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

package ch.systemsx.cisd.bds.hcs;

import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * An <code>AbstractGeometry</code> implementation suitable for <i>Plate</i>.
 * 
 * @author Christian Ribeaud
 */
public final class PlateGeometry extends Geometry
{

    /**
     * Directory name which contains the plate geometry.
     * <p>
     * Also used as unique identifier when used as {@link FormatParameter}.
     * </p>
     */
    public static final String PLATE_GEOMETRY = "plate_geometry";

    public PlateGeometry(final Geometry geometry)
    {
        this(geometry.getRows(), geometry.getColumns());
    }

    public PlateGeometry(final int rows, final int columns)
    {
        super(rows, columns);
    }

    /**
     * Loads the geometry from the specified directory.
     */
    final static Geometry loadFrom(final IDirectory directory)
    {
        return new PlateGeometry(loadFrom(directory, PLATE_GEOMETRY));
    }

    /**
     * Creates a new <code>WellGeometry</code> from given <var>toString</var>.
     */
    public final static Geometry createFromString(final String toString)
    {
        return new PlateGeometry(Geometry.createFromString(toString));
    }

    //
    // Geometry
    //

    @Override
    protected final String getGeometryDirectoryName()
    {
        return PLATE_GEOMETRY;
    }

}
