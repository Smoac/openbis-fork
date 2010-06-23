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

package ch.systemsx.cisd.openbis.dss.etl.genedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.geometry.Point;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;

/**
 * Converts currentFeature vectors from the Genedata currentFeature vector file format to
 * CanonicaFeatureVector objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GenedataFormatToCanonicalFeatureVector
{
    private final String layerPrefix;

    private final List<String> lines;

    private final ArrayList<FeatureParser> features = new ArrayList<FeatureParser>();

    public GenedataFormatToCanonicalFeatureVector(List<String> lines, String layerPrefix)
    {
        this.layerPrefix = layerPrefix;
        this.lines = lines;
    }

    public ArrayList<CanonicalFeatureVector> convert()
    {
        readLines();

        return convertFeaturesToFeatureFectors();
    }

    private ArrayList<CanonicalFeatureVector> convertFeaturesToFeatureFectors()
    {
        ArrayList<CanonicalFeatureVector> result = new ArrayList<CanonicalFeatureVector>();
        for (FeatureParser feature : features)
        {
            CanonicalFeatureVector featureVector = convertFeatureToFeatureVector(feature);
            result.add(featureVector);
        }

        return result;
    }

    private CanonicalFeatureVector convertFeatureToFeatureVector(FeatureParser feature)
    {
        int[] dims =
            { feature.numberOfRows, feature.numberOfColumns };

        CanonicalFeatureVector featureVector = new CanonicalFeatureVector();
        featureVector.setFeatureDef(new ImgFeatureDefDTO(feature.name, feature.name, 0));
        MDDoubleArray valuesValues = convertColumnToByteArray(dims, feature);
        ImgFeatureValuesDTO values = new ImgFeatureValuesDTO(0., 0., valuesValues, 0);
        featureVector.setValues(Collections.singletonList(values));
        return featureVector;
    }

    private MDDoubleArray convertColumnToByteArray(int[] dims, FeatureParser feature)
    {
        MDDoubleArray doubleArray = new MDDoubleArray(dims);
        for (Point loc : feature.values.keySet())
        {
            Double value = feature.values.get(loc);
            doubleArray.set(value, loc.getX(), loc.getY());
        }

        return doubleArray;
    }

    private void readLines()
    {
        String featureName = null;
        ArrayList<String> featureLines = new ArrayList<String>();

        // Don't need to do anything with the barcode, just make sure it is there
        extractBarCode(lines.get(0).trim());

        for (int i = 1; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();

            if (StringUtils.isEmpty(line))
            {
                continue;
            }
            // If the line starts with the layer prefix, this is a new feature
            if (line.startsWith(getLayerPrefix()))
            {
                // End the old feature
                if (false == featureLines.isEmpty())
                {
                    createFeature(featureName, featureLines);
                }

                // begin the new feature
                featureName = extractLayer(line, i);
                featureLines = new ArrayList<String>();
            } else
            {
                featureLines.add(line);
            }
        }
        // End the last feature
        createFeature(featureName, featureLines);
    }

    private FeatureParser createFeature(String name, ArrayList<String> featureLines)
    {
        FeatureParser feature = new FeatureParser(name, featureLines);
        feature.parse();
        features.add(feature);
        return feature;
    }

    private String extractBarCode(String firstLine)
    {
        int indexOfEqual = firstLine.indexOf('=');
        if (indexOfEqual < 0)
        {
            throw error(0, firstLine, "Missing '='");
        }
        return firstLine.substring(indexOfEqual + 1).trim();
    }

    private String extractLayer(String line, int lineIndex)
    {
        String layer = line.substring(getLayerPrefix().length());
        if (layer.endsWith(">") == false)
        {
            throw error(lineIndex, line, "Missing '>' at the end");
        }
        return layer.substring(0, layer.length() - 1);
    }

    private String getLayerPrefix()
    {
        return layerPrefix;
    }

    private UserFailureException error(int lineIndex, String line, String reason)
    {
        return new UserFailureException("Error in line " + lineIndex + 1 + ": " + reason + ": "
                + line);
    }

    /**
     * Class for parsing features from the Genedata format.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class FeatureParser
    {
        private final String name;

        private final ArrayList<String> lines;

        private final ArrayList<String> rowLetters;

        private final int numberOfColumns;

        private final HashMap<Point, Double> values;

        // this is not known until we complete processing
        private int numberOfRows = 0;

        private FeatureParser(String name, ArrayList<String> lines)
        {
            this.name = name;
            this.lines = lines;
            this.numberOfColumns = computeNumberOfColumns(lines);
            values = new HashMap<Point, Double>();
            rowLetters = new ArrayList<String>();
        }

        /**
         * Parse the header to get the number of columns
         */
        private int computeNumberOfColumns(List<String> aList)
        {
            StringTokenizer tokenizer = new StringTokenizer(aList.get(0));
            return tokenizer.countTokens();
        }

        public void parse()
        {
            // skip the first line, the header, since the only information it contains is the number
            // of columns
            for (int i = 1; i < lines.size(); ++i)
            {
                pasrseLine(lines.get(i), i);
            }
            numberOfRows = rowLetters.size();
        }

        public void pasrseLine(String line, int lineIndex)
        {
            StringTokenizer tokenizer = new StringTokenizer(line);
            int countTokens = tokenizer.countTokens();
            if (countTokens != numberOfColumns + 1)
            {
                throw error(lineIndex, line, "Inconsistent number of features: Expected "
                        + numberOfColumns + " but was " + (countTokens - 1));
            }

            String rowLetter = tokenizer.nextToken();
            if (rowLetters.contains(rowLetter) == false)
            {
                rowLetters.add(rowLetter);
            }

            for (int i = 0; tokenizer.hasMoreTokens(); ++i)
            {
                String token = tokenizer.nextToken();
                Point point = new Point(rowLetters.size() - 1, i);
                try
                {
                    values.put(point, Double.parseDouble(token));
                } catch (NumberFormatException ex)
                {
                }
            }
        }

        private UserFailureException error(int lineIndex, String line, String reason)
        {
            return new UserFailureException("Error in line " + lineIndex + 1 + ": " + reason + ": "
                    + line);
        }
    }
}
