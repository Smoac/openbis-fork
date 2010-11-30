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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

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
        this.header = fileLines.getHeaderLabels();
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
            if ((true == column.isWellName) || column.isEmpty())
            {
                continue;
            }
            CanonicalFeatureVector featureVector =
                    convertColumnToFeatureVector(geometry, column, counters);
            result.add(featureVector);
        }

        return result;
    }

    private CanonicalFeatureVector convertColumnToFeatureVector(Geometry geometry,
            FeatureColumn column, Counters<String> counters)
    {
        CanonicalFeatureVector featureVector = new CanonicalFeatureVector();
        CodeAndLabel codeAndTitle = CodeAndLabelUtil.create(column.name);
        ImgFeatureDefDTO featureDef = new ImgFeatureDefDTO();
        featureDef.setLabel(codeAndTitle.getLabel());
        featureDef.setDescription(codeAndTitle.getLabel());
        String code = codeAndTitle.getCode();
        int count = counters.count(code);
        featureDef.setCode(count == 1 ? code : code + count);
        featureVector.setFeatureDef(featureDef);

        return setFeatureValues(featureVector, column, geometry);
    }

    private CanonicalFeatureVector setFeatureValues(CanonicalFeatureVector featureVector,
            FeatureColumn column, Geometry geometry)
    {
        Map<WellLocation, Float> floatValues = column.tryExtractFloatValues();
        List<ImgFeatureVocabularyTermDTO> vocabularyTerms =
                new ArrayList<ImgFeatureVocabularyTermDTO>();
        if (floatValues == null)
        {
            VocabularyFeatureColumnValues vocabularyValues = column.extractVocabularyValues();

            floatValues = vocabularyValues.getWellTermsMapping();
            vocabularyTerms = createVocabularyTerms(vocabularyValues.getValueToSequanceMap());
        }
        final PlateFeatureValues valuesValues = convertColumnToByteArray(geometry, floatValues);
        ImgFeatureValuesDTO values = new ImgFeatureValuesDTO(0., 0., valuesValues, 0);

        featureVector.setValues(Collections.singletonList(values));
        featureVector.setVocabularyTerms(vocabularyTerms);
        return featureVector;
    }

    private List<ImgFeatureVocabularyTermDTO> createVocabularyTerms(
            Map<String, Integer> valueToSequanceMap)
    {
        List<ImgFeatureVocabularyTermDTO> vocabularyTerms =
                new ArrayList<ImgFeatureVocabularyTermDTO>();
        for (Entry<String, Integer> entry : valueToSequanceMap.entrySet())
        {
            vocabularyTerms.add(new ImgFeatureVocabularyTermDTO(entry.getKey(), entry.getValue()));
        }
        return vocabularyTerms;
    }

    private PlateFeatureValues convertColumnToByteArray(Geometry geometry,
            Map<WellLocation, Float> values)
    {
        final PlateFeatureValues featureValues = new PlateFeatureValues(geometry);
        for (WellLocation loc : values.keySet())
        {
            final Float value = values.get(loc);
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
            if (true == column.isWellName)
            {
                continue;
            }
            String columnValue = line[column.index];
            if (StringUtils.isBlank(columnValue) == false)
            {
                column.values.put(well, columnValue);
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

        private final HashMap<WellLocation, String> values;

        private FeatureColumn(int index, String name, boolean isWellName)
        {
            this.index = index;
            this.name = name;
            this.isWellName = isWellName;
            values = new HashMap<WellLocation, String>();
        }

        public boolean isEmpty()
        {
            return values.isEmpty();
        }

        /**
         * Tries to parse all values as float numbers.
         * 
         * @return null if any column value cannot be parsed as float number.
         */
        public Map<WellLocation, Float> tryExtractFloatValues()
        {
            Map<WellLocation, Float> map = new HashMap<WellLocation, Float>();
            for (Entry<WellLocation, String> entry : values.entrySet())
            {
                try
                {
                    WellLocation wellLocation = entry.getKey();
                    String value = entry.getValue();
                    float floatValue = Float.parseFloat(value);
                    map.put(wellLocation, floatValue);
                } catch (NumberFormatException ex)
                {
                    return null;
                }
            }
            return map;
        }

        /**
         * Assuming that all values come from the set fixed of vocabulary terms calculates the
         * mapping from vocabulary term into a unique term sequence number.<br>
         * Should be called when {@link #tryExtractFloatValues} returns null.
         */
        public VocabularyFeatureColumnValues extractVocabularyValues()
        {
            return VocabularyFeatureColumnValues.create(values);
        }

    }

    private static final class VocabularyFeatureColumnValues
    {
        private final Map<String, Integer/* value sequence number */> valueToSequanceMap;

        private final Map<WellLocation, Float> wellTermsMapping;

        public static VocabularyFeatureColumnValues create(Map<WellLocation, String> values)
        {
            Map<String, Integer> valueToSequanceMap = fixVocabularyTermSequences(values);

            Map<WellLocation, Float> wellTermsMapping = new HashMap<WellLocation, Float>();
            for (Entry<WellLocation, String> entry : values.entrySet())
            {
                WellLocation wellLocation = entry.getKey();
                String value = entry.getValue();
                int sequenceNumber = valueToSequanceMap.get(value);
                wellTermsMapping.put(wellLocation, (float) sequenceNumber);
            }

            return new VocabularyFeatureColumnValues(valueToSequanceMap, wellTermsMapping);
        }

        private static Map<String, Integer/* value sequence number */> fixVocabularyTermSequences(
                Map<WellLocation, String> values)
        {
            Set<String> uniqueValues = new HashSet<String>();
            for (String value : values.values())
            {
                uniqueValues.add(value);
            }

            Map<String, Integer> valueToSequanceMap = new HashMap<String, Integer>();
            int sequenceNumber = 0;
            for (String value : uniqueValues)
            {
                valueToSequanceMap.put(value, sequenceNumber++);
            }
            return valueToSequanceMap;
        }

        private VocabularyFeatureColumnValues(Map<String, Integer> valueToSequanceMap,
                Map<WellLocation, Float> wellTermsMapping)
        {
            this.valueToSequanceMap = valueToSequanceMap;
            this.wellTermsMapping = wellTermsMapping;
        }

        /** mapping from term code to sequence number */
        public Map<String, Integer> getValueToSequanceMap()
        {
            return valueToSequanceMap;
        }

        /** mapping between wells and integer sequence numbers of terms casted to floats */
        public Map<WellLocation, Float> getWellTermsMapping()
        {
            return wellTermsMapping;
        }

    }
}
