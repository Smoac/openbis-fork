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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Describes the whole plate - metadata of each non-empty well, references to datasets with images
 * and analysis results.
 * 
 * @author Tomasz Pylak
 */
public class PlateContent implements IsSerializable
{
    // reference to dataset will be null if not exactly one dataset with images exist for a plate
    private PlateImages plateImages;

    // not null if exactly one image analysis dataset exists
    private DatasetReference imageAnalysisDatasetOrNull;

    private int imageDatasetsNumber;

    private int imageAnalysisDatasetsNumber;

    // GWT only
    @SuppressWarnings("unused")
    private PlateContent()
    {
    }

    public PlateContent(Sample plate, List<WellMetadata> wells,
            DatasetImagesReference imagesOrNull, int imageDatasetsNumber,
            DatasetReference imageAnalysisDatasetOrNull, int imageAnalysisDatasetsNumber)
    {
        assert (imagesOrNull != null && imageDatasetsNumber == 1)
                || (imagesOrNull == null && imageDatasetsNumber != 1);
        assert (imageAnalysisDatasetOrNull != null && imageAnalysisDatasetsNumber == 1)
                || (imageAnalysisDatasetOrNull == null && imageAnalysisDatasetsNumber != 1);
        this.plateImages = new PlateImages(plate, wells, imagesOrNull);
        this.imageDatasetsNumber = imageDatasetsNumber;
        this.imageAnalysisDatasetOrNull = imageAnalysisDatasetOrNull;
        this.imageAnalysisDatasetsNumber = imageAnalysisDatasetsNumber;
    }

    public DatasetReference tryGetImageAnalysisDataset()
    {
        return imageAnalysisDatasetOrNull;
    }

    public int getImageDatasetsNumber()
    {
        return imageDatasetsNumber;
    }

    public int getImageAnalysisDatasetsNumber()
    {
        return imageAnalysisDatasetsNumber;
    }

    public PlateImages getPlateImages()
    {
        return plateImages;
    }

    public Sample getPlate()
    {
        return plateImages.getPlate();
    }
}
