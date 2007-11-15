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
    public final int x;

    /**
     * The <i>y</i> (or <i>row</i>) coordinate.
     */
    public final int y;

    public Location(int x, int y)
    {
        assert x > 0 : String.format(NOT_POSITIVE, "x", x);
        this.x = x;
        assert y > 0 : String.format(NOT_POSITIVE, "y", y);
        this.y = y;
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

        return 17 * 37 + x + y;
    }

    @Override
    public final String toString()
    {
        return "[x=" + x + ",y=" + y + "]";
    }
}