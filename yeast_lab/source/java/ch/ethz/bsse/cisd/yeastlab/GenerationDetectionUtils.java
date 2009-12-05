/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.yeastlab;

import ch.ethz.bsse.cisd.yeastlab.model.Cell;

/**
 * Utility methods and constants for Generation Detection algorithm computations, input and output.
 * 
 * @author Piotr Buczek
 */
public class GenerationDetectionUtils
{
    public static final String NEW_LINE = "\n";

    public static final String SEPARATOR = "\t";

    /** @returns squared distance between specified cells */
    public static int distanceSq(Cell c1, Cell c2)
    {
        final int dx = c1.getX() - c2.getX();
        final int dy = c1.getY() - c2.getY();
        return dx * dx + dy * dy;
    }

    /** @returns ceiling of given value squared */
    public static int square(double value)
    {
        return (int) Math.ceil(value * value);
    }

    public static String intToString(int value)
    {
        return String.format("%d", value);
    }

    public static String doubleToString(double value)
    {
        return String.format("%1.2f", value);
    }

    /** log (on stderr) */
    public static void log(String message)
    {
        System.err.println(message);
    }

    /** log (on stderr) if debug messages are enabled */
    public static void debug(String message)
    {
        if (GenerationDetection.DEBUG)
        {
            System.err.println("DEBUG: " + message);
        }
    }
}
