/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.server;

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Aggregation command which creates a file in session workspace and returns the file name.
 *
 * @author Franz-Josef Elmer
 */
public class AggregationFileCommand extends AggregationCommand
{

    @Override
    protected void aggregate(Map<String, Object> parameters,
            ISimpleTableModelBuilderAdaptor tableBuilder)
    {
        String fileName = createFile(parameters);
        tableBuilder.addHeader(Constants.FILE_NAME_COLUMN);
        tableBuilder.addRow().setCell(Constants.FILE_NAME_COLUMN, fileName);
    }
    
    /**
     * Creates a file by aggregating data based on specified parameters.
     * Should be overridden by subclasses.
     * 
     * @return the file name.
     */
    protected String createFile(Map<String, Object> parameters)
    {
        return null;
    }
}
