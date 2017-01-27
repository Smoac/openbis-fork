package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;

public class DataStoreServerApiLogger extends AbstractServerLogger implements
        IDataStoreServerApi
{
    DataStoreServerApiLogger(IInvocationLoggerContext context)
    {
        super(null, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-files", "SEARCH_CRITERION:\n%s\n", searchCriteria);
        return null;
    }

    @Override
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions)
    {
        logAccess(sessionToken, "download-files", "FILE_IDS:\n%s\n%s", fileIds, downloadOptions);
        return null;
    }

    @Override
    public List<DataSetPermId> createDataSets(String sessionToken, List<FullDataSetCreation> newDataSets)
    {
        logAccess(sessionToken, "create-data-sets", "DATA_SETS:\n%s", newDataSets);
        return null;
    }

}