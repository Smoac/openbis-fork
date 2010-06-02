/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.geometry;

/**
 * Conversion utility functions.
 * 
 * @author Franz-Josef Elmer
 */
public class ConversionUtils
{
    private static final int MAX_LETTER_NUMBER = getLetterNumber('Z');

    /**
     * Parses a spreadsheet location and return a point. The location has to start with one or more
     * letters ignoring case. This letter section code the x-coordinate with 'A'=0, 'B'=1, ...,
     * 'Z'=25, 'AA'=26, ..., 'AZ'=51, 'BA'=52, 'BB'=53, etc. After the letter section follows one or
     * more digits.
     * <p>
     * Examples:
     * 
     * <pre>
     * A01 -&gt; x = 0, y = 0
     * C7 -&gt; x = 2, y = 6
     * AB19 -&gt; x = 27, y = 18
     * </pre>
     * 
     * Note that the letter points to the column, so the Excel-like convention is used.
     * 
     * @throws IllegalArgumentException if the location is not a valid one.
     */
    public static Point parseSpreadsheetLocation(String spreadsheetLocation)
    {
        if (spreadsheetLocation == null || spreadsheetLocation.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified location.");
        }
        int indexOfFirstDigit = 0;
        while (indexOfFirstDigit < spreadsheetLocation.length()
                && Character.isDigit(spreadsheetLocation.charAt(indexOfFirstDigit)) == false)
        {
            indexOfFirstDigit++;
        }
        if (indexOfFirstDigit == 0)
        {
            throw new IllegalArgumentException("Missing letter part of the location: "
                    + spreadsheetLocation);
        }
        int x = 0;
        for (int i = 0; i < indexOfFirstDigit; i++)
        {
            char letter = spreadsheetLocation.charAt(i);
            int digit = getLetterNumber(letter);
            if (digit < 0 || digit > MAX_LETTER_NUMBER)
            {
                throw new IllegalArgumentException("Invalid letter '" + letter + "' in location: "
                        + spreadsheetLocation);
            }
            x = x * MAX_LETTER_NUMBER + digit;
        }
        int y;
        try
        {
            y = Integer.parseInt(spreadsheetLocation.substring(indexOfFirstDigit));
        } catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Number part of the location is not a number: "
                    + spreadsheetLocation);
        }
        if (y < 1)
        {
            throw new IllegalArgumentException(
                    "Number part of the location is not a positive number: " + spreadsheetLocation);
        }
        return new Point(x - 1, y - 1);

    }

    static public String convertToSpreadsheetLocation(Point p)
    {
        if (p == null)
        {
            throw new IllegalArgumentException("Unspecified point.");
        }
        if (p.getX() >= 26 || p.getX() < 0)
        {
            throw new IllegalArgumentException("Unsupported value: " + p.getX());
        }
        char x = (char) (p.getX() + 'A');
        int y = p.getY() + 1;
        return String.format("%s%s", x, y);
    }

    private static int getLetterNumber(final char ch)
    {
        return Character.toUpperCase(ch) - 'A' + 1;
    }

    private ConversionUtils()
    {
    }
}
