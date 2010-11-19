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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores the model of {@link PlateLayouter}. Contains some logic to change the model.
 * 
 * @author Tomasz Pylak
 */
class PlateLayouterModel
{
    private final WellData[][] wellMatrix;

    private final List<WellData> wellList; // the same wells as in the matrix

    // --- internal dynamix state

    private DatasetImagesReference imageDatasetOrNull;

    private List<String> featureLabelsOrNull;

    // ---

    public PlateLayouterModel(PlateMetadata plateMetadata)
    {
        this.wellMatrix = createWellMatrix(plateMetadata);
        this.wellList = asList(wellMatrix);
    }

    public WellData[][] getWellMatrix()
    {
        return wellMatrix;
    }

    public List<WellData> getWellList()
    {
        return wellList;
    }

    public DatasetImagesReference tryGetImageDataset()
    {
        return imageDatasetOrNull;
    }

    public void setImageDataset(DatasetImagesReference imageDataset)
    {
        this.imageDatasetOrNull = imageDataset;
    }

    public List<String> tryGetFeatureLabels()
    {
        return featureLabelsOrNull;
    }

    // --- some logic

    public void setFeatureVectorDataset(FeatureVectorDataset featureVectorDatasetOrNull)
    {
        unsetFeatureVectors();
        if (featureVectorDatasetOrNull == null)
        {
            this.featureLabelsOrNull = null;
        } else
        {
            this.featureLabelsOrNull = featureVectorDatasetOrNull.getFeatureLabels();
            List<? extends FeatureVectorValues> features =
                    featureVectorDatasetOrNull.getDatasetFeatures();
            for (FeatureVectorValues featureVector : features)
            {
                WellLocation loc = featureVector.getWellLocation();
                WellData wellData = tryGetWellData(loc);
                if (wellData != null)
                {
                    wellData.setFeatureValues(featureVector.getFeatureValues());
                }
            }
        }
    }

    private WellData tryGetWellData(WellLocation loc)
    {
        int rowIx = loc.getRow() - 1;
        int colIx = loc.getColumn() - 1;
        if (rowIx < wellMatrix.length && colIx < wellMatrix[rowIx].length)
        {
            return wellMatrix[rowIx][colIx];
        } else
        {
            // can happen if the plate geometry is not in sync with the feature vector database
            return null;
        }
    }

    private void unsetFeatureVectors()
    {
        for (WellData well : wellList)
        {
            well.setFeatureValues(null);
        }
    }

    // ------------------------

    // Elements will NOT contain null even if well is empty.
    private static WellData[][] createWellMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] matrix = createEmptyWellMatrix(plateMetadata);
        List<WellMetadata> wells = plateMetadata.getWells();
        for (WellMetadata well : wells)
        {
            WellLocation location = well.tryGetLocation();
            if (location != null)
            {
                WellData wellData = matrix[location.getRow() - 1][location.getColumn() - 1];
                wellData.setMetadata(well);
            }
        }
        return matrix;
    }

    private static WellData[][] createEmptyWellMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] data = new WellData[plateMetadata.getRowsNum()][plateMetadata.getColsNum()];
        Experiment experiment = plateMetadata.getPlate().getExperiment();
        for (int row = 0; row < data.length; row++)
        {
            for (int col = 0; col < data[row].length; col++)
            {
                data[row][col] = new WellData(new WellLocation(row + 1, col + 1), experiment);
            }
        }
        return data;
    }

    private static <T> List<T> asList(T[][] matrix)
    {
        List<T> result = new ArrayList<T>();
        for (int row = 0; row < matrix.length; row++)
        {
            for (int col = 0; col < matrix[row].length; col++)
            {
                result.add(matrix[row][col]);
            }
        }
        return result;
    }

}