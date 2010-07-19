/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.dto;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Utility class to convert well location to Cartesian coordinates.
 * 
 * @author Tomasz Pylak
 */
public class WellLocationUtils
{
    /** Calculates the row from the given Cartesian coordinates. */
    public static int calcRow(Geometry geometry, int x, int y)
    {
        assert geometry != null;
        assert x >= 0 && x < geometry.getWidth() : x;
        assert y >= 0 && y < geometry.getHeight() : y;

        return geometry.getHeight() - y;
    }

    /** Calculates the column from the given Cartesian coordinates. */
    public static int calcColumn(Geometry geometry, int x, int y)
    {
        assert geometry != null;
        assert x >= 0 && x < geometry.getWidth() : "X is " + x + " (allowed: (0,"
                + (geometry.getWidth() - 1) + ")";
        assert y >= 0 && y < geometry.getHeight() : "Y is " + y + " (allowed: (0,"
                + (geometry.getHeight() - 1) + ")";

        return x + 1;
    }

    /** Calculates the Cartesian x coordinate. */
    public static int calcX(Geometry geometry, int row, int col)
    {
        assert geometry != null;
        assert row > 0 && row <= geometry.getHeight() : "Row is " + row + " (allowed: (1,"
                + geometry.getHeight() + ")";
        assert col > 0 && col <= geometry.getWidth() : "Col is " + col + " (allowed: (1,"
                + geometry.getWidth() + ")";

        return col - 1;
    }

    /** Calculates the Cartesian x coordinate. */
    public static int calcX(Geometry geometry, WellLocation wellLocation)
    {
        return calcX(geometry, wellLocation.getRow(), wellLocation.getColumn());
    }

    /** Calculates the Cartesian y coordinate. */
    public static int calcY(Geometry geometry, WellLocation wellLocation)
    {
        return calcY(geometry, wellLocation.getRow(), wellLocation.getColumn());
    }

    /** Calculates the Cartesian y coordinate. */
    public static int calcY(Geometry geometry, int row, int col)
    {
        assert geometry != null;
        assert row > 0 && row <= geometry.getHeight() : row;
        assert col > 0 && col <= geometry.getWidth() : col;

        return geometry.getHeight() - row;
    }
}
