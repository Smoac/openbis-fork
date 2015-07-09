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

package ch.ethz.sis.openbis.generic.dss.api.v3;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.id.datasetfile.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.DataSetFileSearchCriterion;
import ch.systemsx.cisd.common.api.IRpcService;

/**
 * @author Jakub Straszewski
 */
public interface IDataStoreServerApi extends IRpcService
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "data-store-server_INTERNAL";

    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "data-store-server";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v3";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    public List<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriterion searchCriterion);

    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions);

}
