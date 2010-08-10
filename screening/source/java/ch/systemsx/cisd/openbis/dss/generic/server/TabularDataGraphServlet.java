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

import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ITabularData;
import ch.systemsx.cisd.utils.CsvFileReaderHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphServlet extends AbstractTabularDataGraphServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * Return the tabular data from a file as a DatasetFileLines.
     */
    @Override
    protected ITabularData getDatasetLines(String dataSetCode, String pathOrNull)
            throws IOException
    {
        if (pathOrNull == null)
        {
            throw new UserFailureException("No value for the parameter " + FILE_PATH_PARAM
                    + " found in the URL");
        }
        return CsvFileReaderHelper.getDatasetFileLines(new File(pathOrNull), configuration);
    }
}
