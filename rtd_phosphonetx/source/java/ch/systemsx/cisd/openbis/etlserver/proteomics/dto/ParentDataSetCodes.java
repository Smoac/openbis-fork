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

package ch.systemsx.cisd.openbis.etlserver.proteomics.dto;

import java.util.List;

/**
 * Data transfer object which contains a list of data set codes and an optional error message.
 *
 * @author Franz-Josef Elmer
 */
public class ParentDataSetCodes
{
    private final List<String> dataSetCodes;

    private final String errorMessage;

    public ParentDataSetCodes(List<String> dataSetCodes, String errorMessage)
    {
        super();
        this.dataSetCodes = dataSetCodes;
        this.errorMessage = errorMessage;
    }

    public List<String> getDataSetCodes()
    {
        return dataSetCodes;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

}
