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

import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * A location in (x, y) coordinate space, specified in integer precision.
 * 
 * @author Christian Ribeaud
 */
public final class Location
{
    static final String NOT_POSITIVE = "Given coordinate '%s' must be > 0 (%d <= 0).";

    /**
     * The <i>x</i> (or <i>column</i>) coordinate.
     */
    private final int x;

    /**
     * The <i>y</i> (or <i>row</i>) coordinate.
     */
    private final int y;

    public Location(final int x, final int y)
    {
        assert x > 0 : String.format(NOT_POSITIVE, "x", x);
        this.x = x;
        assert y > 0 : String.format(NOT_POSITIVE, "y", y);
        this.y = y;
    }

    /**
     * For given <var>position</var> in given <code>geometry</code> returns corresponding
     * <code>Location</code>. Position should be greater than 0.
     * 
     * @return <code>null</code> if position is out of range.
     */
    public static final Location tryCreateLocationFromPosition(final int position,
            final Geometry geometry)
    {
        assert geometry != null : "Given geometry can not be null.";
        final int columns = geometry.getColumns();
        final int max = columns * geometry.getRows();
        // Given position is within the range.
        if (position > 0 && position <= max)
        {
            final int modulo = position % columns;
            final int x = modulo == 0 ? columns : modulo;
            final int y = (int) Math.ceil(position / (float) columns);
            return new Location(x, y);
        }
        return null;
    }

    /**
     * For given matrix <var>coordinate</var> in given <code>geometry</code> returns
     * corresponding <code>Location</code>.
     * 
     * @return <code>null</code> if given <var>coordinate</var> is not a matrix coordinate.
     */
    public static final Location tryCreateLocationFromMatrixCoordinate(final String coordinate)
    {
        assert coordinate != null : "Coordinate can not be null.";
        final String[] split = StringUtilities.splitMatrixCoordinate(coordinate);
        if (split == null)
        {
            return null;
        }
        try
        {
            final String letter = split[0];
            assert letter.length() == 1 : "Only one letter is supported right now.";
            final int y = letter.toLowerCase().charAt(0) - 'a' + 1;
            final int x = Integer.parseInt(split[1]);
            return new Location(x, y);
        } catch (NumberFormatException ex)
        {
            return null;
        }
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
        if (obj instanceof Location == false)
        {
            return false;
        }
        final Location location = (Location) obj;
        return location.x == x && location.y == y;
    }

    @Override
    public final int hashCode()
    {
        int hashCode = 17;
        hashCode = hashCode * 37 + x;
        hashCode = hashCode * 37 + y;
        return hashCode;
    }

    @Override
    public final String toString()
    {
        return "[x=" + x + ",y=" + y + "]";
    }

    /**
     * Returns the <i>x</i> (or <i>column</i>) coordinate.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Returns the <i>y</i> (or <i>row</i>) coordinate.
     */
    public int getY()
    {
        return y;
    }
}