/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

/**
 * An exception to signal that an feature data set has the wrong number of feature values.
 * 
 * @author Bernd Rinn
 */
public class WrongNumberOfFeatureValuesException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;
    
    private final ImageId imageId;

    private final int numberOfFeaturesExpected;

    private final int numberOfFeaturesFound;

    WrongNumberOfFeatureValuesException(String datasetCode, ImageId imageId,
            int numberOfFeaturesExpected, int numberOffeaturesFound)
    {
        super(String.format(
                "Dataset '%s', %s [# features values expected: %d, # feature values found found: %d]",
                datasetCode, imageId, numberOfFeaturesExpected, numberOffeaturesFound));
        this.datasetCode = datasetCode;
        this.imageId = imageId;
        this.numberOfFeaturesExpected = numberOfFeaturesExpected;
        this.numberOfFeaturesFound = numberOffeaturesFound;
    }

    /**
     * Returns the dataset code of the exception.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * Returns the image id where the exception occurred.
     */
    public ImageId getImageId()
    {
        return imageId;
    }

    /**
     * Returns the number of feature values expected.
     */
    public int getNumberOfFeaturesExpected()
    {
        return numberOfFeaturesExpected;
    }

    /**
     * Returns the number of feature values found.
     */
    public int getNumberOfFeaturesFound()
    {
        return numberOfFeaturesFound;
    }

}
