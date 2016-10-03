/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.harvester.synchronizer;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
class DSSFileUtils
{
    public static final int TIMEOUT = 10000;

    private final IDataStoreServerApi dss;
    private final IApplicationServerApi as;

    public static DSSFileUtils create(String asUrl, String dssUrl)
    {
        return new DSSFileUtils(asUrl, dssUrl, TIMEOUT);
    }

    private DSSFileUtils (String asUrl, String dssUrl, int timeout)
    {
        SslCertificateHelper.trustAnyCertificate(asUrl);
        SslCertificateHelper.trustAnyCertificate(dssUrl);

        this.dss =HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class, dssUrl + IDataStoreServerApi.SERVICE_URL, timeout);
        this.as = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, asUrl + IApplicationServerApi.SERVICE_URL, timeout);    
     }

    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria criteria, DataSetFileFetchOptions dsFileFetchOptions)
    {
        return dss.searchFiles(sessionToken, criteria, dsFileFetchOptions);
    }

    public SearchResult<DataSetFile> searchWithDataSetCode(String sessionToken, String dataSetCode, DataSetFileFetchOptions dsFileFetchOptions)
    {
        DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
        criteria.withDataSet().withCode().thatEquals(dataSetCode);
        return searchFiles(sessionToken, criteria, dsFileFetchOptions);
    }

    public InputStream downloadFiles(String sessionToken, List<IDataSetFileId> fileIds, DataSetFileDownloadOptions options)
    {
        return dss.downloadFiles(sessionToken, fileIds, options);
    }

    public String login(String loginUser, String loginPass)
    {
        return as.login(loginUser, loginPass);
    }
}
