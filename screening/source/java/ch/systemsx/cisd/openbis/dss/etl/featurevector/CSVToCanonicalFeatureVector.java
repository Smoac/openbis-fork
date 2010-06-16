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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.geometry.Point;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CSVToCanonicalFeatureVector
{
    public static class CSVToCanonicalFeatureVectorConfiguration
    {
        private final String wellRowColumn;

        private final String wellColumnColumn;

        private final boolean isWellColumnAlphanumeric;

        private final boolean isSplit;

        public CSVToCanonicalFeatureVectorConfiguration(String wellRow, String wellColumn,
                boolean wellColumnIsAlphanumeric)
        {
            this.wellRowColumn = wellRow;
            this.wellColumnColumn = wellColumn;
            this.isWellColumnAlphanumeric = wellColumnIsAlphanumeric;

            isSplit = (false == wellRow.equals(wellColumn));
        }

        public String getWellRowColumn()
        {
            return wellRowColumn;
        }

        public String getWellColumnColumn()
        {
            return wellColumnColumn;
        }

        public boolean isWellColumnAlphanumeric()
        {
            return isWellColumnAlphanumeric;
        }

        public boolean isSplit()
        {
            return isSplit;
        }
    }

    private final CSVToCanonicalFeatureVectorConfiguration configuration;

    private final String[] header;

    private final List<String[]> lines;

    private final ArrayList<FeatureColumn> columns = new ArrayList<FeatureColumn>();

    // Will be initialized during conversion
    private int xColumn = -1;

    private int yColumn = -1;

    private int maxX = 0;

    private int maxY = 0;

    public CSVToCanonicalFeatureVector(DatasetFileLines fileLines,
            CSVToCanonicalFeatureVectorConfiguration config)
    {
        this.configuration = config;
        this.header = fileLines.getHeaderTokens();
        this.lines = fileLines.getDataLines();
    }

    public ArrayList<CanonicalFeatureVector> convert()
    {
        initializeColumns();
        readLines();

        return convertColumnsToFeatureFectors();
    }

    private ArrayList<CanonicalFeatureVector> convertColumnsToFeatureFectors()
    {
        int[] dims =
            { maxX + 1, maxY + 1 };

        ArrayList<CanonicalFeatureVector> result = new ArrayList<CanonicalFeatureVector>();
        for (FeatureColumn column : columns)
        {
            if ((true == column.isWellName) || (false == column.isNumeric))
            {
                continue;
            }
            CanonicalFeatureVector featureVector = convertColumnToFeatureVector(dims, column);
            result.add(featureVector);
        }

        return result;
    }

    private CanonicalFeatureVector convertColumnToFeatureVector(int[] dims, FeatureColumn column)
    {
        CanonicalFeatureVector featureVector = new CanonicalFeatureVector();
        featureVector.setFeatureDef(new ImgFeatureDefDTO(column.name, column.name, 0));
        byte[] valuesValues = convertColumnToByteArray(dims, column);
        ImgFeatureValuesDTO values = new ImgFeatureValuesDTO(0., 0., valuesValues, 0);
        featureVector.setValues(Collections.singletonList(values));
        return featureVector;
    }

    private byte[] convertColumnToByteArray(int[] dims, FeatureColumn column)
    {
        MDDoubleArray doubleArray = new MDDoubleArray(dims);
        for (Point loc : column.values.keySet())
        {
            Double value = column.values.get(loc);
            doubleArray.set(value, loc.getX(), loc.getY());
        }

        return NativeTaggedArray.toByteArray(doubleArray);
    }

    private void readLines()
    {
        for (String[] line : lines)
        {
            readLine(line);
        }
    }

    private void readLine(String[] line)
    {
        Point point = readPointFromLine(line);
        for (FeatureColumn column : columns)
        {
            if ((true == column.isWellName) || (false == column.isNumeric))
            {
                continue;
            }
            try
            {
                column.values.put(point, Double.parseDouble(line[column.index]));
            } catch (NumberFormatException ex)
            {
                // skip this column in the future
                column.isNumeric = false;
            }
        }

        if (point.getX() > maxX)
        {
            maxX = point.getX();
        }

        if (point.getY() > maxY)
        {
            maxY = point.getY();
        }
    }

    private Point readPointFromLine(String[] line)
    {
        int x, y;
        if (configuration.isSplit())
        {
            String xString = line[xColumn];
            String yString = line[yColumn];
            if (configuration.isWellColumnAlphanumeric())
            {
                Location loc =
                        Location.tryCreateLocationFromSplitMatrixCoordinate(xString, yString);
                x = loc.getX();
                y = loc.getY();
            } else
            {
                x = Integer.parseInt(xString);
                y = Integer.parseInt(yString);
            }
        } else
        {
            Location loc = Location.tryCreateLocationFromMatrixCoordinate(line[xColumn]);
            x = loc.getX();
            y = loc.getY();
        }

        // Well-locations are 1-offset; we need 0-offset to put into an matrix
        Point point = new Point(x - 1, y - 1);
        return point;
    }

    private void initializeColumns()
    {
        for (int i = 0; i < header.length; ++i)
        {
            String headerName = header[i];
            boolean isWellName;
            if (configuration.getWellRowColumn().equals(headerName))
            {
                xColumn = i;
                isWellName = true;
            } else if (configuration.getWellColumnColumn().equals(headerName))
            {
                yColumn = i;
                isWellName = true;
            } else
            {
                isWellName = false;
            }
            FeatureColumn featureColumn = new FeatureColumn(i, headerName, isWellName);
            columns.add(featureColumn);
        }

        if (false == configuration.isSplit())
        {
            yColumn = xColumn;
        }

        if (xColumn < 0 || yColumn < 0)
        {
            throw new IllegalArgumentException("Could not parse data set");
        }
    }

    private static class FeatureColumn
    {
        private final int index;

        private final String name;

        private final boolean isWellName;

        private final HashMap<Point, Double> values;

        // this may change during the course of reading the file
        private boolean isNumeric = true;

        private FeatureColumn(int index, String name, boolean isWellName)
        {
            this.index = index;
            this.name = name;
            this.isWellName = isWellName;
            values = new HashMap<Point, Double>();
        }
    }
}
