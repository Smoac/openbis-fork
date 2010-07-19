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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Describes position of the well on the plate.
 * 
 * @author Tomasz Pylak
 */
public class WellLocation implements IsSerializable
{
    private static final int MAX_LETTER_NUMBER = getLetterNumber('Z');

    static final String NOT_POSITIVE = "Given coordinate '%s' must be > 0 (%d <= 0).";

    private int row;

    private int column;

    /** Calculates the row from the given cartesian coordinates. */
    public static int calcRow(Geometry geometry, int x, int y)
    {
        assert geometry != null;
        assert x >= 0 && x < geometry.getWidth() : x;
        assert y >= 0 && y < geometry.getHeight() : y;

        return geometry.getHeight() - y;
    }

    /** Calculates the column from the given cartesian coordinates. */
    public static int calcColumn(Geometry geometry, int x, int y)
    {
        assert geometry != null;
        assert x >= 0 && x < geometry.getWidth() : "X is " + x + " (allowed: (0,"
                + (geometry.getWidth() - 1) + ")";
        assert y >= 0 && y < geometry.getHeight() : "Y is " + y + " (allowed: (0,"
                + (geometry.getHeight() - 1) + ")";

        return x + 1;
    }

    /** Calculates the cartesian x coordinate. */
    public static int calcX(Geometry geometry, int row, int col)
    {
        assert geometry != null;
        assert row > 0 && row <= geometry.getHeight() : "Row is " + row + " (allowed: (1,"
                + geometry.getHeight() + ")";
        assert col > 0 && col <= geometry.getWidth() : "Col is " + col + " (allowed: (1,"
                + geometry.getWidth() + ")";

        return col - 1;
    }

    /** Calculates the cartesian y coordinate. */
    public static int calcY(Geometry geometry, int row, int col)
    {
        assert geometry != null;
        assert row > 0 && row <= geometry.getHeight() : row;
        assert col > 0 && col <= geometry.getWidth() : col;

        return geometry.getHeight() - row;
    }

    /**
     * Parses a location given as a string and returns a {@link WellLocation}. The location has to
     * start with one or more letters ignoring case. This letter section code the x-coordinate with
     * 'A'=1, 'B'=2, ..., 'Z'=26, 'AA'=27, ..., 'AZ'=52, 'BA'=53, 'BB'=54, etc. After the letter
     * section follows one or more digits.
     * <p>
     * Examples:
     * 
     * <pre>
     * A01 -&gt; row = 1, col = 1
     * C7 -&gt; row = 3, col = 7
     * AB19 -&gt; row = 28, col = 19
     * </pre>
     * 
     * @throws IllegalArgumentException if the location is not valid.
     */
    public static WellLocation parseLocationStr(String locationStr)
    {
        if (locationStr == null || locationStr.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified location.");
        }
        int indexOfFirstDigit = 0;
        while (indexOfFirstDigit < locationStr.length()
                && Character.isDigit(locationStr.charAt(indexOfFirstDigit)) == false)
        {
            indexOfFirstDigit++;
        }
        if (indexOfFirstDigit == 0)
        {
            throw new IllegalArgumentException("Missing row part of the location: " + locationStr);
        }
        return parseLocationStr(locationStr.substring(0, indexOfFirstDigit), locationStr
                .substring(indexOfFirstDigit));
    }

    /**
     * Parses a location given as two strings for row and column and returns a {@link WellLocation}.
     * The location has to start with one or more letters ignoring case. This
     * <var>rowLocationStr</var> has 'A'=1, 'B'=2, ..., 'Z'=26, 'AA'=27, ..., 'AZ'=52, 'BA'=53,
     * 'BB'=54, etc. After <var>colLocationStr</var> has one or more digits.
     * <p>
     * Examples:
     * 
     * <pre>
     * A, 01 -&gt; row = 1, col = 1
     * 1, 01 -&gt; row = 1, col = 1
     * C, 7 -&gt; row = 3, col = 7
     * AB, 19 -&gt; row = 28, col = 19
     * 28, 19 -&gt; row = 28, col = 19
     * </pre>
     * 
     * @throws IllegalArgumentException if the location is not valid.
     */
    public static WellLocation parseLocationStr(String rowLocationStr, String colLocationStr)
    {
        if (colLocationStr.length() == 0)
        {
            throw new IllegalArgumentException("No column location given.");
        }
        int col;
        try
        {
            col = Integer.parseInt(colLocationStr);
        } catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Column part of the location is not a number: "
                    + colLocationStr);
        }
        if (col < 1)
        {
            throw new IllegalArgumentException(
                    "Column part of the location is not a positive number: " + colLocationStr);
        }
        return parseLocationStr(rowLocationStr, col);
    }

    /**
     * Parses a location given as two strings for row and column and returns a {@link WellLocation}.
     * The location has to start with one or more letters ignoring case. This
     * <var>rowLocationStr</var> has 'A'=1, 'B'=2, ..., 'Z'=26, 'AA'=27, ..., 'AZ'=52, 'BA'=53,
     * 'BB'=54, etc. After <var>colLocationStr</var> has one or more digits.
     * <p>
     * Examples:
     * 
     * <pre>
     * A, 01 -&gt; row = 1, col = 1
     * 1, 01 -&gt; row = 1, col = 1
     * C, 7 -&gt; row = 3, col = 7
     * AB, 19 -&gt; row = 28, col = 19
     * 28, 19 -&gt; row = 28, col = 19
     * </pre>
     * 
     * @throws IllegalArgumentException if the location is not valid.
     */
    public static WellLocation parseLocationStr(String rowLocationStr, int column)
    {
        if (rowLocationStr.length() == 0)
        {
            throw new IllegalArgumentException("No row location given.");
        }
        int row = 0;
        if (Character.isDigit(rowLocationStr.charAt(0)))
        {
            try
            {
                row = Integer.parseInt(rowLocationStr);
            } catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException(
                        "Row part of the location starts with a digit but is not a valid number: "
                                + rowLocationStr);
            }
            if (row < 1)
            {
                throw new IllegalArgumentException(
                        "Row part of the location is not a positive number: " + rowLocationStr);
            }
        } else
        {
            for (int i = 0; i < rowLocationStr.length(); i++)
            {
                char letter = rowLocationStr.charAt(i);
                int digit = getLetterNumber(letter);
                if (digit < 0 || digit > MAX_LETTER_NUMBER)
                {
                    throw new IllegalArgumentException("Invalid letter '" + letter
                            + "' in row location: " + rowLocationStr);
                }
                row = row * MAX_LETTER_NUMBER + digit;
            }
        }
        if (column < 1)
        {
            throw new IllegalArgumentException(
                    "Column part of the location is not a positive number: " + column);
        }
        return new WellLocation(row, column);
    }

    private static int getLetterNumber(final char ch)
    {
        return Character.toUpperCase(ch) - 'A' + 1;
    }

    // GWT only
    @SuppressWarnings("unused")
    private WellLocation()
    {
    }

    public WellLocation(int row, int column)
    {
        assert row > 0 : String.format(NOT_POSITIVE, "row", row);
        assert column > 0 : String.format(NOT_POSITIVE, "column", column);
        this.row = row;
        this.column = column;
    }

    /** The row (rowCount - y where y is the cartesian coordinate). Starts with 1. */
    public int getRow()
    {
        return row;
    }

    /** The column (x + 1 where x is the cartesian coordinate). Starts with 1. */
    public int getColumn()
    {
        return column;
    }

    /** Calculates the cartesian X coordinate. */
    public int calcX(Geometry geometry)
    {
        return calcX(geometry, row, column);
    }

    /** Calculates the cartesian y coordinate. */
    public int calcY(Geometry geometry)
    {
        return calcY(geometry, row, column);
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "(" + row + "," + column + ")";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WellLocation other = (WellLocation) obj;
        if (column != other.column)
            return false;
        if (row != other.row)
            return false;
        return true;
    }

}
