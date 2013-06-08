/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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
 * Exception to signal that a dataset has the wrong format.
 * 
 * @author Bernd Rinn
 */
public class WrongDatasetFormatException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;

    private final String expectedFormat;

    private final String foundFormat;

    WrongDatasetFormatException(String datasetCode, String expectedType,
            String foundType)
    {
        super(String.format("Dataset '%s' [type expected: %s, type found: %s]", datasetCode,
                expectedType, foundType));
        this.datasetCode = datasetCode;
        this.expectedFormat = expectedType;
        this.foundFormat = foundType;
    }

    /**
     * Returns the dataset code of the exception.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * Returns the dataset format expected.
     */
    public String getExpectedDatasetType()
    {
        return expectedFormat;
    }

    /**
     * Returns the dataset format found.
     */
    public String getFoundDatasetType()
    {
        return foundFormat;
    }
}
