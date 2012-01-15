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
public class WrongNumberOfSegmentedObjectsException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;

    private final int numberOfObjectsExpected;

    private final int numberOfObjectsFound;

    WrongNumberOfSegmentedObjectsException(String datasetCode, int numberOfObjectsExpected,
            int numberOfObjectsFound)
    {
        super(
                String.format(
                        "Dataset '%s [# segmented objects expected: %d, # segmented objects found: %d]",
                        datasetCode, numberOfObjectsExpected, numberOfObjectsFound));
        this.datasetCode = datasetCode;
        this.numberOfObjectsExpected = numberOfObjectsExpected;
        this.numberOfObjectsFound = numberOfObjectsFound;
    }

    /**
     * Returns the dataset code of the exception.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * Returns the number of objects expected.
     */
    public int getNumberOfObjectsExpected()
    {
        return numberOfObjectsExpected;
    }

    /**
     * Returns the number of objects found.
     */
    public int getNumberOfObjectsFound()
    {
        return numberOfObjectsFound;
    }

}
