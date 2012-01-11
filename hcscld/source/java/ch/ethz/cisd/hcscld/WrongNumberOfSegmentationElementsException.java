/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

/**
 * An exception to signal that an image segmentation has the wrong number of elements.
 * 
 * @author Bernd Rinn
 */
public class WrongNumberOfSegmentationElementsException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;

    private final int numberOfElementsExpected;

    private final int numberOfElementsFound;

    WrongNumberOfSegmentationElementsException(String datasetCode, int numberOfElementsExpected,
            int numberOfElementsFound)
    {
        super(
                String.format(
                        "Dataset '%s [# segmentation elements expected: %d, # segmentation elements found: %d]",
                        datasetCode, numberOfElementsExpected, numberOfElementsFound));
        this.datasetCode = datasetCode;
        this.numberOfElementsExpected = numberOfElementsExpected;
        this.numberOfElementsFound = numberOfElementsFound;
    }

    /**
     * Returns the dataset code of the exception.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * Returns the number of elements expected.
     */
    public int getNumberOfElementsExpected()
    {
        return numberOfElementsExpected;
    }

    /**
     * Returns the number of elements found.
     */
    public int getNumberOfElementsFound()
    {
        return numberOfElementsFound;
    }

}
