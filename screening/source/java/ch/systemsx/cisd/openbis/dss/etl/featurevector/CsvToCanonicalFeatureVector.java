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

import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndTitle;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Converts feature vectors from CSV files to CanonicaFeatureVector objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CsvToCanonicalFeatureVector
{
    public static class CsvToCanonicalFeatureVectorConfiguration
    {
        private final String wellRowColumn;

        private final String wellColumnColumn;

        private final boolean isSplit;

        public CsvToCanonicalFeatureVectorConfiguration(String wellRow, String wellColumn)
        {
            this.wellRowColumn = wellRow;
            this.wellColumnColumn = wellColumn;

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

        public boolean isSplit()
        {
            return isSplit;
        }
    }

    private final CsvToCanonicalFeatureVectorConfiguration configuration;

    private final String[] header;

    private final List<String[]> lines;

    private final ArrayList<FeatureColumn> columns = new ArrayList<FeatureColumn>();

    // Will be initialized during conversion
    private int xColumn = -1;

    private int yColumn = -1;

    private int maxRowFound = 0;

    private int maxColFound = 0;

    private final int maxPlateGeometryRow;

    private final int maxPlateGeometryCol;

    public CsvToCanonicalFeatureVector(DatasetFileLines fileLines,
            CsvToCanonicalFeatureVectorConfiguration config, int maxRow, int maxCol)
    {
        this.configuration = config;
        this.header = fileLines.getHeaderTokens();
        this.lines = fileLines.getDataLines();
        this.maxPlateGeometryRow = maxRow;
        this.maxPlateGeometryCol = maxCol;
    }

    public ArrayList<CanonicalFeatureVector> convert()
    {
        initializeColumns();
        readLines();

        return convertColumnsToFeatureVectors();
    }

    private ArrayList<CanonicalFeatureVector> convertColumnsToFeatureVectors()
    {
        final Geometry geometry =
                Geometry.createFromRowColDimensions(maxPlateGeometryRow, maxPlateGeometryCol);

        ArrayList<CanonicalFeatureVector> result = new ArrayList<CanonicalFeatureVector>();
        Counters<String> counters = new Counters<String>();
        for (FeatureColumn column : columns)
        {
            if ((true == column.isWellName) || (false == column.isNumeric))
            {
                continue;
            }
            CanonicalFeatureVector featureVector = convertColumnToFeatureVector(geometry, column, counters);
            result.add(featureVector);
        }

        return result;
    }

    private CanonicalFeatureVector convertColumnToFeatureVector(Geometry geometry,
            FeatureColumn column, Counters<String> counters)
    {
        CanonicalFeatureVector featureVector = new CanonicalFeatureVector();
        CodeAndTitle codeAndTitle = new CodeAndTitle(column.name);
        ImgFeatureDefDTO featureDef = new ImgFeatureDefDTO();
        featureDef.setName(codeAndTitle.getTitle());
        String code = codeAndTitle.getCode();
        int count = counters.count(code);
        featureDef.setCode(count == 1 ? code : code + count);
        featureVector.setFeatureDef(featureDef);
        final PlateFeatureValues valuesValues = convertColumnToByteArray(geometry, column);
        ImgFeatureValuesDTO values = new ImgFeatureValuesDTO(0., 0., valuesValues, 0);
        featureVector.setValues(Collections.singletonList(values));
        return featureVector;
    }

    private PlateFeatureValues convertColumnToByteArray(Geometry geometry, FeatureColumn column)
    {
        final PlateFeatureValues featureValues = new PlateFeatureValues(geometry);
        for (WellLocation loc : column.values.keySet())
        {
            final Float value = column.values.get(loc);
            featureValues.setForWellLocation(value, loc);
        }

        return featureValues;
    }

    private void readLines()
    {
        for (String[] line : lines)
        {
            readLine(line);
        }
        if (maxColFound > maxPlateGeometryCol || maxRowFound > maxPlateGeometryRow)
        {
            throw new IllegalStateException(String.format(
                    "Feature vector has values outside the plate geometry. "
                            + "Plate geometry: (%d, %d), well: (%d, %d).", maxPlateGeometryRow,
                    maxPlateGeometryCol, maxRowFound, maxColFound));
        }
    }

    private void readLine(String[] line)
    {
        final WellLocation well = readWellLocationFromLine(line);
        for (FeatureColumn column : columns)
        {
            if ((true == column.isWellName) || (false == column.isNumeric))
            {
                continue;
            }
            try
            {
                column.values.put(well, Float.parseFloat(line[column.index]));
            } catch (NumberFormatException ex)
            {
                // skip this column in the future
                column.isNumeric = false;
            }
        }

        if (well.getRow() > maxRowFound)
        {
            maxRowFound = well.getRow();
        }

        if (well.getColumn() > maxColFound)
        {
            maxColFound = well.getColumn();
        }
    }

    private WellLocation readWellLocationFromLine(String[] line)
    {
        if (configuration.isSplit())
        {
            String rowString = line[xColumn];
            String colString = line[yColumn];
            return WellLocation.parseLocationStr(rowString, colString);
        } else
        {
            return WellLocation.parseLocationStr(line[xColumn]);
        }
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

        private final HashMap<WellLocation, Float> values;

        // this may change during the course of reading the file
        private boolean isNumeric = true;

        private FeatureColumn(int index, String name, boolean isWellName)
        {
            this.index = index;
            this.name = name;
            this.isWellName = isWellName;
            values = new HashMap<WellLocation, Float>();
        }
    }
}
