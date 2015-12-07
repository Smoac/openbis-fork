/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.download;

import java.io.InputStream;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.DataSetFile;

/**
 * @author pkupczyk
 */
public class DataSetFileDownload
{

    private DataSetFile dataSetFile;

    private InputStream inputStream;

    public DataSetFileDownload(DataSetFile dataSetFile, InputStream inputStream)
    {
        this.dataSetFile = dataSetFile;
        this.inputStream = inputStream;
    }

    public DataSetFile getDataSetFile()
    {
        return dataSetFile;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

}
