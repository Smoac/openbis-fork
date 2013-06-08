/*
 * Copyright 2011 ETH Zuerich, CISD
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
 * Exception to signal that a dataset is the wrong one in a given context.
 * 
 * @author Bernd Rinn
 */
public class WrongDatasetException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final String datasetCodeFound;

    private final String datasetCodeExpected;

    WrongDatasetException(String datasetCodeExpected, String datasetCodeFound)
    {
        super(String.format("Wrong dataset [expected: '%s', found: '%s']", datasetCodeExpected,
                datasetCodeFound));
        this.datasetCodeExpected = datasetCodeExpected;
        this.datasetCodeFound = datasetCodeFound;
    }

    /**
     * Returns the expected dataset code of the exception.
     */
    public String getExpectedDatasetCode()
    {
        return datasetCodeExpected;
    }

    /**
     * Returns the actually founddataset code of the exception.
     */
    public String getFoundDatasetCode()
    {
        return datasetCodeFound;
    }
}
