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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Bean for a row in a table of feature vectors. Each row is specified by data set code, plate
 * identifier, well coordinates and and array of feature values. Double.NaN is used for unknown
 * feature value in this array.
 * 
 * @author Franz-Josef Elmer
 */
public class FeatureTableRow
{
    private String dataSetCode;

    private SampleIdentifier plateIdentifier;

    private int rowIndex;

    private int columnIndex;

    private float[] featureValues;

    public final String getDataSetCode()
    {
        return dataSetCode;
    }

    public final void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    public final SampleIdentifier getPlateIdentifier()
    {
        return plateIdentifier;
    }

    public final void setPlateIdentifier(SampleIdentifier plateIdentifier)
    {
        this.plateIdentifier = plateIdentifier;
    }

    public final int getRowIndex()
    {
        return rowIndex;
    }

    public final void setRowIndex(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }

    public final int getColumnIndex()
    {
        return columnIndex;
    }

    public final void setColumnIndex(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    public final float[] getFeatureValues()
    {
        return featureValues;
    }

    public final double[] getFeatureValuesAsDouble()
    {
        final double[] doubleValues = new double[featureValues.length];
        for (int i = 0; i < featureValues.length; ++i)
        {
            doubleValues[i] = featureValues[i];
        }
        return doubleValues;
    }

    public final void setFeatureValues(float[] featureValues)
    {
        this.featureValues = featureValues;
    }
}
