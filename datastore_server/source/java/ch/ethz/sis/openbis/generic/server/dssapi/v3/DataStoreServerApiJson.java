/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;

/**
 * @author pkupczyk
 */
@Component(DataStoreServerApiJson.INTERNAL_SERVICE_NAME)
public class DataStoreServerApiJson implements IDataStoreServerApi
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "data-store-server-json_INTERNAL";

    @Resource(name = DataStoreServerApi.INTERNAL_SERVICE_NAME)
    private IDataStoreServerApi api;

    @Override
    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions)
    {
        return api.searchFiles(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds, DataSetFileDownloadOptions downloadOptions)
    {
        throw new UnsupportedOperationException("This method is not supported in JSON API");
    }

    @Override
    public int getMajorVersion()
    {
        return api.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return api.getMinorVersion();
    }

}
